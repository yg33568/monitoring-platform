package com.monitor.monitoring_platform.controller;

import com.monitor.monitoring_platform.entity.DiskInfo;
import com.monitor.monitoring_platform.entity.DiskSpaceAnalysis;
import com.monitor.monitoring_platform.entity.SystemMetrics;
import com.monitor.monitoring_platform.mapper.SystemMetricsMapper;
import com.monitor.monitoring_platform.service.DiskSpaceAnalyzer;
import com.monitor.monitoring_platform.service.RealSystemDataService;
import com.monitor.monitoring_platform.service.SmartAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
public class MetricController {

    @Autowired
    private SystemMetricsMapper systemMetricsMapper;

    @Autowired
    private SmartAlertService smartAlertService;

    @Autowired  // 添加这个依赖注入
    private RealSystemDataService realSystemDataService;

    @PostMapping("/metrics")
    public String receiveMetrics(@RequestBody SystemMetrics metricsData) {
        try {
            // 1. 设置时间戳
            metricsData.setTimestamp(LocalDateTime.now());

            // 2. 先保存到数据库
            systemMetricsMapper.insert(metricsData);

            // 3. 进行智能预警检测
            SmartAlertService.AlertResult alertResult = smartAlertService.checkWithSmartAlert(
                    metricsData.getComponentName(),
                    metricsData.getCpuUsage(),
                    metricsData.getMemUsage(),
                    metricsData.getResponseTimeMs()
            );

            // 4. 根据检测结果返回不同消息
            if (alertResult.isNeedAlert()) {
                return "数据保存成功！但检测到异常：" + alertResult.getMessage() + " | 处理建议：" + alertResult.getSuggestions();
            } else {
                return "数据保存成功！当前组件状态正常。";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "数据保存失败: " + e.getMessage();
        }
    }

    @GetMapping("/metrics/latest")
    public ResponseEntity<List<SystemMetrics>> getLatestMetrics() {
        try {
            // 使用新的方法获取所有组件的最新指标
            List<SystemMetrics> latestMetrics = systemMetricsMapper.selectLatestMetricsForAllComponents();

            // 如果数据库没有数据，生成一些真实数据
            if (latestMetrics == null || latestMetrics.isEmpty()) {
                latestMetrics = generateRealComputerMetrics();
                // 保存真实数据到数据库
                for (SystemMetrics metric : latestMetrics) {
                    systemMetricsMapper.insert(metric);
                }
            }

            return ResponseEntity.ok(latestMetrics);
        } catch (Exception e) {
            e.printStackTrace();
            // 如果出错，返回真实数据确保前端能显示
            List<SystemMetrics> realData = generateRealComputerMetrics();
            return ResponseEntity.ok(realData);
        }
    }

    /**
     * 生成真实的电脑指标数据（替换原来的模拟数据）
     */
    private List<SystemMetrics> generateRealComputerMetrics() {
        List<SystemMetrics> metrics = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        try {
            // 获取真实的核心组件数据
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

            // 获取真实的磁盘数据
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
            // 如果获取真实数据失败，使用备用数据
            return generateFallbackMetrics();
        }

        return metrics;
    }

    /**
     * 备用数据（当真实数据获取失败时使用）
     */
    private List<SystemMetrics> generateFallbackMetrics() {
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

        // 添加基础的磁盘数据
        SystemMetrics diskCMetric = new SystemMetrics();
        diskCMetric.setComponentName("Disk-C");
        diskCMetric.setDiskUsage(125L);
        diskCMetric.setTimestamp(now);
        metrics.add(diskCMetric);

        return metrics;
    }

    @GetMapping("/disks")  // 移除重复的 /api 前缀
    public List<DiskInfo> getDiskInfo() {
        return realSystemDataService.getAllDiskUsage();
    }

    @GetMapping("/metrics/components")
    public Map<String, Object> getComponentStatus() {
        Map<String, Object> components = new HashMap<>();

        try {
            // 获取实时数据
            double cpuUsage = realSystemDataService.getRealCpuUsage();
            double memoryUsage = realSystemDataService.getRealMemoryUsage();
            List<DiskInfo> disks = realSystemDataService.getAllDiskUsage();
            double networkRate = realSystemDataService.getRealNetworkRate();
            int processCount = realSystemDataService.getRealProcessCount();

            // CPU数据
            Map<String, Object> cpuMetrics = new HashMap<>();
            cpuMetrics.put("cpuUsage", cpuUsage);
            components.put("CPU", cpuMetrics);

            // 内存数据
            Map<String, Object> memoryMetrics = new HashMap<>();
            memoryMetrics.put("memUsage", memoryUsage);
            components.put("Memory", memoryMetrics);

            // 磁盘数据
            components.put("Disks", disks);

            // 网络数据
            Map<String, Object> networkMetrics = new HashMap<>();
            networkMetrics.put("networkRate", networkRate);
            components.put("Network", networkMetrics);

            // 进程数据
            Map<String, Object> processMetrics = new HashMap<>();
            processMetrics.put("processCount", processCount);
            components.put("Processes", processMetrics);

            System.out.println("组件监控API被调用，返回 " + disks.size() + " 个磁盘");

        } catch (Exception e) {
            System.err.println("获取组件状态失败: " + e.getMessage());
            // 返回空数据而不是抛出异常
            components.put("CPU", Map.of("cpuUsage", 0.0));
            components.put("Memory", Map.of("memUsage", 0.0));
            components.put("Disks", new ArrayList<>());
            components.put("Network", Map.of("networkRate", 0.0));
            components.put("Processes", Map.of("processCount", 0));
        }

        return components;
    }
    @Autowired
    private DiskSpaceAnalyzer diskSpaceAnalyzer;

    @GetMapping("/disk-analysis/{mountPoint}")
    public DiskSpaceAnalysis getDiskAnalysis(@PathVariable String mountPoint) {
        System.out.println("=== 磁盘分析API被调用 ===");
        System.out.println("挂载点: " + mountPoint);

        try {
            DiskSpaceAnalysis analysis = diskSpaceAnalyzer.analyzeDiskSpace(mountPoint);
            System.out.println("分析成功: " + analysis.getMountPoint());
            return analysis;
        } catch (Exception e) {
            System.err.println("分析失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("磁盘分析失败: " + e.getMessage(), e);
        }
    }
}