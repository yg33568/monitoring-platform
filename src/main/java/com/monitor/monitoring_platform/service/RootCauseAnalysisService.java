package com.monitor.monitoring_platform.service;

import com.monitor.monitoring_platform.entity.SystemMetrics;
import com.monitor.monitoring_platform.entity.DiskInfo;
import com.monitor.monitoring_platform.mapper.SystemMetricsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RootCauseAnalysisService {

    @Autowired
    private SystemMetricsMapper systemMetricsMapper;

    @Autowired
    private RealSystemDataService realSystemDataService;

    // 核心组件依赖拓扑
    private final Map<String, List<String>> coreComponentDependencies = Map.of(
            "CPU", List.of(),
            "Memory", List.of("CPU"),
            "Network", List.of("CPU", "Memory"),
            "Processes", List.of("CPU", "Memory")
    );

    // 核心组件权重
    private final Map<String, Double> coreComponentWeights = Map.of(
            "CPU", 0.35,
            "Memory", 0.3,
            "Network", 0.15,
            "Processes", 0.2
    );

    public Map<String, Object> analyzeRootCause(String affectedComponent) {
        Map<String, Object> result = new HashMap<>();
        result.put("affectedComponent", affectedComponent);
        result.put("analysisTime", LocalDateTime.now());

        try {
            // 1. 获取所有组件的最新状态（包括动态磁盘）
            Map<String, SystemMetrics> componentMetrics = getComponentMetrics();
            result.put("analyzedComponents", new ArrayList<>(componentMetrics.keySet()));

            // 2. 计算动态基线
            Map<String, Double> baselines = calculateDynamicBaselines(componentMetrics.keySet());

            // 3. 分析依赖链（动态构建包含磁盘的依赖关系）
            List<String> dependencyChain = findDependencyChain(affectedComponent, componentMetrics.keySet());
            result.put("dependencyChain", dependencyChain);

            // 4. 多维度根因分析
            RootCauseInfo rootCauseInfo = performMultiDimensionalAnalysis(
                    affectedComponent, componentMetrics, baselines, dependencyChain);

            result.put("rootCause", rootCauseInfo.getRootCause());
            result.put("confidence", rootCauseInfo.getConfidence());
            result.put("evidence", rootCauseInfo.getEvidence());
            result.put("suggestions", rootCauseInfo.getSuggestions());
            result.put("metricsAnalysis", rootCauseInfo.getMetricsAnalysis());

        } catch (Exception e) {
            // 分析失败时的降级方案
            handleAnalysisFailure(result, affectedComponent, e);
        }

        return result;
    }

    /**
     * 获取所有组件的指标（包括动态磁盘）
     */
    private Map<String, SystemMetrics> getComponentMetrics() {
        Map<String, SystemMetrics> metrics = new HashMap<>();

        // 核心固定组件
        String[] coreComponents = {"CPU", "Memory", "Network", "Processes"};
        for (String component : coreComponents) {
            SystemMetrics metric = systemMetricsMapper.selectLatestByComponentName(component);
            if (metric != null) {
                metrics.put(component, metric);
            }
        }

        // 动态磁盘组件
        try {
            List<DiskInfo> disks = realSystemDataService.getAllDiskUsage();
            for (DiskInfo disk : disks) {
                String diskComponentName = "Disk-" + disk.getMountPoint().replace(":", "").replace("/", "");
                SystemMetrics diskMetric = new SystemMetrics();
                diskMetric.setComponentName(diskComponentName);
                diskMetric.setDiskUsage(disk.getUsedSpace());
                diskMetric.setTimestamp(LocalDateTime.now());
                metrics.put(diskComponentName, diskMetric);
            }
        } catch (Exception e) {
            System.err.println("获取磁盘指标失败: " + e.getMessage());
        }

        return metrics;
    }

    /**
     * 计算动态基线（根据实际组件调整）
     */
    private Map<String, Double> calculateDynamicBaselines(Set<String> components) {
        Map<String, Double> baselines = new HashMap<>();

        // 核心组件基线
        baselines.put("CPU", 70.0);
        baselines.put("Memory", 75.0);
        baselines.put("Network", 50.0);
        baselines.put("Processes", 200.0);

        // 磁盘组件基线（基于磁盘总空间的百分比）
        for (String component : components) {
            if (component.startsWith("Disk-")) {
                baselines.put(component, 80.0); // 磁盘使用率基线80%
            }
        }

        return baselines;
    }

    /**
     * 查找依赖链（动态构建包含磁盘的依赖关系）
     */
    private List<String> findDependencyChain(String component, Set<String> availableComponents) {
        List<String> chain = new ArrayList<>();
        findDependenciesRecursive(component, chain, availableComponents);
        return chain;
    }

    private void findDependenciesRecursive(String component, List<String> chain, Set<String> availableComponents) {
        // 获取基础依赖
        List<String> dependencies = coreComponentDependencies.get(component);
        if (dependencies != null) {
            for (String dependency : dependencies) {
                if (availableComponents.contains(dependency) && !chain.contains(dependency)) {
                    chain.add(dependency);
                    findDependenciesRecursive(dependency, chain, availableComponents);
                }
            }
        }

        // 磁盘的特殊依赖逻辑：所有磁盘都依赖 Memory 和 CPU
        if (component.startsWith("Disk-")) {
            addDiskDependencies(chain, availableComponents);
        }

        // 进程依赖所有磁盘
        if (component.equals("Processes")) {
            addProcessDependencies(chain, availableComponents);
        }
    }

    private void addDiskDependencies(List<String> chain, Set<String> availableComponents) {
        if (availableComponents.contains("Memory") && !chain.contains("Memory")) {
            chain.add("Memory");
            findDependenciesRecursive("Memory", chain, availableComponents);
        }
        if (availableComponents.contains("CPU") && !chain.contains("CPU")) {
            chain.add("CPU");
            findDependenciesRecursive("CPU", chain, availableComponents);
        }
    }

    private void addProcessDependencies(List<String> chain, Set<String> availableComponents) {
        for (String comp : availableComponents) {
            if (comp.startsWith("Disk-") && !chain.contains(comp)) {
                chain.add(comp);
            }
        }
    }

    private RootCauseInfo performMultiDimensionalAnalysis(
            String affectedComponent,
            Map<String, SystemMetrics> metrics,
            Map<String, Double> baselines,
            List<String> dependencyChain) {

        RootCauseInfo info = new RootCauseInfo();
        List<String> evidenceList = new ArrayList<>();
        List<String> suggestionsList = new ArrayList<>();
        Map<String, Object> metricsAnalysis = new HashMap<>();

        double maxCorrelation = 0.0;
        String mostLikelyRootCause = affectedComponent;

        // 分析依赖链中的组件
        for (String component : dependencyChain) {
            SystemMetrics metric = metrics.get(component);
            if (metric != null) {
                double correlation = calculateCorrelation(component, metric, baselines.get(component));
                metricsAnalysis.put(component + "_correlation", correlation);

                if (correlation > maxCorrelation) {
                    maxCorrelation = correlation;
                    mostLikelyRootCause = component;
                }

                // 收集证据
                if (isComponentAbnormal(component, metric, baselines.get(component))) {
                    evidenceList.add(getAbnormalEvidence(component, metric));
                    suggestionsList.add(getComponentSuggestion(component));
                }
            }
        }

        // 分析受影响组件自身
        SystemMetrics affectedMetric = metrics.get(affectedComponent);
        if (affectedMetric != null) {
            double selfCorrelation = calculateCorrelation(affectedComponent, affectedMetric,
                    baselines.getOrDefault(affectedComponent, 70.0));
            metricsAnalysis.put(affectedComponent + "_correlation", selfCorrelation);

            if (selfCorrelation > maxCorrelation) {
                mostLikelyRootCause = affectedComponent;
            }

            if (isComponentAbnormal(affectedComponent, affectedMetric,
                    baselines.getOrDefault(affectedComponent, 70.0))) {
                evidenceList.add(getAbnormalEvidence(affectedComponent, affectedMetric));
                suggestionsList.add(getComponentSuggestion(affectedComponent));
            }
        }

        info.setRootCause(mostLikelyRootCause);
        info.setConfidence(Math.min(0.95, maxCorrelation * 1.2));
        info.setEvidence(evidenceList.isEmpty() ? "未发现明显异常" : String.join("; ", evidenceList));
        info.setSuggestions(suggestionsList.isEmpty() ? List.of("系统运行正常") : suggestionsList);
        info.setMetricsAnalysis(metricsAnalysis);

        return info;
    }

    private double calculateCorrelation(String component, SystemMetrics metric, double baseline) {
        double deviation = 0.0;

        if (component.equals("CPU") && metric.getCpuUsage() != null) {
            deviation = Math.max(0, metric.getCpuUsage() - baseline) / 100;
        } else if (component.equals("Memory") && metric.getMemUsage() != null) {
            deviation = Math.max(0, metric.getMemUsage() - baseline) / 100;
        } else if (component.equals("Processes") && metric.getProcessCount() != null) {
            deviation = Math.max(0, metric.getProcessCount() - baseline) / 300;
        } else if (component.startsWith("Disk-") && metric.getDiskUsage() != null) {
            // 磁盘相关性计算（简化）
            deviation = Math.max(0, (baseline - 50) / 50); // 基于基线计算
        }

        return Math.min(1.0, deviation * 2);
    }

    private boolean isComponentAbnormal(String component, SystemMetrics metric, double baseline) {
        if (component.equals("CPU") && metric.getCpuUsage() != null) {
            return metric.getCpuUsage() > baseline;
        } else if (component.equals("Memory") && metric.getMemUsage() != null) {
            return metric.getMemUsage() > baseline;
        } else if (component.equals("Processes") && metric.getProcessCount() != null) {
            return metric.getProcessCount() > baseline;
        } else if (component.startsWith("Disk-")) {
            // 磁盘异常检测（简化）
            return metric.getDiskUsage() != null && metric.getDiskUsage() > 100; // 使用量超过100GB认为异常
        }
        return false;
    }

    private String getAbnormalEvidence(String component, SystemMetrics metric) {
        if (component.equals("CPU")) {
            return String.format("CPU使用率过高: %.1f%%", metric.getCpuUsage());
        } else if (component.equals("Memory")) {
            return String.format("内存使用率过高: %.1f%%", metric.getMemUsage());
        } else if (component.equals("Processes")) {
            return String.format("进程数量过多: %d个", metric.getProcessCount());
        } else if (component.startsWith("Disk-")) {
            return String.format("磁盘%s使用量: %dGB", component.substring(5), metric.getDiskUsage());
        }
        return component + "状态异常";
    }

    private String getComponentSuggestion(String component) {
        Map<String, String> suggestions = Map.of(
                "CPU", "关闭不必要的应用程序，减少CPU负载",
                "Memory", "清理内存，关闭未使用的程序",
                "Network", "检查网络连接，优化带宽使用",
                "Processes", "结束不必要的后台进程"
        );

        if (component.startsWith("Disk-")) {
            return "清理" + component.substring(5) + "盘空间，删除临时文件";
        }

        return suggestions.getOrDefault(component, "检查" + component + "状态");
    }

    private void handleAnalysisFailure(Map<String, Object> result, String affectedComponent, Exception e) {
        result.put("rootCause", affectedComponent);
        result.put("confidence", 0.6);
        result.put("evidence", "分析服务暂时不可用: " + e.getMessage());
        result.put("suggestions", List.of("检查" + affectedComponent + "资源使用情况", "重启相关服务"));
        result.put("analyzedComponents", List.of("CPU", "Memory", "Network", "Processes"));
    }

    // 内部类用于封装分析结果
    private static class RootCauseInfo {
        private String rootCause;
        private double confidence;
        private String evidence;
        private List<String> suggestions;
        private Map<String, Object> metricsAnalysis;

        public String getRootCause() { return rootCause; }
        public void setRootCause(String rootCause) { this.rootCause = rootCause; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public String getEvidence() { return evidence; }
        public void setEvidence(String evidence) { this.evidence = evidence; }
        public List<String> getSuggestions() { return suggestions; }
        public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
        public Map<String, Object> getMetricsAnalysis() { return metricsAnalysis; }
        public void setMetricsAnalysis(Map<String, Object> metricsAnalysis) { this.metricsAnalysis = metricsAnalysis; }
    }
}