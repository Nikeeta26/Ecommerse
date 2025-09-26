package com.ecommerce.controller;

import com.ecommerce.dto.AddressResponse;
import com.ecommerce.model.Address;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.dto.UpdateAddressRequest;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<AddressResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        var user = userRepository.findById(principal.getId()).orElseThrow();
        return ResponseEntity.ok(addressService.list(user));
    }

    @GetMapping("/active")
    public ResponseEntity<AddressResponse> getActiveAddress(@AuthenticationPrincipal UserPrincipal principal) {
        var user = userRepository.findById(principal.getId()).orElseThrow();
        List<AddressResponse> activeAddresses = addressService.listActive(user);

        return activeAddresses.isEmpty()
            ? ResponseEntity.notFound().build()
            : ResponseEntity.ok(activeAddresses.stream()
                .max(Comparator.comparing(AddressResponse::getUpdatedAt))
                .orElseThrow());
    }

    @GetMapping("/default")
    public ResponseEntity<AddressResponse> getDefaultAddress(@AuthenticationPrincipal UserPrincipal principal) {
        var user = userRepository.findById(principal.getId()).orElseThrow();
        return addressService.getDefaultAddress(user)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AddressResponse>> listByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(addressService.listByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<AddressResponse> create(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody Address address) {
        var user = userRepository.findById(principal.getId()).orElseThrow();
        // Ensure the new address is set as active
        address.setActive(true);
        AddressResponse createdAddress = addressService.create(user, address);
        return ResponseEntity.ok(createdAddress);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> update(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long id,
        @Valid @RequestBody UpdateAddressRequest updateRequest) {
        var user = userRepository.findById(principal.getId()).orElseThrow();
        return ResponseEntity.ok(addressService.update(user, id, updateRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long id) {
        var user = userRepository.findById(principal.getId()).orElseThrow();
        addressService.delete(user, id);
        return ResponseEntity.noContent().build();
    }
}




//curl --location 'http://localhost:8080/api/addresses' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJxcUBnbWFpbC5jb20iLCJpYXQiOjE3NTg5MDc5NjMsImV4cCI6MTc1ODk5NDM2M30.tVST0xsGg7XOm2ysToCvO_Lew9Zc2klS2GyYnnxKFYvyZ6ptec6PdCZrdMkKrx0S' \
//        --header 'Content-Type: application/json' \
//        --data '{
//        "addressLine1": "mncmvbmf Main Street",
//        "addressLine2": "m fmg 4B",
//        "city": "Nm dmfgew York",
//        "state": "NY",
//        "postalCode": "10001",
//        "country": "USA",
//        "isDefault":true,
//        "addressType": "HOME",
//        "isActive": true
//        }




//curl --location 'http://localhost:8080/api/addresses/active' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTg5MTE1MDEsImV4cCI6MTc1ODk5NzkwMX0.oDpUAtKkjbPUdBcC6Fo3CFlP5H0TjA7cSHsv-ssbr6prZfp7PkDozfMLW4lgTZc2'
//



//curl --location --request PUT 'http://localhost:8080/api/addresses/17' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTg5MTE1MDEsImV4cCI6MTc1ODk5NzkwMX0.oDpUAtKkjbPUdBcC6Fo3CFlP5H0TjA7cSHsv-ssbr6prZfp7PkDozfMLW4lgTZc2' \
//        --header 'Content-Type: application/json' \
//        --data '{
//        "addressLine1": "mncmvbmf Main Street",
//        "addressLine2": "m fmg 4B",
//        "city": "Nm dmfgew York",
//        "state": "NY",
//        "postalCode": "10001",
//        "country": "USA",
//        "isDefault": true,
//        "addressType": "HOME",
//        "isActive": true
//        }'





