package com.academic.dashboard.batch.dto;

import com.academic.dashboard.dto.DashboardPerformanceDTO;

import java.util.List;

public record BatchImportReportDTO(
        String jobExecutionId,
        String status,
        int totalLidos,
        int totalImportados,
        int totalRejeitados,
        List<String> avisos,
        DashboardPerformanceDTO dashboard
) {
}

