package com.pulsegrid.alertingservice.domain.model;

import com.pulsegrid.alertingservice.domain.exception.InvalidAlertStatusTransitionException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlertTest {

    private final AlertRule rule = AlertRule.create(UUID.randomUUID(), null, "TEMPERATURE",
            AlertCondition.GREATER_THAN, 80, AlertSeverity.WARNING);

    @Test
    void shouldCopySeverityFromRuleWhenTriggered() {
        Alert alert = Alert.trigger(rule, rule.getDeviceId(), 91.2);

        assertThat(alert.getStatus()).isEqualTo(AlertStatus.TRIGGERED);
        assertThat(alert.getSeverity()).isEqualTo(AlertSeverity.WARNING);
        assertThat(alert.getAlertRuleId()).isEqualTo(rule.getId());
        assertThat(alert.isOpen()).isTrue();
    }

    @Test
    void shouldAcknowledgeThenResolve() {
        UUID userId = UUID.randomUUID();
        Alert alert = Alert.trigger(rule, rule.getDeviceId(), 91.2);

        alert.acknowledge(userId);
        alert.resolve(userId);

        assertThat(alert.getStatus()).isEqualTo(AlertStatus.RESOLVED);
        assertThat(alert.getAcknowledgedBy()).isEqualTo(userId);
        assertThat(alert.getResolvedBy()).isEqualTo(userId);
        assertThat(alert.getResolvedAt()).isNotNull();
        assertThat(alert.isOpen()).isFalse();
    }

    @Test
    void shouldRejectAcknowledgeWhenNotTriggered() {
        Alert alert = Alert.trigger(rule, rule.getDeviceId(), 91.2);
        alert.resolve(UUID.randomUUID());

        assertThatThrownBy(() -> alert.acknowledge(UUID.randomUUID()))
                .isInstanceOf(InvalidAlertStatusTransitionException.class);
    }

    @Test
    void shouldRejectResolveWhenAlreadyResolved() {
        Alert alert = Alert.trigger(rule, rule.getDeviceId(), 91.2);
        alert.resolve(UUID.randomUUID());

        assertThatThrownBy(() -> alert.resolve(UUID.randomUUID()))
                .isInstanceOf(InvalidAlertStatusTransitionException.class);
    }
}
