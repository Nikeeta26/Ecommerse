package com.ecommerce.service;

import com.ecommerce.model.Address;
import com.ecommerce.model.User;

import java.util.List;

public interface AddressService {
    List<Address> list(User user);
    Address create(User user, Address address);
    Address update(User user, Long id, Address address);
    void delete(User user, Long id);
}
