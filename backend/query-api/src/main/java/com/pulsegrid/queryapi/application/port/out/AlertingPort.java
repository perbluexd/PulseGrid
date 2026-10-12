package com.pulsegrid.queryapi.application.port.out;

import com.pulsegrid.queryapi.application.model.AlertFilter;
import com.pulsegrid.queryapi.application.model.AlertRuleView;
import com.pulsegrid.queryapi.application.model.AlertView;

import java.util.List;

public interface AlertingPort {
    List<AlertView> findAlerts(AlertFilter filter);

    List<AlertRuleView> findAllRules();
}
