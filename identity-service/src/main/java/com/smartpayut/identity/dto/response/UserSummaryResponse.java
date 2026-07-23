package com.smartpayut.identity.dto.response;

public record UserSummaryResponse(
        long totalUsers,
        long activeUsers,
        long inactiveUsers,
        long deletedUsers) {
}
