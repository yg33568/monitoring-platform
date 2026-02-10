package com.monitor.monitoring_platform.service;

import com.monitor.monitoring_platform.entity.SystemMetrics;
import com.monitor.monitoring_platform.entity.Diagnosis;
import com.monitor.monitoring_platform.entity.SmartAnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
//@Slf4j
public class SmartRootCauseService {

    private static final Logger log = LoggerFactory.getLogger(SmartRootCauseService.class);
    public SmartAnalysisResult analyze(SystemMetrics current, List<SystemMetrics> recentHistory) {
        List<Diagnosis> diagnoses = new ArrayList<>();

        log.info("开始智能根因分析，当前数据点：{}", recentHistory.size());

        // 1. 内存泄漏检测
        Diagnosis memoryLeak = detectMemoryLeak(current, recentHistory);
        if (memoryLeak.getConfidence() > 40) {
            diagnoses.add(memoryLeak);
        }

        // 2. CPU问题检测
        Diagnosis cpuIssue = detectCPUIssue(current, recentHistory);
        if (cpuIssue.getConfidence() > 40) {
            diagnoses.add(cpuIssue);
        }

        // 3. 磁盘问题检测
        Diagnosis diskIssue = detectDiskIssue(current, recentHistory);
        if (diskIssue.getConfidence() > 40) {
            diagnoses.add(diskIssue);
        }

        log.info("智能分析完成，发现 {} 个潜在问题", diagnoses.size());
        return new SmartAnalysisResult(diagnoses, new Date());
    }

    private Diagnosis detectMemoryLeak(SystemMetrics current, List<SystemMetrics> history) {
        if (history.size() < 3) {
            return new Diagnosis("内存分析", 0, "历史数据不足");
        }

        // 计算内存增长趋势
        double growthRate = calculateMemoryGrowthRate(history);
        double avgMemory = calculateAverageMemory(history);

        int confidence = 0;
        String evidence = "";

        if (growthRate > 1.5 && avgMemory > 80) {
            confidence = 85;
            evidence = String.format("内存快速增长(%.1f%%)，平均使用率%.1f%%", growthRate, avgMemory);
        } else if (growthRate > 0.8 && avgMemory > 70) {
            confidence = 65;
            evidence = String.format("内存稳定增长(%.1f%%)，使用率偏高", growthRate);
        } else if (current.getMemUsage() > 90) {
            confidence = 75;
            evidence = "内存使用率超过90%";
        } else {
            confidence = 0;
            evidence = "内存使用正常";
        }

        return new Diagnosis("内存泄漏风险", confidence, evidence);
    }

    private Diagnosis detectCPUIssue(SystemMetrics current, List<SystemMetrics> history) {
        double avgCpu = calculateAverageCPU(history);
        double cpuVolatility = calculateCPUVolatility(history);

        int confidence = 0;
        String evidence = "";

        if (current.getCpuUsage() > 95) {
            confidence = 90;
            evidence = "CPU使用率超过95%";
        } else if (avgCpu > 85 && cpuVolatility < 10) {
            confidence = 75;
            evidence = String.format("CPU持续高负载(平均%.1f%%)，波动小", avgCpu);
        } else if (avgCpu > 80) {
            confidence = 60;
            evidence = String.format("CPU负载偏高(平均%.1f%%)", avgCpu);
        } else {
            confidence = 0;
            evidence = "CPU使用正常";
        }

        return new Diagnosis("CPU性能问题", confidence, evidence);
    }

    private Diagnosis detectDiskIssue(SystemMetrics current, List<SystemMetrics> history) {
        double avgDisk = calculateAverageDisk(history);

        int confidence = 0;
        String evidence = "";

        if (current.getDiskUsage() > 95) {
            confidence = 95;
            evidence = "磁盘使用率超过95%";
        } else if (avgDisk > 90) {
            confidence = 80;
            evidence = String.format("磁盘持续高使用率(平均%.1f%%)", avgDisk);
        } else if (current.getDiskUsage() > 85) {
            confidence = 65;
            evidence = "磁盘使用率偏高";
        } else {
            confidence = 0;
            evidence = "磁盘使用正常";
        }

        return new Diagnosis("磁盘空间风险", confidence, evidence);
    }

    // 计算工具方法
    private double calculateMemoryGrowthRate(List<SystemMetrics> history) {
        if (history.size() < 2) return 0;

        double first = history.get(0).getMemUsage();
        double last = history.get(history.size() - 1).getMemUsage();

        return last - first; // 增长百分比
    }

    private double calculateAverageMemory(List<SystemMetrics> history) {
        return history.stream()
                .mapToDouble(SystemMetrics::getMemUsage)
                .average()
                .orElse(0);
    }

    private double calculateAverageCPU(List<SystemMetrics> history) {
        return history.stream()
                .mapToDouble(SystemMetrics::getCpuUsage)
                .average()
                .orElse(0);
    }

    private double calculateCPUVolatility(List<SystemMetrics> history) {
        double avg = calculateAverageCPU(history);
        double variance = history.stream()
                .mapToDouble(m -> Math.pow(m.getCpuUsage() - avg, 2))
                .average()
                .orElse(0);
        return Math.sqrt(variance);
    }

    private double calculateAverageDisk(List<SystemMetrics> history) {
        return history.stream()
                .mapToDouble(SystemMetrics::getDiskUsage)
                .average()
                .orElse(0);
    }
}