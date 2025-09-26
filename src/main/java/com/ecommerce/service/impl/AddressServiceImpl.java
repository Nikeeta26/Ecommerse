package com.ecommerce.service.impl;

import com.ecommerce.dto.AddressResponse;
import com.ecommerce.model.Address;
import com.ecommerce.model.User;
import com.ecommerce.repository.AddressRepository;
import com.ecommerce.dto.UpdateAddressRequest;
import com.ecommerce.service.AddressService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    private static final Logger logger = LoggerFactory.getLogger(AddressServiceImpl.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY = 1000L; // 1 second

    private final AddressRepository addressRepository;

    @Autowired
    public AddressServiceImpl(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> list(User user) {
        logger.debug("Fetching all addresses for user: {}", user.getId());
        return addressRepository.findByUser(user).stream()
                .map(AddressResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> listActive(User user) {
        logger.debug("Fetching active address for user: {}", user.getId());
        return addressRepository.findByUserAndIsActiveOrderByUpdatedAtDesc(user, true)
                .stream()
                .map(AddressResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<AddressResponse> getDefaultAddress(User user) {
        logger.debug("Fetching default address for user: {}", user.getId());
        return addressRepository.findByUserAndIsDefault(user, true)
                .map(AddressResponse::fromEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> listByUserId(Long userId) {
        logger.debug("Fetching all addresses for user ID: {}", userId);
        return addressRepository.findByUserId(userId).stream()
                .map(AddressResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        propagation = Propagation.REQUIRED,
        timeout = 30 // 30 seconds
    )
    @Retryable(
        value = { ObjectOptimisticLockingFailureException.class },
        maxAttempts = MAX_RETRY_ATTEMPTS,
        backoff = @Backoff(delay = RETRY_DELAY)
    )
    public AddressResponse create(User user, Address address) {
        validateAddress(address);
        logger.info("Creating new address for user: {}", user.getId());

        // Deactivate all other addresses for this user
        deactivateAllUserAddresses(user);

        // Set up the new address
        address.setUser(user);
        address.setActive(true);
        
        // If this is the first address, set it as default
        boolean isFirstAddress = addressRepository.findByUser(user).isEmpty();
        address.setDefault(isFirstAddress);

        try {
            Address savedAddress = addressRepository.save(address);
            logger.info("Successfully created address with ID: {}", savedAddress.getId());
            return AddressResponse.fromEntity(savedAddress);
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while creating address for user: {}", user.getId(), e);
            throw new IllegalStateException("Could not create address due to data integrity violation", e);
        }
    }
    
    @Override
    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        propagation = Propagation.REQUIRED,
        timeout = 30 // 30 seconds
    )
    @Retryable(
        value = { ObjectOptimisticLockingFailureException.class },
        maxAttempts = MAX_RETRY_ATTEMPTS,
        backoff = @Backoff(delay = RETRY_DELAY)
    )
    public AddressResponse update(User user, Long id, UpdateAddressRequest updateRequest) {
        logger.debug("Updating address ID: {} for user: {}", id, user.getId());
        
        if (updateRequest == null) {
            throw new IllegalArgumentException("Update request cannot be null");
        }
        
        Address address = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> {
                    logger.warn("Address not found with ID: {} for user: {}", id, user.getId());
                    return new EntityNotFoundException("Address not found");
                });
        
        // Handle active status update
        if (updateRequest.isActive() != null) {
            if (Boolean.TRUE.equals(updateRequest.isActive()) && !address.isActive()) {
                logger.debug("Activating address ID: {} for user: {}", id, user.getId());
                // If activating, ensure only one active address exists
                deactivateOtherAddresses(user, id);
                address.setActive(true);
            } else if (Boolean.FALSE.equals(updateRequest.isActive()) && address.isActive()) {
                logger.debug("Deactivating address ID: {} for user: {}", id, user.getId());
                address.setActive(false);
                
                // If deactivating the default address, set a new default
                if (address.isDefault()) {
                    setNewDefaultAddress(user);
                }
            }
        }
        
        // Handle default status update
        if (updateRequest.getDefault() != null) {
            if (Boolean.TRUE.equals(updateRequest.getDefault()) && !address.isDefault()) {
                logger.debug("Setting address ID: {} as default for user: {}", id, user.getId());
                // Find and unset current default address
                addressRepository.findByUserAndIsDefault(user, true)
                    .ifPresent(currentDefault -> {
                        currentDefault.setDefault(false);
                        addressRepository.save(currentDefault);
                    });
                address.setDefault(true);
                
                // Ensure default address is always active
                if (!address.isActive()) {
                    address.setActive(true);
                    logger.debug("Auto-activating default address ID: {}", id);
                }
            } else if (Boolean.FALSE.equals(updateRequest.getDefault()) && address.isDefault()) {
                logger.debug("Unsetting default status for address ID: {} for user: {}", id, user.getId());
                address.setDefault(false);
                // Set a new default address if available
                setNewDefaultAddress(user);
            }
        }
        
        // Update other fields from request
        updateAddressFields(address, updateRequest);

        try {
            Address updatedAddress = addressRepository.save(address);
            logger.info("Successfully updated address ID: {}", updatedAddress.getId());
            return AddressResponse.fromEntity(updatedAddress);
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while updating address ID: {}", id, e);
            throw new IllegalStateException("Could not update address due to data integrity violation", e);
        }
    }
    
    @Override
    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        propagation = Propagation.REQUIRED
    )
    public void delete(User user, Long id) {
        logger.debug("Deleting address ID: {} for user: {}", id, user.getId());
        
        Address existing = addressRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Address not found for deletion - ID: {}, User: {}", id, user.getId());
                    return new EntityNotFoundException("Address not found");
                });

        if (!existing.getUser().getId().equals(user.getId())) {
            logger.warn("User {} attempted to delete address {} belonging to another user", 
                user.getId(), id);
            throw new SecurityException("Not authorized to delete this address");
        }

        try {
            addressRepository.delete(existing);
            logger.info("Successfully deleted address ID: {}", id);
            
            // If the deleted address was default, set the most recent address as default
            if (existing.isDefault()) {
                setNewDefaultAddress(user);
            }
        } catch (Exception e) {
            logger.error("Error deleting address ID: {}", id, e);
            throw new RuntimeException("Error deleting address", e);
        }
    }
    
    // Helper Methods
    
    private void validateAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        if (!StringUtils.hasText(address.getAddressLine1())) {
            throw new IllegalArgumentException("Address line 1 is required");
        }
        if (!StringUtils.hasText(address.getCity())) {
            throw new IllegalArgumentException("City is required");
        }
        if (!StringUtils.hasText(address.getPostalCode())) {
            throw new IllegalArgumentException("Postal code is required");
        }
        if (!StringUtils.hasText(address.getCountry())) {
            throw new IllegalArgumentException("Country is required");
        }
    }
    
    private void updateAddressFields(Address address, UpdateAddressRequest updateRequest) {
        if (updateRequest.getAddressLine1() != null) {
            address.setAddressLine1(updateRequest.getAddressLine1());
        }
        if (updateRequest.getAddressLine2() != null) {
            address.setAddressLine2(updateRequest.getAddressLine2());
        }
        if (updateRequest.getCity() != null) {
            address.setCity(updateRequest.getCity());
        }
        if (updateRequest.getState() != null) {
            address.setState(updateRequest.getState());
        }
        if (updateRequest.getPostalCode() != null) {
            address.setPostalCode(updateRequest.getPostalCode());
        }
        if (updateRequest.getCountry() != null) {
            address.setCountry(updateRequest.getCountry());
        }
        if (updateRequest.getAddressType() != null) {
            address.setAddressType(updateRequest.getAddressType());
        }
        if (updateRequest.getIsDefault() != null) {
            address.setDefault(updateRequest.getIsDefault());
        }
    }
    
    private void deactivateOtherAddresses(User user, Long currentAddressId) {
        List<Address> otherAddresses = addressRepository.findByUserAndIdNot(user, currentAddressId);
        if (!otherAddresses.isEmpty()) {
            otherAddresses.forEach(addr -> {
                addr.setActive(false);
                // If we're activating a new address, ensure it's not marked as default
                // unless explicitly set
                if (addr.isDefault()) {
                    addr.setDefault(false);
                }
            });
            addressRepository.saveAll(otherAddresses);
        }
    }
    
    private void deactivateAllUserAddresses(User user) {
        List<Address> existingAddresses = addressRepository.findByUser(user);
        if (!existingAddresses.isEmpty()) {
            existingAddresses.forEach(addr -> addr.setActive(false));
            addressRepository.saveAll(existingAddresses);
        }
    }
    
    private void setNewDefaultAddress(User user) {
        // Get the most recently updated active address to set as default
        addressRepository.findTopByUserAndIsActiveOrderByUpdatedAtDesc(user)
            .ifPresent(address -> {
                address.setDefault(true);
                addressRepository.save(address);
                logger.info("Set address ID: {} as new default for user: {}", 
                    address.getId(), user.getId());
            });
    }
}
