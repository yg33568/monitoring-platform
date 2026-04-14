package com.monitor.monitoring_platform.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class SmartAlertService {

    @Resource
    private AiSmartService aiSmartService;

    // 存储各服务的动态基线（简化版：手动设置初始值）
    private Map<String, ServiceBaseline> serviceBaselines = new HashMap<>();

    public SmartAlertService() {
        // 初始化一些示例服务的基线数据
        initializeBaselines();
    }

    private void initializeBaselines() {
        // 用户服务的初始基线
        ServiceBaseline userServiceBaseline = new ServiceBaseline();
        userServiceBaseline.setAvgCpu(45.0);
        userServiceBaseline.setStdCpu(15.0);
        userServiceBaseline.setAvgMem(65.0);
        userServiceBaseline.setStdMem(10.0);
        userServiceBaseline.setAvgResponse(120.0);
        userServiceBaseline.setStdResponse(30.0);
        serviceBaselines.put("用户服务", userServiceBaseline);

        // 订单服务的初始基线
        ServiceBaseline orderServiceBaseline = new ServiceBaseline();
        orderServiceBaseline.setAvgCpu(50.0);
        orderServiceBaseline.setStdCpu(20.0);
        orderServiceBaseline.setAvgMem(70.0);
        orderServiceBaseline.setStdMem(15.0);
        orderServiceBaseline.setAvgResponse(150.0);
        orderServiceBaseline.setStdResponse(50.0);
        serviceBaselines.put("订单服务", orderServiceBaseline);
    }

    /**
     * 智能检查指标：基于动态基线，而非固定阈值
     */
    public AlertResult checkWithSmartAlert(String serviceName, Double cpuUsage, Double memUsage, Integer responseTimeMs) {
        AlertResult result = new AlertResult();

        // 获取或初始化该服务的基线数据
        ServiceBaseline baseline = serviceBaselines.get(serviceName);
        if (baseline == null) {
            // 如果服务不存在，创建新的基线
            baseline = new ServiceBaseline();
            baseline.setAvgCpu(50.0); // 默认值
            baseline.setStdCpu(20.0);
            baseline.setAvgMem(60.0);
            baseline.setStdMem(15.0);
            baseline.setAvgResponse(100.0);
            baseline.setStdResponse(25.0);
            serviceBaselines.put(serviceName, baseline);
        }

        // 智能判断（核心逻辑）
        boolean isCpuAlert = checkMetricAlert(cpuUsage, baseline.getAvgCpu(), baseline.getStdCpu(), "CPU");
        boolean isMemAlert = checkMetricAlert(memUsage, baseline.getAvgMem(), baseline.getStdMem(), "内存");
        boolean isResponseAlert = checkMetricAlert(responseTimeMs.doubleValue(), baseline.getAvgResponse(), baseline.getStdResponse(), "响应时间");

        // 构建返回结果
        if (isCpuAlert || isMemAlert || isResponseAlert) {
            result.setNeedAlert(true);
            result.setAlertLevel("WARNING");
            result.setMessage(generateAlertMessage(serviceName, isCpuAlert, isMemAlert, isResponseAlert));

            // ======================= 【AI 核心代码】 =======================
            String prompt = String.format(
                    "你是系统运维专家。服务名：%s，CPU使用率：%.1f%%，内存使用率：%.1f%%，响应时间：%dms。请给出简洁的优化建议。",
                    serviceName, cpuUsage, memUsage, responseTimeMs
            );
            String aiSuggestion = aiSmartService.askAi(prompt);
            result.setSuggestions(aiSuggestion);
            // ===============================================================

            System.out.println("🚨 智能告警触发: " + result.getMessage());
            System.out.println("🤖 AI 建议: " + aiSuggestion);
        } else {
            result.setNeedAlert(false);
            result.setAlertLevel("NORMAL");
            result.setMessage("服务运行正常");
            result.setSuggestions("无");
        }

        return result;
    }

    /**
     * 核心算法：基于动态阈值检测
     * 如果当前值 > 平均值 + 1.5倍标准差，则认为异常
     */
    private boolean checkMetricAlert(Double currentValue, Double avg, Double std, String metricName) {
        if (avg == null || std == null) return false;

        double threshold = avg + 1.5 * std;
        boolean isAlert = currentValue > threshold;

        if (isAlert) {
            System.out.printf("🔔 %s异常检测：当前值%.2f > 阈值%.2f (avg=%.2f, std=%.2f)%n",
                    metricName, currentValue, threshold, avg, std);
        }

        return isAlert;
    }

    /**
     * 生成告警消息
     */
    private String generateAlertMessage(String serviceName, boolean cpuAlert, boolean memAlert, boolean responseAlert) {
        StringBuilder message = new StringBuilder();
        message.append("服务【").append(serviceName).append("】检测到异常：");

        if (cpuAlert) message.append(" CPU使用率超出正常范围");
        if (memAlert) message.append(" 内存使用率超出正常范围");
        if (responseAlert) message.append(" 响应时间超出正常范围");

        return message.toString();
    }

    /**
     * 内部类：服务基线数据
     */
    public static class ServiceBaseline {
        private Double avgCpu;      // CPU平均使用率
        private Double stdCpu;      // CPU标准差
        private Double avgMem;      // 内存平均使用率
        private Double stdMem;      // 内存标准差
        private Double avgResponse; // 平均响应时间
        private Double stdResponse; // 响应时间标准差

        public Double getAvgCpu() { return avgCpu; }
        public void setAvgCpu(Double avgCpu) { this.avgCpu = avgCpu; }
        public Double getStdCpu() { return stdCpu; }
        public void setStdCpu(Double stdCpu) { this.stdCpu = stdCpu; }
        public Double getAvgMem() { return avgMem; }
        public void setAvgMem(Double avgMem) { this.avgMem = avgMem; }
        public Double getStdMem() { return stdMem; }
        public void setStdMem(Double stdMem) { this.stdMem = stdMem; }
        public Double getAvgResponse() { return avgResponse; }
        public void setAvgResponse(Double avgResponse) { this.avgResponse = avgResponse; }
        public Double getStdResponse() { return stdResponse; }
        public void setStdResponse(Double stdResponse) { this.stdResponse = stdResponse; }
    }

    /**
     * 告警结果封装类
     */
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