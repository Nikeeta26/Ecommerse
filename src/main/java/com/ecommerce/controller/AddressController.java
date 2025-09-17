package com.ecommerce.controller;

import com.ecommerce.model.Address;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired private AddressService addressService;
    @Autowired private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Address>> list(@AuthenticationPrincipal UserPrincipal principal) {
        var user = userRepository.findById(principal.getId()).orElseThrow();
        return ResponseEntity.ok(addressService.list(user));
    }

    @PostMapping
    public ResponseEntity<Address> create(@AuthenticationPrincipal UserPrincipal principal,
                                          @Valid @RequestBody Address address) {
        var user = userRepository.findById(principal.getId()).orElseThrow();
        return ResponseEntity.ok(addressService.create(user, address));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Address> update(@AuthenticationPrincipal UserPrincipal principal,
                                          @PathVariable Long id,
                                          @Valid @RequestBody Address address) {
        var user = userRepository.findById(principal.getId()).orElseThrow();
        return ResponseEntity.ok(addressService.update(user, id, address));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable Long id) {
        var user = userRepository.findById(principal.getId()).orElseThrow();
        addressService.delete(user, id);
        return ResponseEntity.noContent().build();
    }
}
