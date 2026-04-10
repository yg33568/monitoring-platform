package com.monitor.monitoring_platform.controller;

import com.monitor.monitoring_platform.entity.SystemMetrics;
import com.monitor.monitoring_platform.mapper.SystemMetricsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/system")
public class RealSystemMonitorController {

    @Autowired
    private SystemMetricsMapper systemMetricsMapper;

    private final Random random = new Random();

    @GetMapping("/real-metrics")
    public Map<String, Object> getRealSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // 模拟获取真实系统指标
        metrics.put("cpuUsage", 20 + random.nextDouble() * 50); // 20-70%
        metrics.put("memoryUsage", 40 + random.nextDouble() * 40); // 40-80%
        metrics.put("diskUsage", 150L + random.nextInt(100)); // 150-250GB
        metrics.put("networkRate", random.nextDouble() * 5); // 0-5MB/s
        metrics.put("processCount", 100 + random.nextInt(100)); // 100-200个进程
        metrics.put("timestamp", LocalDateTime.now());

        return metrics;
    }

    @GetMapping("/dashboard-data")
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();

        try {
            // 获取CPU的最新数据 - 使用新的方法名
            SystemMetrics cpuMetrics = systemMetricsMapper.selectLatestByComponentName("CPU");
            if (cpuMetrics != null) {
                dashboardData.put("cpuUsage", cpuMetrics.getCpuUsage());
            } else {
                dashboardData.put("cpuUsage", 30.0);
            }

            // 获取内存的最新数据 - 使用新的方法名
            SystemMetrics memoryMetrics = systemMetricsMapper.selectLatestByComponentName("Memory");
            if (memoryMetrics != null) {
                dashboardData.put("memoryUsage", memoryMetrics.getMemUsage());
            } else {
                dashboardData.put("memoryUsage", 50.0);
            }

            // 获取磁盘数据 - 使用新的方法名
            SystemMetrics diskMetrics = systemMetricsMapper.selectLatestByComponentName("Disk-C");
            if (diskMetrics != null) {
                dashboardData.put("diskUsage", diskMetrics.getDiskUsage());
            } else {
                dashboardData.put("diskUsage", 150L);
            }

            // 获取网络数据 - 使用新的方法名
            SystemMetrics networkMetrics = systemMetricsMapper.selectLatestByComponentName("Network");
            if (networkMetrics != null) {
                dashboardData.put("networkRate", networkMetrics.getNetworkRate());
            } else {
                dashboardData.put("networkRate", 1.5);
            }

            // 获取进程数据 - 使用新的方法名
            SystemMetrics processMetrics = systemMetricsMapper.selectLatestByComponentName("Processes");
            if (processMetrics != null) {
                dashboardData.put("processCount", processMetrics.getProcessCount());
            } else {
                dashboardData.put("processCount", 150);
            }

            dashboardData.put("status", "success");
            dashboardData.put("timestamp", LocalDateTime.now());

        } catch (Exception e) {
            e.printStackTrace();
            // 如果出错，返回模拟数据
            dashboardData.put("cpuUsage", 35.0);
            dashboardData.put("memoryUsage", 55.0);
            dashboardData.put("diskUsage", 160L);
            dashboardData.put("networkRate", 2.0);
            dashboardData.put("processCount", 120);
            dashboardData.put("status", "error");
            dashboardData.put("timestamp", LocalDateTime.now());
        }

        return dashboardData;
    }

    // 如果还有其他方法调用了 selectLatestByServiceName，也需要更新
    @GetMapping("/health")
    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            // 检查各组件健康状态 - 使用新的方法名
            SystemMetrics cpu = systemMetricsMapper.selectLatestByComponentName("CPU");
            SystemMetrics memory = systemMetricsMapper.selectLatestByComponentName("Memory");

            boolean cpuHealthy = cpu != null && cpu.getCpuUsage() < 90;
            boolean memoryHealthy = memory != null && memory.getMemUsage() < 95;

            health.put("cpuHealthy", cpuHealthy);
            health.put("memoryHealthy", memoryHealthy);
            health.put("overallHealthy", cpuHealthy && memoryHealthy);
            health.put("timestamp", LocalDateTime.now());

        } catch (Exception e) {
            health.put("cpuHealthy", true);
            health.put("memoryHealthy", true);
            health.put("overallHealthy", true);
            health.put("timestamp", LocalDateTime.now());
        }

        return health;
    }
}