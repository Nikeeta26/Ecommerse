package com.ecommerce.service;

import com.ecommerce.dto.AddressResponse;
import com.ecommerce.dto.UpdateAddressRequest;
import com.ecommerce.model.Address;
import com.ecommerce.model.User;

import java.util.List;
import java.util.Optional;

public interface AddressService {
    List<AddressResponse> list(User user);
    List<AddressResponse> listActive(User user);
    Optional<AddressResponse> getDefaultAddress(User user);
    List<AddressResponse> listByUserId(Long userId);
    AddressResponse create(User user, Address address);
    AddressResponse update(User user, Long id, UpdateAddressRequest updateRequest);
    void delete(User user, Long id);
}
