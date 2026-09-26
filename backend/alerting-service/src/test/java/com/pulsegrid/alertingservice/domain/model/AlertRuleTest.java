package com.pulsegrid.alertingservice.domain.model;

import com.pulsegrid.alertingservice.domain.exception.InvalidAlertRuleTargetException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlertRuleTest {

    @Test
    void shouldRejectRuleWithDeviceAndGroup() {
        assertThatThrownBy(() -> AlertRule.create(UUID.randomUUID(), UUID.randomUUID(), "TEMPERATURE",
                AlertCondition.GREATER_THAN, 80, AlertSeverity.CRITICAL))
                .isInstanceOf(InvalidAlertRuleTargetException.class);
    }

    @Test
    void shouldRejectRuleWithoutTarget() {
        assertThatThrownBy(() -> AlertRule.create(null, null, "TEMPERATURE",
                AlertCondition.GREATER_THAN, 80, AlertSeverity.CRITICAL))
                .isInstanceOf(InvalidAlertRuleTargetException.class);
    }

    @Test
    void shouldBeTriggeredOnlyWhenActiveSameMetricAndConditionMet() {
        AlertRule rule = AlertRule.create(UUID.randomUUID(), null, "TEMPERATURE",
                AlertCondition.GREATER_THAN, 80, AlertSeverity.CRITICAL);

        assertThat(rule.isTriggeredBy("TEMPERATURE", 85)).isTrue();
        assertThat(rule.isTriggeredBy("TEMPERATURE", 80)).isFalse();
        assertThat(rule.isTriggeredBy("HUMIDITY", 85)).isFalse();

        rule.deactivate();

        assertThat(rule.isTriggeredBy("TEMPERATURE", 85)).isFalse();
    }

    @Test
    void shouldEvaluateEachCondition() {
        assertThat(AlertCondition.GREATER_THAN.isMetBy(10.1, 10)).isTrue();
        assertThat(AlertCondition.LESS_THAN.isMetBy(9.9, 10)).isTrue();
        assertThat(AlertCondition.LESS_THAN.isMetBy(10, 10)).isFalse();
        assertThat(AlertCondition.EQUALS.isMetBy(10, 10)).isTrue();
        assertThat(AlertCondition.EQUALS.isMetBy(10.0001, 10)).isFalse();
    }
}
