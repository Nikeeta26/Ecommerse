package com.ecommerce.dto;

import com.ecommerce.model.Address;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddressResponse {
    private Long id;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    
    @JsonProperty("isDefault")
    private boolean isDefault;
    
    private String addressType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
    
    // Manual getters and setters for boolean fields to handle JSON serialization properly
    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    @JsonProperty("isActive")
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public static AddressResponse fromEntity(Address address) {
        if (address == null) {
            return null;
        }
        
        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setAddressLine1(address.getAddressLine1());
        response.setAddressLine2(address.getAddressLine2());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setPostalCode(address.getPostalCode());
        response.setCountry(address.getCountry());
        response.setDefault(address.isDefault());
        response.setAddressType(address.getAddressType() != null ? address.getAddressType().name() : null);
        response.setCreatedAt(address.getCreatedAt());
        response.setUpdatedAt(address.getUpdatedAt());
        response.setActive(address.isActive());
        
        return response;
    }
}
