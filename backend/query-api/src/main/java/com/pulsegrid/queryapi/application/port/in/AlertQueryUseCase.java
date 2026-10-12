package com.pulsegrid.queryapi.application.port.in;

import com.pulsegrid.queryapi.application.model.AlertFilter;
import com.pulsegrid.queryapi.application.model.AlertPage;
import com.pulsegrid.queryapi.application.model.AlertRuleView;
import com.pulsegrid.queryapi.application.model.AlertView;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AlertQueryUseCase {
    AlertPage getAlertPage(AlertFilter filter, int first, int offset);

    List<AlertRuleView> listRules(Boolean active);

    Map<UUID, AlertRuleView> getRulesByIds(Collection<UUID> ruleIds);

    Map<UUID, AlertView> getAlertsByIds(Collection<UUID> alertIds);
}
