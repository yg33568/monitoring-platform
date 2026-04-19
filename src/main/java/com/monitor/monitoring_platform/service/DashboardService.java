package com.monitor.monitoring_platform.service;

import com.monitor.monitoring_platform.entity.SmartAnalysisResult;
import com.monitor.monitoring_platform.entity.SystemMetrics;
import com.monitor.monitoring_platform.mapper.SystemMetricsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DashboardService {

    @Autowired
    private SystemMetricsMapper systemMetricsMapper;

    @Autowired
    private SmartRootCauseService smartRootCauseService;

    /**
     * 获取智能分析结果
     */
    public SmartAnalysisResult getSmartAnalysis() {
        // 从数据库获取最近的真实数据
        List<SystemMetrics> recentMetrics = systemMetricsMapper.selectRecentMetrics(10);

        if (recentMetrics == null || recentMetrics.isEmpty()) {
            // 如果没有真实数据，才用测试数据
            System.out.println("⚠️ 没有真实数据，使用测试数据");
            recentMetrics = createTestMetrics();
        }

        //取出最新的一条数据（当前时刻的 CPU、内存、磁盘数据）
        SystemMetrics current = recentMetrics.get(recentMetrics.size() - 1);
        return smartRootCauseService.analyze(current, recentMetrics);
    }
    /**
     * 获取动态拓扑图

    public Map<String, Object> getDynamicTopology() {
        Map<String, Object> topology = new HashMap<>();

        List<SystemMetrics> recentDisks = systemMetricsMapper.selectRecentDisks();

        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> links = new ArrayList<>();

        // 核心固定节点
        nodes.add(createNode("CPU", "中央处理器", 0, 100, 100));
        nodes.add(createNode("Memory", "内存", 1, 300, 100));
        nodes.add(createNode("Network", "网络", 2, 500, 100));
        nodes.add(createNode("Processes", "进程管理", 3, 500, 200));

        // 动态添加磁盘节点
        if (recentDisks != null && !recentDisks.isEmpty()) {
            int diskCategory = 4;
            int diskY = 200;
            int diskXStart = 100;
            int diskXGap = 200;

            for (int i = 0; i < recentDisks.size(); i++) {
                SystemMetrics disk = recentDisks.get(i);
                String diskId = disk.getComponentName();
                String diskName = getDiskDisplayName(diskId);

                int diskX = diskXStart + (i * diskXGap);
                nodes.add(createNode(diskId, diskName, diskCategory, diskX, diskY));

                links.add(createLink("Memory", diskId));
                links.add(createLink("Processes", diskId));
            }
        } else {
            nodes.add(createNode("Disk-C", "C盘存储", 4, 100, 200));
            nodes.add(createNode("Disk-D", "D盘存储", 4, 300, 200));
        }

        // 核心连接关系
        links.add(createLink("CPU", "Memory"));
        links.add(createLink("CPU", "Processes"));
        links.add(createLink("Memory", "Network"));
        links.add(createLink("Processes", "Network"));

        topology.put("nodes", nodes);
        topology.put("links", links);

        System.out.println("生成拓扑结构: " + nodes.size() + " 个节点, " + links.size() + " 个连接");

        return topology;
    }*/

    /**
     * 获取备用拓扑图

    public Map<String, Object> getFallbackTopology() {
        Map<String, Object> topology = new HashMap<>();

        List<Map<String, Object>> nodes = Arrays.asList(
                createNode("CPU", "中央处理器", 0, 100, 100),
                createNode("Memory", "内存", 1, 300, 100),
                createNode("Disk-C", "C盘存储", 2, 100, 200),
                createNode("Disk-D", "D盘存储", 3, 300, 200),
                createNode("Network", "网络", 4, 500, 100),
                createNode("Processes", "进程管理", 5, 500, 200)
        );

        List<Map<String, Object>> links = Arrays.asList(
                createLink("CPU", "Memory"),
                createLink("CPU", "Processes"),
                createLink("Memory", "Disk-C"),
                createLink("Memory", "Disk-D"),
                createLink("Processes", "Network")
        );

        topology.put("nodes", nodes);
        topology.put("links", links);

        return topology;
    }*/

    /**
     * 获取历史数据
     */
    public List<SystemMetrics> getHistoryData(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        return systemMetricsMapper.selectByTimeRange(startTime, endTime);
    }

    /**
     * 创建测试数据
     */
    private List<SystemMetrics> createTestMetrics() {
        List<SystemMetrics> metrics = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            SystemMetrics metric = new SystemMetrics();
            metric.setCpuUsage((double) (60 + i * 4));
            metric.setMemUsage((double) (50 + i * 5));
            metric.setDiskUsage((long) (40 + i * 3));
            metrics.add(metric);
        }
        return metrics;
    }

    /**
     * 创建拓扑节点

    private Map<String, Object> createNode(String id, String name, int category, int x, int y) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", id);
        node.put("name", name);
        node.put("category", category);
        node.put("x", x);
        node.put("y", y);
        node.put("symbolSize", 50);
        return node;
    }*/

    /**
     * 创建拓扑连线

    private Map<String, Object> createLink(String source, String target) {
        Map<String, Object> link = new HashMap<>();
        link.put("source", source);
        link.put("target", target);
        return link;
    }*/

    /**
     * 获取磁盘显示名称

    private String getDiskDisplayName(String componentName) {
        if (componentName.startsWith("Disk-")) {
            String diskLetter = componentName.substring(5);
            return diskLetter + "盘存储";
        }
        return componentName;
    }*/
}