package com.monitor.monitoring_platform.controller;

import com.monitor.monitoring_platform.entity.SmartAnalysisResult;
import com.monitor.monitoring_platform.entity.SystemMetrics;
import com.monitor.monitoring_platform.mapper.SystemMetricsMapper;
import com.monitor.monitoring_platform.service.SmartRootCauseService;
import com.monitor.monitoring_platform.service.SystemMetricsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.*;

@Controller
//@Slf4j
public class DashboardController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DashboardController.class);
    @Autowired
    private SystemMetricsMapper systemMetricsMapper;

    @Autowired  // 添加这个注入
    private SystemMetricsService systemMetricsService;

    @Autowired  // 添加这个注入
    private SmartRootCauseService smartRootCauseService;

    @GetMapping("/interactive-dashboard")
    public String interactiveDashboard() {
        return "interactive-dashboard";
    }

    @GetMapping("/microservices")
    public String microservices() {
        return "microservices";
    }

    @GetMapping("/history")
    public String history(Model model) {
        try {
            // 获取最近24小时数据
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24);

            List<SystemMetrics> historyData = systemMetricsMapper.selectByTimeRange(startTime, endTime);
            model.addAttribute("historyData", historyData);
            model.addAttribute("startTime", startTime);
            model.addAttribute("endTime", endTime);

        } catch (Exception e) {
            e.printStackTrace();
            // 如果出错，设置空数据但显示页面
            model.addAttribute("historyData", new ArrayList<>());
            model.addAttribute("startTime", LocalDateTime.now().minusHours(24));
            model.addAttribute("endTime", LocalDateTime.now());
        }
        return "history";
    }

    @GetMapping("/analysis")
    public String analysis(Model model) {
        System.out.println("=== 开始处理 /analysis 请求 ===");

        // 步骤1：拓扑生成
        try {
            System.out.println("步骤1: 创建拓扑图...");
            Map<String, Object> topology = createDynamicTopology();
            model.addAttribute("topology", topology);
            System.out.println("✓ 拓扑创建成功");

        } catch (Exception e) {
            System.out.println("✗ 拓扑创建失败: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("topology", createFallbackTopology());
            System.out.println("✓ 使用备用拓扑");
        }

        // 步骤2：智能分析
        try {
            System.out.println("步骤2: 准备测试数据...");
            List<SystemMetrics> testMetrics = createTestMetrics();
            System.out.println("✓ 测试数据创建完成: " + testMetrics.size() + " 条记录");

            System.out.println("步骤3: 调用智能分析服务...");
            SystemMetrics current = testMetrics.get(testMetrics.size() - 1);
            SmartAnalysisResult testResult = smartRootCauseService.analyze(current, testMetrics);
            System.out.println("✓ 智能分析完成: " + testResult.getDiagnoses().size() + " 个诊断");

            model.addAttribute("smartAnalysis", testResult);

        } catch (Exception e) {
            System.out.println("✗ 智能分析失败: " + e.getMessage());
            e.printStackTrace();
            System.out.println("步骤4: 创建空结果...");
            SmartAnalysisResult emptyResult = new SmartAnalysisResult(new ArrayList<>(), new Date());
            model.addAttribute("smartAnalysis", emptyResult);
            System.out.println("✓ 空结果创建完成");
        }

        System.out.println("=== /analysis 请求处理完成，返回页面 ===");
        return "analysis";
    }

    /**
     * 创建测试数据（备用方法）
     */
    private List<SystemMetrics> createTestMetrics() {
        List<SystemMetrics> metrics = new ArrayList<>();

        // 创建模拟数据，模拟系统指标变化
        for (int i = 0; i < 10; i++) {
            SystemMetrics metric = new SystemMetrics();
            metric.setCpuUsage((double) (60 + i * 4));      // CPU从60%逐渐上升到96%
            metric.setMemUsage((double) (50 + i * 5));   // 内存从50%逐渐上升到95%
            metric.setDiskUsage((long) (40 + i * 3));     // 磁盘从40%逐渐上升到67%
            metrics.add(metric);
        }

        return metrics;
    }

    /**
     * 创建动态拓扑结构（自动检测磁盘）
     */
    private Map<String, Object> createDynamicTopology() {
        Map<String, Object> topology = new HashMap<>();

        // 获取所有磁盘信息
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
            int diskCategory = 4; // 磁盘节点的分类
            int diskY = 200;      // 磁盘节点的Y坐标
            int diskXStart = 100; // 第一个磁盘的X坐标
            int diskXGap = 200;   // 磁盘节点之间的间隔

            for (int i = 0; i < recentDisks.size(); i++) {
                SystemMetrics disk = recentDisks.get(i);
                String diskId = disk.getComponentName();
                String diskName = getDiskDisplayName(diskId);

                int diskX = diskXStart + (i * diskXGap);
                nodes.add(createNode(diskId, diskName, diskCategory, diskX, diskY));

                // 为磁盘创建连接
                links.add(createLink("Memory", diskId)); // 内存连接到磁盘
                links.add(createLink("Processes", diskId)); // 进程连接到磁盘
            }
        } else {
            // 如果没有检测到磁盘，使用默认的C盘、D盘
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
    }

    /**
     * 获取磁盘显示名称
     */
    private String getDiskDisplayName(String componentName) {
        if (componentName.startsWith("Disk-")) {
            String diskLetter = componentName.substring(5); // 去掉 "Disk-"
            return diskLetter + "盘存储";
        }
        return componentName;
    }

    /**
     * 备用拓扑结构（当动态检测失败时使用）
     */
    private Map<String, Object> createFallbackTopology() {
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
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/interactive-dashboard";
    }

    private Map<String, Object> createNode(String id, String name, int category, int x, int y) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", id);
        node.put("name", name);
        node.put("category", category);
        node.put("x", x);
        node.put("y", y);
        node.put("symbolSize", 50);
        return node;
    }

    private Map<String, Object> createLink(String source, String target) {
        Map<String, Object> link = new HashMap<>();
        link.put("source", source);
        link.put("target", target);
        return link;
    }
}