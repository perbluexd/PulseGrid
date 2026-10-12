package com.pulsegrid.notificationworkers.api.controller;

import com.pulsegrid.notificationworkers.api.dto.SentNotificationResponse;
import com.pulsegrid.notificationworkers.api.dto.SentNotificationsResponse;
import com.pulsegrid.notificationworkers.application.port.in.GetSentNotificationsUseCase;
import com.pulsegrid.notificationworkers.application.port.out.SentNotification;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/internal/notifications")
@RequiredArgsConstructor
public class InternalNotificationController {

    private final GetSentNotificationsUseCase getSentNotificationsUseCase;

    @GetMapping
    public ResponseEntity<SentNotificationsResponse> byAlertIds(@RequestParam @NotEmpty @Size(max = 500) List<UUID> alertIds) {
        return ResponseEntity.ok(toResponse(getSentNotificationsUseCase.getByAlertIds(alertIds)));
    }

    @GetMapping("/recent")
    public ResponseEntity<SentNotificationsResponse> recent(@RequestParam(defaultValue = "50") @Min(1) @Max(200) int limit) {
        return ResponseEntity.ok(toResponse(getSentNotificationsUseCase.getMostRecent(limit)));
    }

    private SentNotificationsResponse toResponse(List<SentNotification> notifications) {
        return new SentNotificationsResponse(notifications.stream()
                .map(n -> new SentNotificationResponse(n.alertId(), n.notifiedAt()))
                .toList());
    }
}
