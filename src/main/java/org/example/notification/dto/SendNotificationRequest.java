package org.example.notification.dto;

public record SendNotificationRequest(
        String email,
        String operation
) {
}
