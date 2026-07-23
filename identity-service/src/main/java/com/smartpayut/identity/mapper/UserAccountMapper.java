package com.smartpayut.identity.mapper;

import com.smartpayut.identity.domain.entity.*;
import com.smartpayut.identity.dto.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserAccountMapper {
    public UserResponse toResponse(UserAccount u, UserProfile p) {
        return new UserResponse(u.getId(), u.getId(), u.getUserName(), u.getEmail(), u.getName(), u.getLastName(),
                u.getStatus(), u.getCreatedAt(), u.getUpdatedAt(), p == null ? null : p.getImage());
    }
}
