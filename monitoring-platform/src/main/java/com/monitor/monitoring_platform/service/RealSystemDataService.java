//真实数据采集服务
package com.monitor.monitoring_platform.service;

import com.monitor.monitoring_platform.entity.DiskInfo;
import com.monitor.monitoring_platform.entity.SystemMetrics;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RealSystemDataService {

    private final SystemInfo systemInfo = new SystemInfo();
    private final HardwareAbstractionLayer hardware = systemInfo.getHardware();
    private final CentralProcessor processor = hardware.getProcessor();
    private final GlobalMemory memory = hardware.getMemory();
    private final OperatingSystem os = systemInfo.getOperatingSystem();

    // 用于CPU使用率计算
    private long[] previousTicks;
    private long previousTime;

    // 用于网络速率计算
    private long previousBytesReceived = 0;
    private long previousBytesSent = 0;
    private long previousNetworkTime = 0;

    /**
     * 获取真实的CPU使用率
     */
    public double getRealCpuUsage() {
        try {
            if (previousTicks == null) {
                // 第一次调用，初始化
                previousTicks = processor.getSystemCpuLoadTicks();
                previousTime = System.currentTimeMillis();
                Thread.sleep(500); //0.5s刷新一次
            }

            long[] currentTicks = processor.getSystemCpuLoadTicks();
            long currentTime = System.currentTimeMillis();

            // 计算CPU使用率
            double cpuUsage = processor.getSystemCpuLoadBetweenTicks(previousTicks) * 100;

            // 更新前一次的值
            previousTicks = currentTicks;
            previousTime = currentTime;

            return Double.parseDouble(new DecimalFormat("#.##").format(cpuUsage));

        } catch (Exception e) {
            System.err.println("获取CPU使用率失败: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * 获取真实的内存使用率
     */
    public double getRealMemoryUsage() {
        try {
            long totalMemory = memory.getTotal();
            long availableMemory = memory.getAvailable();
            long usedMemory = totalMemory - availableMemory;
            double memoryUsage = (usedMemory * 100.0) / totalMemory;

            return Double.parseDouble(new DecimalFormat("#.##").format(memoryUsage));
        } catch (Exception e) {
            System.err.println("获取内存使用率失败: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * 获取所有磁盘分区的真实使用情况
     */
    public List<DiskInfo> getAllDiskUsage() {
        List<DiskInfo> diskList = new ArrayList<>();

        try {
            FileSystem fileSystem = os.getFileSystem();
            List<OSFileStore> fileStores = fileSystem.getFileStores();

            for (OSFileStore fs : fileStores) {
                // 跳过无效的磁盘
                if (shouldSkipDisk(fs)) {
                    continue;
                }

                // 获取真实的磁盘容量信息
                long totalBytes = fs.getTotalSpace();
                long freeBytes = fs.getUsableSpace();
                long usedBytes = totalBytes - freeBytes;

                // 转换为GB
                long totalGB = totalBytes / (1024 * 1024 * 1024);
                long freeGB = freeBytes / (1024 * 1024 * 1024);
                long usedGB = usedBytes / (1024 * 1024 * 1024);

                // 计算真实的使用率
                double usagePercent = totalGB > 0 ? (double) usedGB / totalGB * 100 : 0.0;

                DiskInfo diskInfo = new DiskInfo(
                        fs.getName(),
                        fs.getMount(),
                        totalGB,
                        usedGB,
                        freeGB,
                        usagePercent,
                        fs.getType()
                );

                diskList.add(diskInfo);

                System.out.println("磁盘检测: " + fs.getMount() +
                        " 总容量: " + totalGB + "GB, 已用: " + usedGB + "GB, 可用: " + freeGB + "GB, 使用率: " +
                        String.format("%.1f", usagePercent) + "%");
            }

            // 按挂载点排序
            diskList.sort(Comparator.comparing(DiskInfo::getMountPoint));

            System.out.println("=== 磁盘检测完成 ===");
            System.out.println("共检测到 " + diskList.size() + " 个磁盘分区");
            System.out.println("总容量: " + diskList.stream().mapToLong(DiskInfo::getTotalSpace).sum() + "GB");
            System.out.println("总已用: " + diskList.stream().mapToLong(DiskInfo::getUsedSpace).sum() + "GB");

            return diskList;

        } catch (Exception e) {
            System.err.println("获取磁盘信息失败: " + e.getMessage());
            e.printStackTrace();
            return getFallbackDiskInfo();
        }
    }

    /**
     * 判断是否应该跳过该磁盘
     */
    private boolean shouldSkipDisk(OSFileStore fs) {
        String type = fs.getType().toLowerCase();
        String mount = fs.getMount().toLowerCase();
        long totalSpace = fs.getTotalSpace();

        // 跳过以下类型的磁盘
        return type.contains("tmpfs") ||          // 临时文件系统
                type.contains("ramfs") ||          // 内存文件系统
                type.contains("devtmpfs") ||       // 设备临时文件系统
                mount.contains("/proc") ||         // 进程文件系统
                mount.contains("/sys") ||          // 系统文件系统
                mount.contains("/dev") ||          // 设备文件系统
                mount.contains("/snap") ||         // Snap包系统
                totalSpace == 0 ||                 // 总空间为0
                totalSpace < (100 * 1024 * 1024);  // 小于100MB的磁盘（可能是虚拟磁盘）
    }

    /**
     * 备用磁盘信息（当获取失败时使用真实的基础信息）
     */
    private List<DiskInfo> getFallbackDiskInfo() {
        List<DiskInfo> fallback = new ArrayList<>();

        try {
            // 尝试获取基本的磁盘信息
            File root = new File("/");
            if (root.exists()) {
                long total = root.getTotalSpace() / (1024 * 1024 * 1024);
                long free = root.getFreeSpace() / (1024 * 1024 * 1024);
                long used = total - free;
                double percent = total > 0 ? (double) used / total * 100 : 0.0;

                // 使用新的构造器，添加 freeSpace 参数
                fallback.add(new DiskInfo(
                        "System",
                        System.getProperty("os.name").contains("Windows") ? "C:" : "/",
                        total,
                        used,
                        free,        // 添加 freeSpace
                        percent,
                        "Unknown"
                ));

                System.out.println("使用备用磁盘信息: " +
                        (System.getProperty("os.name").contains("Windows") ? "C:" : "/") +
                        " 总容量: " + total + "GB, 已用: " + used + "GB, 可用: " + free + "GB");
            }
        } catch (Exception e) {
            // 如果连基础信息都获取失败，返回默认值
            System.err.println("获取备用磁盘信息失败: " + e.getMessage());
            fallback.add(createDefaultDiskInfo());
        }

        return fallback;
    }

    /**
     * 创建默认的磁盘信息
     */
    private DiskInfo createDefaultDiskInfo() {
        System.out.println("使用默认磁盘信息");
        return new DiskInfo(
                "Default",
                "C:",
                256L,    // 默认总容量
                120L,    // 默认已用
                136L,    // 默认可用
                46.9,    // 默认使用率
                "NTFS"
        );
    }
    /**
     * 获取实时网络速率（MB/s）
     */
    public Double getRealNetworkRate() {
        try {
            List<NetworkIF> networks = hardware.getNetworkIFs();
            if (networks.isEmpty()) {
                return 0.1;
            }

            // 找到活动的网络接口（通过数据量判断）
            NetworkIF activeNetwork = null;
            long maxBytes = 0;

            for (NetworkIF net : networks) {
                net.updateAttributes();
                long bytesRecv = net.getBytesRecv();

                // 选择接收数据最多的网络接口（通常是最活跃的）
                if (bytesRecv > maxBytes) {
                    maxBytes = bytesRecv;
                    activeNetwork = net;
                }
            }

            if (activeNetwork == null) {
                activeNetwork = networks.get(0); // 如果没有活动的，用第一个
            }

            activeNetwork.updateAttributes();

            long currentBytesRecv = activeNetwork.getBytesRecv();
            long currentBytesSent = activeNetwork.getBytesSent();
            long currentTime = System.currentTimeMillis();

            if (previousNetworkTime == 0) {
                // 第一次调用，初始化
                previousBytesReceived = currentBytesRecv;
                previousBytesSent = currentBytesSent;
                previousNetworkTime = currentTime;
                return 0.1; // 第一次返回默认值
            }

            long timeInterval = currentTime - previousNetworkTime;
            if (timeInterval < 1000) {
                timeInterval = 1000; // 最小1秒间隔
            }

            // 计算速率 (字节/秒 → MB/秒)
            double recvRate = (currentBytesRecv - previousBytesReceived) / (timeInterval / 1000.0) / (1024 * 1024);
            double sentRate = (currentBytesSent - previousBytesSent) / (timeInterval / 1000.0) / (1024 * 1024);

            // 更新前一次的值
            previousBytesReceived = currentBytesRecv;
            previousBytesSent = currentBytesSent;
            previousNetworkTime = currentTime;

            double totalRate = recvRate + sentRate;
            return Double.parseDouble(new DecimalFormat("#.##").format(Math.max(totalRate, 0.01)));

        } catch (Exception e) {
            System.err.println("获取网络速率失败: " + e.getMessage());
            return 0.1;
        }
    }

    /**
     * 获取进程数量
     */
    public int getRealProcessCount() {
        try {
            return os.getProcessCount();
        } catch (Exception e) {
            return 150;
        }
    }

    /**
     * 获取完整的真实监控数据
     */
    public SystemMetrics getCompleteRealMetrics(String componentName) {
        SystemMetrics metrics = new SystemMetrics();
        metrics.setComponentName(componentName);
        metrics.setTimestamp(LocalDateTime.now());

        try {
            switch (componentName) {
                case "CPU":
                    metrics.setCpuUsage(getRealCpuUsage());
                    break;
                case "Memory":
                    metrics.setMemUsage(getRealMemoryUsage());
                    break;
                case "Network":
                    metrics.setNetworkRate(getRealNetworkRate());
                    break;
                case "Processes":
                    metrics.setProcessCount(getRealProcessCount());
                    break;
                // 完全删除 Disk-C 和 Disk-D 分支
                // 磁盘数据现在通过专门的 /api/disks 接口获取
            }
        } catch (Exception e) {
            System.err.println("获取 " + componentName + " 数据失败: " + e.getMessage());
        }

        return metrics;
    }
    /**
     * 获取系统信息摘要
     */
    public String getSystemSummary() {
        try {
            long totalMemory = memory.getTotal();
            double totalGB = totalMemory / (1024.0 * 1024.0 * 1024.0);

            return String.format("系统: %s %s | 处理器: %s | 内存: %.1fGB",
                    os.getFamily(),
                    os.getVersionInfo().getVersion(),
                    processor.getProcessorIdentifier().getName(),
                    totalGB);
        } catch (Exception e) {
            return "无法获取系统信息";
        }
    }
}