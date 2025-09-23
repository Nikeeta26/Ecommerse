package com.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminSignupRequest {

    @NotBlank
    @Size(min = 3, max = 80)
    private String fullName;

    @Email
    private String email; // optional if phone provided

    private String phone; // optional if email provided

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    // Admin setup code can be provided in body as an alternative to header
    @NotBlank
    private String setupCode;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getSetupCode() { return setupCode; }
    public void setSetupCode(String setupCode) { this.setupCode = setupCode; }

    public SignupRequest toSignupRequest() {
        SignupRequest s = new SignupRequest();
        s.setFullName(fullName);
        s.setEmail(email);
        s.setPhone(phone);
        s.setPassword(password);
        return s;
    }
}
