package com.pulsegrid.queryapi.application.service;

import com.pulsegrid.queryapi.application.error.InvalidQueryArgumentException;
import com.pulsegrid.queryapi.application.model.AlertFilter;
import com.pulsegrid.queryapi.application.model.AlertPage;
import com.pulsegrid.queryapi.application.model.AlertRuleView;
import com.pulsegrid.queryapi.application.model.AlertView;
import com.pulsegrid.queryapi.application.port.in.AlertQueryUseCase;
import com.pulsegrid.queryapi.application.port.out.AlertingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AlertQueryService implements AlertQueryUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final AlertingPort alertingPort;

    @Override
    public AlertPage getAlertPage(AlertFilter filter, int first, int offset) {
        if (first < 1 || first > MAX_PAGE_SIZE) {
            throw new InvalidQueryArgumentException("'first' debe estar entre 1 y " + MAX_PAGE_SIZE);
        }
        if (offset < 0) {
            throw new InvalidQueryArgumentException("Cursor inválido");
        }
        List<AlertView> all = alertingPort.findAlerts(filter == null ? AlertFilter.none() : filter);
        int from = Math.min(offset, all.size());
        int to = Math.min(from + first, all.size());
        return new AlertPage(all.subList(from, to), from, to < all.size(), all.size());
    }

    @Override
    public List<AlertRuleView> listRules(Boolean active) {
        return alertingPort.findAllRules().stream()
                .filter(rule -> active == null || rule.active() == active)
                .toList();
    }

    @Override
    public Map<UUID, AlertRuleView> getRulesByIds(Collection<UUID> ruleIds) {
        return alertingPort.findAllRules().stream()
                .filter(rule -> ruleIds.contains(rule.id()))
                .collect(Collectors.toMap(AlertRuleView::id, Function.identity()));
    }

    @Override
    public Map<UUID, AlertView> getAlertsByIds(Collection<UUID> alertIds) {
        return alertingPort.findAlerts(AlertFilter.none()).stream()
                .filter(alert -> alertIds.contains(alert.id()))
                .collect(Collectors.toMap(AlertView::id, Function.identity()));
    }
}
