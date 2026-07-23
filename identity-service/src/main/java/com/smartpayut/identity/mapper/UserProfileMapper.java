package com.smartpayut.identity.mapper;

import com.smartpayut.identity.domain.entity.*;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class UserProfileMapper {
    public UserProfile create(UserAccount account) {
        return new UserProfile(UUID.randomUUID(), account);
    }
}
