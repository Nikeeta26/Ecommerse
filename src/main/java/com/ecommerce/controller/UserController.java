package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.UpdateProfileRequest;
import com.ecommerce.dto.UserProfileResponse;
import com.ecommerce.model.User;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(@AuthenticationPrincipal User user) {
        UserProfileResponse profile = userService.getUserProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", profile));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateCurrentUser(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse updatedProfile = userService.updateUserProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedProfile));
    }

    @PostMapping("/me/avatar")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfileImage(
            @AuthenticationPrincipal User user,
            @RequestParam("image") MultipartFile imageFile) {
        // In a real application, you would upload the file to a storage service (e.g., S3)
        // and get the URL. For now, we'll just return a placeholder.
        String imageUrl = "https://example.com/profiles/" + imageFile.getOriginalFilename();
        
        UserProfileResponse updatedProfile = userService.updateProfileImage(user.getId(), imageUrl);
        return ResponseEntity.ok(ApiResponse.success("Profile image updated successfully", updatedProfile));
    }
}
