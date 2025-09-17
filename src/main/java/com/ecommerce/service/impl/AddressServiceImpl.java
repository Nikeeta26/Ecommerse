package com.ecommerce.service.impl;

import com.ecommerce.model.Address;
import com.ecommerce.model.User;
import com.ecommerce.repository.AddressRepository;
import com.ecommerce.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired private AddressRepository addressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Address> list(User user) {
        return addressRepository.findByUser(user);
    }

    @Override
    @Transactional
    public Address create(User user, Address address) {
        address.setId(null);
        address.setUser(user);
        return addressRepository.save(address);
    }

    @Override
    @Transactional
    public Address update(User user, Long id, Address address) {
        Address existing = addressRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Address does not belong to user");
        }
        existing.setAddressLine1(address.getAddressLine1());
        existing.setAddressLine2(address.getAddressLine2());
        existing.setCity(address.getCity());
        existing.setState(address.getState());
        existing.setPostalCode(address.getPostalCode());
        existing.setCountry(address.getCountry());
        existing.setAddressType(address.getAddressType());
        existing.setDefault(address.isDefault());
        return addressRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(User user, Long id) {
        Address existing = addressRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Address does not belong to user");
        }
        addressRepository.delete(existing);
    }
}
