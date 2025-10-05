package com.ecommerce.mapper;

import com.ecommerce.dto.UserDto;
import com.ecommerce.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    
    public UserDto toUserDto(User user) {
        if (user == null) {
            return null;
        }
        
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
