package com.monitor.monitoring_platform.service;

import com.monitor.monitoring_platform.entity.SystemMetrics;
import com.monitor.monitoring_platform.entity.Diagnosis;
import com.monitor.monitoring_platform.entity.SmartAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SmartRootCauseService {

    //创建日志对象，用来打印日志
    private static final Logger log = LoggerFactory.getLogger(SmartRootCauseService.class);

    @Autowired
    private AiSmartService aiSmartService;  // 注入 AI 服务

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

        // ==========  4. 调用 AI 进行深度根因分析 ==========
        String aiRootCauseAnalysis = callAiForRootCauseAnalysis(current, recentHistory, diagnoses);

        log.info("智能分析完成，发现 {} 个潜在问题", diagnoses.size());

        SmartAnalysisResult result = new SmartAnalysisResult(diagnoses, new Date());
        result.setAiAnalysis(aiRootCauseAnalysis);  // 设置 AI 分析结果

        return result;
    }

    /**
     * 调用 AI 进行深度根因分析
     */
    private String callAiForRootCauseAnalysis(SystemMetrics current, List<SystemMetrics> history, List<Diagnosis> diagnoses) {
        try {
            // 1. 计算最近数据的趋势
            double avgCpu = calculateAverageCPU(history);
            double avgMem = calculateAverageMemory(history);
            double avgDisk = calculateAverageDisk(history);

            // 2. 计算增长率
            double cpuGrowthRate = calculateCPUGrowthRate(history);
            double memGrowthRate = calculateMemoryGrowthRate(history);

            // 3. 获取当前值
            double currentCpu = current.getCpuUsage() != null ? current.getCpuUsage() : 0;
            double currentMem = current.getMemUsage() != null ? current.getMemUsage() : 0;
            double currentDisk = current.getDiskUsage() != null ? current.getDiskUsage() : 0;

            // 4. 构建诊断摘要
            StringBuilder diagnosisSummary = new StringBuilder();
            for (Diagnosis d : diagnoses) {
                diagnosisSummary.append(String.format("- %s (置信度: %d%%, 依据: %s)\n",
                        d.getType(), d.getConfidence(), d.getEvidence()));
            }

            // 5. 构建 AI 提示词
            String prompt = String.format(
                    "你是系统运维专家。请基于以下监控数据给出根因分析和优化建议：\n\n" +
                            "【当前指标】\n" +
                            "- CPU使用率: %.1f%%\n" +
                            "- 内存使用率: %.1f%%\n" +
                            "- 磁盘使用: %.0fGB\n\n" +
                            "【历史趋势（最近%d个数据点）】\n" +
                            "- 平均CPU: %.1f%%, CPU增长趋势: %+.1f%%\n" +
                            "- 平均内存: %.1f%%, 内存增长趋势: %+.1f%%\n" +
                            "- 平均磁盘: %.1fGB\n\n" +
                            "【传统算法诊断】\n%s\n\n" +
                            "请按以下格式回复（简洁、专业）：\n" +
                            "1. 根因定位：[最可能的问题根源]\n" +
                            "2. 置信度：[高/中/低]\n" +
                            "3. 优化建议：[具体可操作的建议，2-3条]\n" +
                            "4. 紧急程度：[紧急/一般/可观察]",
                    currentCpu, currentMem, currentDisk,
                    history.size(), avgCpu, cpuGrowthRate, avgMem, memGrowthRate, avgDisk,
                    diagnosisSummary.toString()
            );

            // 6. 调用 AI 服务
            String aiResponse = aiSmartService.askAi(prompt);
            log.info("AI 根因分析完成，响应长度: {}", aiResponse.length());

            return aiResponse;

        } catch (Exception e) {
            log.error("AI 根因分析失败", e);
            return "【AI分析暂时不可用】请检查系统日志或联系运维人员。";
        }
    }

    // 计算 CPU 增长率
    private double calculateCPUGrowthRate(List<SystemMetrics> history) {
        if (history.size() < 2) return 0;
        double first = history.get(0).getCpuUsage();
        double last = history.get(history.size() - 1).getCpuUsage();
        return last - first;
    }

    private double calculateMemoryGrowthRate(List<SystemMetrics> history) {
        if (history.size() < 2) return 0;
        double first = history.get(0).getMemUsage();
        double last = history.get(history.size() - 1).getMemUsage();
        return last - first;
    }

    private double calculateAverageMemory(List<SystemMetrics> history) {
        return history.stream().mapToDouble(m -> m.getMemUsage() != null ? m.getMemUsage() : 0).average().orElse(0);
    }

    private double calculateAverageCPU(List<SystemMetrics> history) {
        return history.stream().mapToDouble(m -> m.getCpuUsage() != null ? m.getCpuUsage() : 0).average().orElse(0);
    }

    private double calculateAverageDisk(List<SystemMetrics> history) {
        return history.stream().mapToDouble(m -> m.getDiskUsage() != null ? m.getDiskUsage() : 0).average().orElse(0);
    }

    private Diagnosis detectMemoryLeak(SystemMetrics current, List<SystemMetrics> history) {
        if (history.size() < 3) {
            return new Diagnosis("内存分析", 0, "历史数据不足");
        }
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
        } else if (current.getMemUsage() != null && current.getMemUsage() > 90) {
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
        if (current.getCpuUsage() != null && current.getCpuUsage() > 95) {
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
        if (current.getDiskUsage() != null && current.getDiskUsage() > 95) {
            confidence = 95;
            evidence = "磁盘使用率超过95%";
        } else if (avgDisk > 90) {
            confidence = 80;
            evidence = String.format("磁盘持续高使用率(平均%.1f%%)", avgDisk);
        } else if (current.getDiskUsage() != null && current.getDiskUsage() > 85) {
            confidence = 65;
            evidence = "磁盘使用率偏高";
        } else {
            confidence = 0;
            evidence = "磁盘使用正常";
        }
        return new Diagnosis("磁盘空间风险", confidence, evidence);
    }

    private double calculateCPUVolatility(List<SystemMetrics> history) {
        double avg = calculateAverageCPU(history);
        double variance = history.stream()
                .mapToDouble(m -> Math.pow((m.getCpuUsage() != null ? m.getCpuUsage() : 0) - avg, 2))
                .average()
                .orElse(0);
        return Math.sqrt(variance);
    }
}