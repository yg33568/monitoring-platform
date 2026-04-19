package com.monitor.monitoring_platform.controller;

import com.monitor.monitoring_platform.service.RealSystemDataService;
import com.monitor.monitoring_platform.service.RealSystemMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class RealSystemMonitorController {

    @Autowired
    private RealSystemMonitorService realSystemMonitorService;
    @Autowired
    private RealSystemDataService realSystemDataService;

    @GetMapping("/real-metrics")
    public Map<String, Object> getRealSystemMetrics() {
        return realSystemMonitorService.getRealSystemMetrics();
    }

    @GetMapping("/dashboard-data")
    public Map<String, Object> getDashboardData() {
        return realSystemMonitorService.getDashboardData();
    }

    @GetMapping("/health")
    public Map<String, Object> getSystemHealth() {
        return realSystemMonitorService.getSystemHealth();
    }

    @GetMapping("/system-info")
    public String getSystemInfo() {
        return realSystemDataService.getSystemSummary();
    }
}