package com.monitor.monitoring_platform.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class SmartAlertService {

    @Resource
    private AiSmartService aiSmartService;

    private Map<String, ServiceBaseline> componentBaselines = new HashMap<>();

    public SmartAlertService() {
        initializeBaselines();
    }

    private void initializeBaselines() {
        // CPU 基线
        ServiceBaseline cpuBaseline = new ServiceBaseline();
        cpuBaseline.setAvgCpu(45.0);
        cpuBaseline.setStdCpu(15.0);
        componentBaselines.put("CPU", cpuBaseline);

        // 内存 基线
        ServiceBaseline memoryBaseline = new ServiceBaseline();
        memoryBaseline.setAvgMem(65.0);
        memoryBaseline.setStdMem(10.0);
        componentBaselines.put("Memory", memoryBaseline);

        // 磁盘 基线（使用量 GB）
        ServiceBaseline diskBaseline = new ServiceBaseline();
        diskBaseline.setAvgDisk(120.0);   // 平均使用 120GB
        diskBaseline.setStdDisk(30.0);    // 标准差 30GB
        componentBaselines.put("Disk", diskBaseline);

        // 网络 基线（MB/s）
        ServiceBaseline networkBaseline = new ServiceBaseline();
        networkBaseline.setAvgNetwork(2.0);
        networkBaseline.setStdNetwork(1.0);
        componentBaselines.put("Network", networkBaseline);

        // 进程 基线（个数）
        ServiceBaseline processBaseline = new ServiceBaseline();
        processBaseline.setAvgProcess(150.0);
        processBaseline.setStdProcess(30.0);
        componentBaselines.put("Processes", processBaseline);
    }

    public AlertResult checkWithSmartAlert(
            String componentName,
            Double cpuUsage,
            Double memUsage,
            Long diskUsage,
            Double networkRate,
            Integer processCount,
            Integer responseTimeMs) {

        AlertResult result = new AlertResult();

        ServiceBaseline baseline = componentBaselines.get(componentName);
        if (baseline == null) {
            // 未知组件，创建默认基线
            baseline = new ServiceBaseline();
            componentBaselines.put(componentName, baseline);
        }

        boolean isCpuAlert = false;
        boolean isMemAlert = false;
        boolean isDiskAlert = false;
        boolean isNetworkAlert = false;
        boolean isProcessAlert = false;
        boolean isResponseAlert = false;

        // 根据不同组件类型检测
        if ("CPU".equals(componentName) && cpuUsage != null) {
            isCpuAlert = checkMetricAlert(cpuUsage, baseline.getAvgCpu(), baseline.getStdCpu(), "CPU");
        }
        if ("Memory".equals(componentName) && memUsage != null) {
            isMemAlert = checkMetricAlert(memUsage, baseline.getAvgMem(), baseline.getStdMem(), "内存");
        }
        if (componentName.startsWith("Disk") && diskUsage != null) {
            isDiskAlert = checkMetricAlert((double) diskUsage, baseline.getAvgDisk(), baseline.getStdDisk(), "磁盘");
        }
        if ("Network".equals(componentName) && networkRate != null) {
            isNetworkAlert = checkMetricAlert(networkRate, baseline.getAvgNetwork(), baseline.getStdNetwork(), "网络");
        }
        if ("Processes".equals(componentName) && processCount != null) {
            isProcessAlert = checkMetricAlert((double) processCount, baseline.getAvgProcess(), baseline.getStdProcess(), "进程");
        }
        if (responseTimeMs != null) {
            isResponseAlert = checkMetricAlert((double) responseTimeMs, baseline.getAvgResponse(), baseline.getStdResponse(), "响应时间");
        }

        if (isCpuAlert || isMemAlert || isDiskAlert || isNetworkAlert || isProcessAlert || isResponseAlert) {
            result.setNeedAlert(true);
            result.setAlertLevel("WARNING");
            result.setMessage(generateAlertMessage(componentName, isCpuAlert, isMemAlert, isDiskAlert, isNetworkAlert, isProcessAlert, isResponseAlert));

            // AI 建议
            String prompt = String.format(
                    "你是系统运维专家。组件：%s，CPU使用率：%.1f%%，内存使用率：%.1f%%，磁盘使用：%dGB，网络速率：%.1fMB/s，进程数：%d个，响应时间：%dms。请给出简洁的优化建议。",
                    componentName,
                    cpuUsage != null ? cpuUsage : 0,
                    memUsage != null ? memUsage : 0,
                    diskUsage != null ? diskUsage : 0,
                    networkRate != null ? networkRate : 0,
                    processCount != null ? processCount : 0,
                    responseTimeMs != null ? responseTimeMs : 0
            );
            String aiSuggestion = aiSmartService.askAi(prompt);
            result.setSuggestions(aiSuggestion);

            System.out.println("🚨 智能告警触发: " + result.getMessage());
            System.out.println("🤖 AI 建议: " + aiSuggestion);
        } else {
            result.setNeedAlert(false);
            result.setAlertLevel("NORMAL");
            result.setMessage("组件运行正常");
            result.setSuggestions("无");
        }

        return result;
    }

    private boolean checkMetricAlert(Double currentValue, Double avg, Double std, String metricName) {
        if (avg == null || std == null || currentValue == null) return false;
        double threshold = avg + 1.5 * std;
        boolean isAlert = currentValue > threshold;
        if (isAlert) {
            System.out.printf("🔔 %s异常：当前%.1f > 阈值%.1f (平均%.1f, 标准差%.1f)%n",
                    metricName, currentValue, threshold, avg, std);
        }
        return isAlert;
    }

    private String generateAlertMessage(String componentName, boolean cpuAlert, boolean memAlert, boolean diskAlert, boolean networkAlert, boolean processAlert, boolean responseAlert) {
        StringBuilder message = new StringBuilder();
        message.append("组件【").append(componentName).append("】检测到异常：");
        if (cpuAlert) message.append(" CPU使用率超出正常范围");
        if (memAlert) message.append(" 内存使用率超出正常范围");
        if (diskAlert) message.append(" 磁盘使用量超出正常范围");
        if (networkAlert) message.append(" 网络速率超出正常范围");
        if (processAlert) message.append(" 进程数量超出正常范围");
        if (responseAlert) message.append(" 响应时间超出正常范围");
        return message.toString();
    }

    // 内部类：组件基线数据
    public static class ServiceBaseline {
        private Double avgCpu; private Double stdCpu;
        private Double avgMem; private Double stdMem;
        private Double avgDisk; private Double stdDisk;
        private Double avgNetwork; private Double stdNetwork;
        private Double avgProcess; private Double stdProcess;
        private Double avgResponse; private Double stdResponse;  // ← 添加这行
        private Double avgDiskUsage; private Double stdDiskUsage;  // ← 添加这行（如果还需要）
        // getter/setter...
        public Double getAvgCpu() { return avgCpu; }
        public void setAvgCpu(Double avgCpu) { this.avgCpu = avgCpu; }
        public Double getStdCpu() { return stdCpu; }
        public void setStdCpu(Double stdCpu) { this.stdCpu = stdCpu; }
        public Double getAvgMem() { return avgMem; }
        public void setAvgMem(Double avgMem) { this.avgMem = avgMem; }
        public Double getStdMem() { return stdMem; }
        public void setStdMem(Double stdMem) { this.stdMem = stdMem; }
        public Double getAvgDisk() { return avgDisk; }
        public void setAvgDisk(Double avgDisk) { this.avgDisk = avgDisk; }
        public Double getStdDisk() { return stdDisk; }
        public void setStdDisk(Double stdDisk) { this.stdDisk = stdDisk; }
        public Double getAvgNetwork() { return avgNetwork; }
        public void setAvgNetwork(Double avgNetwork) { this.avgNetwork = avgNetwork; }
        public Double getStdNetwork() { return stdNetwork; }
        public void setStdNetwork(Double stdNetwork) { this.stdNetwork = stdNetwork; }
        public Double getAvgProcess() { return avgProcess; }
        public void setAvgProcess(Double avgProcess) { this.avgProcess = avgProcess; }
        public Double getStdProcess() { return stdProcess; }
        public void setStdProcess(Double stdProcess) { this.stdProcess = stdProcess; }
        public Double getAvgResponse() { return avgResponse; }      // ← 添加
        public void setAvgResponse(Double avgResponse) { this.avgResponse = avgResponse; }  // ← 添加
        public Double getStdResponse() { return stdResponse; }      // ← 添加
        public void setStdResponse(Double stdResponse) { this.stdResponse = stdResponse; }  // ← 添加
    }

    public static class AlertResult {
        private boolean needAlert;
        private String alertLevel;
        private String message;
        private String suggestions;
        public boolean isNeedAlert() { return needAlert; }
        public void setNeedAlert(boolean needAlert) { this.needAlert = needAlert; }
        public String getAlertLevel() { return alertLevel; }
        public void setAlertLevel(String alertLevel) { this.alertLevel = alertLevel; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getSuggestions() { return suggestions; }
        public void setSuggestions(String suggestions) { this.suggestions = suggestions; }
    }
}