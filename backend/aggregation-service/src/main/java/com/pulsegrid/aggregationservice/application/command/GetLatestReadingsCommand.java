package com.pulsegrid.aggregationservice.application.command;

import java.util.List;
import java.util.UUID;

public record GetLatestReadingsCommand(List<UUID> deviceIds) {
}
