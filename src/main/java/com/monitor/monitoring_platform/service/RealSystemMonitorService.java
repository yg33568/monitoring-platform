package com.monitor.monitoring_platform.service;

import com.monitor.monitoring_platform.entity.DiskInfo;
import com.monitor.monitoring_platform.entity.SystemMetrics;
import com.monitor.monitoring_platform.mapper.SystemMetricsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class RealSystemMonitorService {

    @Autowired
    private RealSystemDataService realSystemDataService;

    @Autowired
    private SystemMetricsMapper systemMetricsMapper;

    private final Random random = new Random();

    /**
     * 获取真实的系统指标
     */
    public Map<String, Object> getRealSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // 调用真实的采集服务
        metrics.put("cpuUsage", realSystemDataService.getRealCpuUsage());
        metrics.put("memoryUsage", realSystemDataService.getRealMemoryUsage());
        metrics.put("diskUsage", getTotalDiskUsage());  // 总磁盘使用量
        metrics.put("networkRate", realSystemDataService.getRealNetworkRate());
        metrics.put("processCount", realSystemDataService.getRealProcessCount());
        metrics.put("timestamp", LocalDateTime.now());

        return metrics;
    }

    /**
     * 获取所有磁盘的总使用量
     */
    private long getTotalDiskUsage() {
        List<DiskInfo> disks = realSystemDataService.getAllDiskUsage();
        long totalUsed = 0;
        for (DiskInfo disk : disks) {
            totalUsed += disk.getUsedSpace();
        }
        return totalUsed;
    }

    /**
     * 获取仪表盘数据
     */
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();

        try {
            SystemMetrics cpuMetrics = systemMetricsMapper.selectLatestByComponentName("CPU");
            dashboardData.put("cpuUsage", cpuMetrics != null ? cpuMetrics.getCpuUsage() : 30.0);

            SystemMetrics memoryMetrics = systemMetricsMapper.selectLatestByComponentName("Memory");
            dashboardData.put("memoryUsage", memoryMetrics != null ? memoryMetrics.getMemUsage() : 50.0);

            SystemMetrics diskMetrics = systemMetricsMapper.selectLatestByComponentName("Disk-C");
            dashboardData.put("diskUsage", diskMetrics != null ? diskMetrics.getDiskUsage() : 150L);

            SystemMetrics networkMetrics = systemMetricsMapper.selectLatestByComponentName("Network");
            dashboardData.put("networkRate", networkMetrics != null ? networkMetrics.getNetworkRate() : 1.5);

            SystemMetrics processMetrics = systemMetricsMapper.selectLatestByComponentName("Processes");
            dashboardData.put("processCount", processMetrics != null ? processMetrics.getProcessCount() : 150);

            dashboardData.put("status", "success");
            dashboardData.put("timestamp", LocalDateTime.now());

        } catch (Exception e) {
            e.printStackTrace();
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

    /**
     * 获取系统健康状态
     */
    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
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