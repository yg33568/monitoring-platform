package com.monitor.monitoring_platform.controller;

import com.monitor.monitoring_platform.entity.DiskInfo;
import com.monitor.monitoring_platform.entity.DiskSpaceAnalysis;
import com.monitor.monitoring_platform.entity.SystemMetrics;
import com.monitor.monitoring_platform.service.DiskSpaceAnalyzer;
import com.monitor.monitoring_platform.service.MetricService;
import com.monitor.monitoring_platform.service.RealSystemDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
public class MetricController {

    @Autowired
    private DiskSpaceAnalyzer diskSpaceAnalyzer;

    @Autowired
    private MetricService metricService;

    @Autowired
    private RealSystemDataService realSystemDataService;

    @PostMapping("/metrics")
    public String receiveMetrics(@RequestBody SystemMetrics metricsData) {
        try {
            return metricService.saveMetrics(metricsData);
        } catch (Exception e) {
            e.printStackTrace();
            return "数据保存失败: " + e.getMessage();
        }
    }

    @GetMapping("/metrics/latest")
    public ResponseEntity<List<SystemMetrics>> getLatestMetrics() {
        try {
            List<SystemMetrics> latestMetrics = metricService.getLatestMetrics();
            return ResponseEntity.ok(latestMetrics);
        } catch (Exception e) {
            e.printStackTrace();
            // 如果出错，返回真实数据确保前端能显示
            List<SystemMetrics> realData = metricService.generateRealComputerMetrics();
            return ResponseEntity.ok(realData);
        }
    }

    @GetMapping("/disks")
    public List<DiskInfo> getDiskInfo() {
        return realSystemDataService.getAllDiskUsage();
    }

    @GetMapping("/metrics/components")
    public Map<String, Object> getComponentStatus() {
            return metricService.getComponentStatus();
    }

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