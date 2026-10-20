package org.example.notification.controller;

import org.example.notification.dto.SendNotificationRequest;
import org.example.notification.service.EmailNotificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final EmailNotificationService emailNotificationService;

    public NotificationController(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(@RequestBody SendNotificationRequest request) {
        emailNotificationService.sendUserEvent(request.email(), request.operation());

        return ResponseEntity.ok("Уведомление отправлено");
    }
}