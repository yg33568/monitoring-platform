package com.monitor.monitoring_platform.service;

import com.monitor.monitoring_platform.entity.DiskInfo;
import com.monitor.monitoring_platform.entity.SystemMetrics;
import com.monitor.monitoring_platform.mapper.SystemMetricsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MetricService {

    @Autowired
    private SystemMetricsMapper systemMetricsMapper;

    @Autowired
    private RealSystemDataService realSystemDataService;

    @Autowired
    private SmartAlertService smartAlertService;

    /**
     * 保存指标并检测告警
     */
    public String saveMetrics(SystemMetrics metricsData) {
        metricsData.setTimestamp(LocalDateTime.now());
        systemMetricsMapper.insert(metricsData);

        SmartAlertService.AlertResult alertResult = smartAlertService.checkWithSmartAlert(
                metricsData.getComponentName(),
                metricsData.getCpuUsage(),
                metricsData.getMemUsage(),
                metricsData.getDiskUsage(),
                metricsData.getNetworkRate(),
                metricsData.getProcessCount(),
                metricsData.getResponseTimeMs()
        );

        if (alertResult.isNeedAlert()) {
            return "数据保存成功！但检测到异常：" + alertResult.getMessage() + " | 处理建议：" + alertResult.getSuggestions();
        } else {
            return "数据保存成功！当前组件状态正常。";
        }
    }

    /**
     * 获取最新指标
     */
    public List<SystemMetrics> getLatestMetrics() {
        List<SystemMetrics> latestMetrics = systemMetricsMapper.selectLatestMetricsForAllComponents();

        if (latestMetrics == null || latestMetrics.isEmpty()) {
            latestMetrics = generateRealComputerMetrics();
            for (SystemMetrics metric : latestMetrics) {
                systemMetricsMapper.insert(metric);
            }
        }

        return latestMetrics;
    }

    /**
     * 生成真实的电脑指标数据
     */
    public List<SystemMetrics> generateRealComputerMetrics() {
        List<SystemMetrics> metrics = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        try {
            SystemMetrics cpuMetric = new SystemMetrics();
            cpuMetric.setComponentName("CPU");
            cpuMetric.setCpuUsage(realSystemDataService.getRealCpuUsage());
            cpuMetric.setTimestamp(now);
            metrics.add(cpuMetric);

            SystemMetrics memoryMetric = new SystemMetrics();
            memoryMetric.setComponentName("Memory");
            memoryMetric.setMemUsage(realSystemDataService.getRealMemoryUsage());
            memoryMetric.setTimestamp(now);
            metrics.add(memoryMetric);

            SystemMetrics networkMetric = new SystemMetrics();
            networkMetric.setComponentName("Network");
            networkMetric.setNetworkRate(realSystemDataService.getRealNetworkRate());
            networkMetric.setTimestamp(now);
            metrics.add(networkMetric);

            SystemMetrics processMetric = new SystemMetrics();
            processMetric.setComponentName("Processes");
            processMetric.setProcessCount(realSystemDataService.getRealProcessCount());
            processMetric.setTimestamp(now);
            metrics.add(processMetric);

            List<DiskInfo> disks = realSystemDataService.getAllDiskUsage();
            for (DiskInfo disk : disks) {
                SystemMetrics diskMetric = new SystemMetrics();
                diskMetric.setComponentName("Disk-" + disk.getMountPoint().replace(":", "").replace("/", ""));
                diskMetric.setDiskUsage(disk.getUsedSpace());
                diskMetric.setTimestamp(now);
                metrics.add(diskMetric);
            }

        } catch (Exception e) {
            System.err.println("生成真实数据失败，使用备用数据: " + e.getMessage());
            return generateFallbackMetrics();
        }

        return metrics;
    }

    /**
     * 备用数据
     */
    public List<SystemMetrics> generateFallbackMetrics() {
        List<SystemMetrics> metrics = new ArrayList<>();
        String[] components = {"CPU", "Memory", "Network", "Processes"};
        Random random = new Random();
        LocalDateTime now = LocalDateTime.now();

        for (String component : components) {
            SystemMetrics metric = new SystemMetrics();
            metric.setComponentName(component);
            metric.setTimestamp(now);

            switch (component) {
                case "CPU":
                    metric.setCpuUsage(15 + random.nextDouble() * 50);
                    break;
                case "Memory":
                    metric.setMemUsage(45 + random.nextDouble() * 40);
                    break;
                case "Network":
                    metric.setNetworkRate(random.nextDouble() * 8);
                    break;
                case "Processes":
                    metric.setProcessCount(160 + random.nextInt(90));
                    break;
            }
            metrics.add(metric);
        }

        SystemMetrics diskCMetric = new SystemMetrics();
        diskCMetric.setComponentName("Disk-C");
        diskCMetric.setDiskUsage(125L);
        diskCMetric.setTimestamp(now);
        metrics.add(diskCMetric);

        return metrics;
    }

    /**
     * 获取组件状态
     */
    public Map<String, Object> getComponentStatus() {
        Map<String, Object> components = new HashMap<>();

        double cpuUsage = realSystemDataService.getRealCpuUsage();
        double memoryUsage = realSystemDataService.getRealMemoryUsage();
        List<DiskInfo> disks = realSystemDataService.getAllDiskUsage();
        double networkRate = realSystemDataService.getRealNetworkRate();
        int processCount = realSystemDataService.getRealProcessCount();

        Map<String, Object> cpuMetrics = new HashMap<>();
        cpuMetrics.put("cpuUsage", cpuUsage);
        components.put("CPU", cpuMetrics);

        Map<String, Object> memoryMetrics = new HashMap<>();
        memoryMetrics.put("memUsage", memoryUsage);
        components.put("Memory", memoryMetrics);

        components.put("Disks", disks);

        Map<String, Object> networkMetrics = new HashMap<>();
        networkMetrics.put("networkRate", networkRate);
        components.put("Network", networkMetrics);

        Map<String, Object> processMetrics = new HashMap<>();
        processMetrics.put("processCount", processCount);
        components.put("Processes", processMetrics);
        System.out.println("组件监控API被调用，返回 " + disks.size() + " 个磁盘");
        return components;
    }
}