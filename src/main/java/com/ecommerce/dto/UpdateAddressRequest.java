package com.ecommerce.dto;

import com.ecommerce.model.Address.AddressType;
import lombok.Data;

@Data
public class UpdateAddressRequest {
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean isDefault;
    private Boolean isActive;
    private AddressType addressType;

    // Custom getter for isDefault to match the field name in the Address entity
    public Boolean getDefault() {
        return isDefault;
    }

    // Custom setter for isDefault to match the field name in the Address entity
    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    // Custom getter for isActive to match the field name in the Address entity
    public Boolean isActive() {
        return isActive;
    }
    
    // Custom setter for isActive to match the field name in the Address entity
    public void setActive(Boolean active) {
        isActive = active;
    }
}
