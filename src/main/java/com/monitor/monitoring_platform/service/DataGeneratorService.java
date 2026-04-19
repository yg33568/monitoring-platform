//数据生成服务
package com.monitor.monitoring_platform.service;

import com.monitor.monitoring_platform.entity.SystemMetrics;
import com.monitor.monitoring_platform.entity.DiskInfo;
import com.monitor.monitoring_platform.mapper.SystemMetricsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataGeneratorService {

    @Autowired
    private SystemMetricsMapper systemMetricsMapper;

    @Autowired
    private RealSystemDataService realSystemDataService;

    @Scheduled(fixedRate = 30000) // 每30秒生成一次
    public void generateRealSystemMetrics() {
        System.out.println("=== 开始生成真实监控数据 ===");

        try {
            // 生成核心组件的真实数据
            generateCoreComponents();

            // 生成磁盘数据
            generateDiskMetrics();

            System.out.println("=== 真实数据生成完成 ===");

        } catch (Exception e) {
            System.err.println(" 生成真实数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 生成核心组件数据
     */
    private void generateCoreComponents() {
        String[] coreComponents = {"CPU", "Memory", "Network", "Processes"};

        for (String component : coreComponents) {
            SystemMetrics metrics = realSystemDataService.getCompleteRealMetrics(component);
            if (metrics != null) {
                systemMetricsMapper.insert(metrics);
                logComponentData(component, metrics);
            }
        }
    }

    /**
     * 生成磁盘监控数据
     */
    private void generateDiskMetrics() {
        try {
            List<DiskInfo> disks = realSystemDataService.getAllDiskUsage();

            if (disks.isEmpty()) {
                System.out.println(" 未检测到磁盘信息");
                return;
            }

            System.out.println("检测到 " + disks.size() + " 个磁盘分区:");

            for (int i = 0; i < disks.size(); i++) {
                DiskInfo disk = disks.get(i);

                // 为每个磁盘创建监控记录
                SystemMetrics diskMetrics = new SystemMetrics();
                diskMetrics.setComponentName("Disk-" + disk.getMountPoint().replace(":", "").replace("/", ""));
                diskMetrics.setDiskUsage(disk.getUsedSpace());
                diskMetrics.setTimestamp(java.time.LocalDateTime.now());

                systemMetricsMapper.insert(diskMetrics);

                // 打印真实的磁盘信息
                System.out.println("   " + disk.getMountPoint() + ": " +
                        disk.getUsedSpace() + "GB/" + disk.getTotalSpace() + "GB (" +
                        String.format("%.1f", disk.getUsagePercent()) + "%) - 可用: " +
                        disk.getFreeSpace() + "GB");
            }

        } catch (Exception e) {
            System.err.println("生成磁盘数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 记录组件数据日志
     */
    private void logComponentData(String component, SystemMetrics metrics) {
        switch (component) {
            case "CPU":
                System.out.println("CPU: " + metrics.getCpuUsage() + "%");
                break;
            case "Memory":
                System.out.println("内存: " + metrics.getMemUsage() + "%");
                break;
            case "Network":
                System.out.println("网络: " + metrics.getNetworkRate() + "MB/s");
                break;
            case "Processes":
                System.out.println("进程: " + metrics.getProcessCount() + "个");
                break;
        }
    }
}