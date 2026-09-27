package com.rikkeibank.notification.controller;

import com.rikkeibank.notification.entity.Notification;
import com.rikkeibank.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Notification> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    @GetMapping(value = "/my-notifications", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Notification> getMyNotifications(
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestParam(value = "email", required = false) String paramEmail) {
        String email = userEmail != null && !userEmail.isBlank() ? userEmail : paramEmail;
        return notificationService.getNotificationsForUser(email != null ? email : "customer1@rikkeibank.com");
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Notification> streamNotifications(
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestParam(value = "email", required = false) String paramEmail) {
        String email = userEmail != null && !userEmail.isBlank() ? userEmail : paramEmail;
        return notificationService.getNotificationsForUser(email != null ? email : "customer1@rikkeibank.com");
    }
}
