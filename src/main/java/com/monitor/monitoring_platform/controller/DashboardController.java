package com.monitor.monitoring_platform.controller;

import com.monitor.monitoring_platform.entity.SmartAnalysisResult;
import com.monitor.monitoring_platform.entity.SystemMetrics;
import com.monitor.monitoring_platform.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.*;

@Controller
public class DashboardController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

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

            List<SystemMetrics> historyData = dashboardService.getHistoryData(startTime, endTime);
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

        // 智能分析
        try {
            System.out.println("调用智能分析服务...");
            SmartAnalysisResult analysisResult=dashboardService.getSmartAnalysis();
            System.out.println("✓ 智能分析完成: " + analysisResult.getDiagnoses().size() + " 个诊断");
            model.addAttribute("smartAnalysis", analysisResult);

        } catch (Exception e) {
            System.out.println("✗ 智能分析失败: " + e.getMessage());
            e.printStackTrace();
            System.out.println("创建空结果...");
            SmartAnalysisResult emptyResult = new SmartAnalysisResult(new ArrayList<>(), new Date());
            model.addAttribute("smartAnalysis", emptyResult);
            System.out.println("✓ 空结果创建完成");
        }

        System.out.println("=== /analysis 请求处理完成，返回页面 ===");
        return "analysis";
    }

    @GetMapping("/analysis-data")
    @ResponseBody
    public SmartAnalysisResult getAnalysisData() {
        return dashboardService.getSmartAnalysis();
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/interactive-dashboard";
    }

}