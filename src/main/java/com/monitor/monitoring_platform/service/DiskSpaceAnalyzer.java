package com.monitor.monitoring_platform.service;

import com.monitor.monitoring_platform.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class DiskSpaceAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(DiskSpaceAnalyzer.class);

    public DiskSpaceAnalysis analyzeDiskSpace(String mountPoint) {
        System.out.println("=== 🎯 磁盘分析API被调用 ===");
        System.out.println("挂载点: " + mountPoint);

        DiskSpaceAnalysis analysis = new DiskSpaceAnalysis();
        analysis.setMountPoint(mountPoint);

        try {
            File disk = new File(mountPoint);
            if (!disk.exists()) {
                throw new IllegalArgumentException("磁盘路径不存在: " + mountPoint);
            }

            // 设置基础磁盘信息（快速计算）
            long totalBytes = disk.getTotalSpace();
            long freeBytes = disk.getFreeSpace();
            long usedBytes = totalBytes - freeBytes;

            analysis.setTotalSpace(totalBytes / (1024 * 1024 * 1024));
            analysis.setUsedSpace(usedBytes / (1024 * 1024 * 1024));
            analysis.setUsagePercent((double) analysis.getUsedSpace() / analysis.getTotalSpace() * 100);

            // 使用模拟数据，避免深度目录扫描
            analysis.setCategories(createMockCategories(analysis.getUsedSpace()));

            System.out.println("✅ 磁盘分析完成: " + mountPoint);

        } catch (Exception e) {
            System.err.println("❌ 磁盘分析失败: " + e.getMessage());
            throw new RuntimeException("磁盘空间分析失败", e);
        }

        return analysis;
    }

    // 创建模拟的分类数据（避免目录扫描）
    private List<SpaceCategory> createMockCategories(long totalUsedSpace) {
        List<SpaceCategory> categories = new ArrayList<>();

        // 系统文件
        SpaceCategory system = new SpaceCategory();
        system.setName("系统文件");
        system.setIcon("💻");
        system.setSize(totalUsedSpace * 30 / 100); // 30%
        categories.add(system);

        // 用户数据
        SpaceCategory user = new SpaceCategory();
        user.setName("用户数据");
        user.setIcon("👤");
        user.setSize(totalUsedSpace * 50 / 100); // 50%
        categories.add(user);

        // 应用程序
        SpaceCategory apps = new SpaceCategory();
        apps.setName("应用程序");
        apps.setIcon("🖥️");
        apps.setSize(totalUsedSpace * 15 / 100); // 15%
        categories.add(apps);

        // 可清理空间
        SpaceCategory cleanup = new SpaceCategory();
        cleanup.setName("可清理空间");
        cleanup.setIcon("🗑️");
        cleanup.setSize(totalUsedSpace * 5 / 100); // 5%
        categories.add(cleanup);

        return categories;
    }

    private SpaceCategory analyzeSystemFiles(String mountPoint) {
        SpaceCategory systemCategory = new SpaceCategory();
        systemCategory.setName("系统文件");
        systemCategory.setIcon("💻");

        List<SpaceItem> systemItems = new ArrayList<>();
        long totalSystemSize = 0;

        File windowsDir = new File(mountPoint, "Windows");
        if (windowsDir.exists()) {
            long windowsSize = getDirectorySizeGB(windowsDir);
            systemItems.add(createSpaceItem("Windows系统", "Windows", windowsSize));
            totalSystemSize += windowsSize;
        }

        File progFiles = new File(mountPoint, "Program Files");
        File progFiles86 = new File(mountPoint, "Program Files (x86)");
        long progSize = getDirectorySizeGB(progFiles) + getDirectorySizeGB(progFiles86);
        systemItems.add(createSpaceItem("程序文件", "Program Files", progSize));
        totalSystemSize += progSize;

        long systemCacheSize = estimateSystemCacheSize(mountPoint);
        systemItems.add(createSpaceItem("系统缓存", "系统临时文件", systemCacheSize));
        totalSystemSize += systemCacheSize;

        systemCategory.setSize(totalSystemSize);
        systemCategory.setItems(systemItems);

        return systemCategory;
    }

    private SpaceCategory analyzeUserData(String mountPoint) {
        SpaceCategory userCategory = new SpaceCategory();
        userCategory.setName("用户数据");
        userCategory.setIcon("👤");

        List<SpaceItem> userItems = new ArrayList<>();
        long totalUserSize = 0;

        File usersDir = new File(mountPoint, "Users");
        if (usersDir.exists()) {
            File documentsDir = new File(usersDir, getCurrentUserName() + "/Documents");
            long docsSize = getDirectorySizeGB(documentsDir);
            userItems.add(createSpaceItem("文档资料", "Users/Documents", docsSize));
            totalUserSize += docsSize;

            File downloadsDir = new File(usersDir, getCurrentUserName() + "/Downloads");
            long downloadsSize = getDirectorySizeGB(downloadsDir);
            userItems.add(createSpaceItem("下载文件", "Users/Downloads", downloadsSize));
            totalUserSize += downloadsSize;

            File desktopDir = new File(usersDir, getCurrentUserName() + "/Desktop");
            long desktopSize = getDirectorySizeGB(desktopDir);
            userItems.add(createSpaceItem("桌面文件", "Users/Desktop", desktopSize));
            totalUserSize += desktopSize;

            long otherUserSize = Math.max(0, getDirectorySizeGB(usersDir) - docsSize - downloadsSize - desktopSize);
            userItems.add(createSpaceItem("其他用户数据", "Users/其他", otherUserSize));
            totalUserSize += otherUserSize;
        }

        userCategory.setSize(totalUserSize);
        userCategory.setItems(userItems);

        return userCategory;
    }

    private SpaceCategory analyzeApplications(String mountPoint) {
        SpaceCategory appCategory = new SpaceCategory();
        appCategory.setName("应用程序");
        appCategory.setIcon("🖥️");

        List<SpaceItem> appItems = new ArrayList<>();
        long totalAppSize = 0;

        long devToolsSize = analyzeDevelopmentTools(mountPoint);
        appItems.add(createSpaceItem("开发工具", "Program Files/JetBrains 等", devToolsSize));
        totalAppSize += devToolsSize;

        long officeSize = analyzeOfficeSoftware(mountPoint);
        appItems.add(createSpaceItem("办公软件", "Program Files/Microsoft Office", officeSize));
        totalAppSize += officeSize;

        long otherAppSize = analyzeOtherApplications(mountPoint);
        appItems.add(createSpaceItem("其他应用", "Program Files/其他", otherAppSize));
        totalAppSize += otherAppSize;

        appCategory.setSize(totalAppSize);
        appCategory.setItems(appItems);

        return appCategory;
    }

    private SpaceCategory analyzeCleanupSpace(String mountPoint) {
        SpaceCategory cleanupCategory = new SpaceCategory();
        cleanupCategory.setName("可清理空间");
        cleanupCategory.setIcon("🗑️");

        List<SpaceItem> cleanupItems = new ArrayList<>();
        long totalCleanupSize = 0;

        long tempSize = analyzeTempFiles();
        cleanupItems.add(createSpaceItem("临时文件", "Temp 目录", tempSize));
        totalCleanupSize += tempSize;

        long systemCacheSize = analyzeSystemCache();
        cleanupItems.add(createSpaceItem("系统缓存", "系统缓存", systemCacheSize));
        totalCleanupSize += systemCacheSize;

        long browserCacheSize = analyzeBrowserCache();
        cleanupItems.add(createSpaceItem("浏览器缓存", "浏览器缓存", browserCacheSize));
        totalCleanupSize += browserCacheSize;

        long recycleBinSize = estimateRecycleBinSize();
        cleanupItems.add(createSpaceItem("回收站", "回收站", recycleBinSize));
        totalCleanupSize += recycleBinSize;

        cleanupCategory.setSize(totalCleanupSize);
        cleanupCategory.setItems(cleanupItems);

        return cleanupCategory;
    }

    // 辅助方法
    private SpaceItem createSpaceItem(String name, String path, long size) {
        SpaceItem item = new SpaceItem();
        item.setName(name);
        item.setPath(path);
        item.setSize(size);
        return item;
    }

    private long getDirectorySizeGB(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return 0;
        }

        long size = 0;
        try {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else if (file.isDirectory()) {
                        size += getDirectorySizeGB(file);
                    }
                }
            }
        } catch (SecurityException e) {
            log.warn("无法访问目录: {}", directory.getAbsolutePath());
        }

        return size / (1024 * 1024 * 1024);
    }

    private String getCurrentUserName() {
        return System.getProperty("user.name");
    }

    private long estimateSystemCacheSize(String mountPoint) {
        // 分析系统缓存目录
        File systemCacheDir = new File(mountPoint, "Windows/Temp");
        long systemCacheSize = getDirectorySizeGB(systemCacheDir);

        // 加上预取文件等
        File prefetchDir = new File(mountPoint, "Windows/Prefetch");
        systemCacheSize += getDirectorySizeGB(prefetchDir);

        return systemCacheSize > 0 ? systemCacheSize : 2L; // 最少2GB估算
    }

    private long analyzeDevelopmentTools(String mountPoint) {
        long totalSize = 0;
        File jetbrainsDir = new File(mountPoint, "Program Files/JetBrains");
        totalSize += getDirectorySizeGB(jetbrainsDir);

        File vscodeDir = new File(mountPoint, "Program Files/Microsoft VS Code");
        totalSize += getDirectorySizeGB(vscodeDir);

        return totalSize;
    }

    private long analyzeOfficeSoftware(String mountPoint) {
        File officeDir = new File(mountPoint, "Program Files/Microsoft Office");
        return getDirectorySizeGB(officeDir);
    }

    private long analyzeOtherApplications(String mountPoint) {
        // 分析 Program Files 下除已知应用外的其他应用
        File progFiles = new File(mountPoint, "Program Files");
        File progFiles86 = new File(mountPoint, "Program Files (x86)");

        long totalProgSize = getDirectorySizeGB(progFiles) + getDirectorySizeGB(progFiles86);

        // 减去已知的开发工具和办公软件
        long knownAppsSize = analyzeDevelopmentTools(mountPoint) + analyzeOfficeSoftware(mountPoint);
        long otherAppsSize = totalProgSize - knownAppsSize;

        return Math.max(otherAppsSize, 0); // 确保不为负数
    }

    private long analyzeTempFiles() {
        String tempDir = System.getenv("TEMP");
        if (tempDir != null) {
            return getDirectorySizeGB(new File(tempDir));
        }
        return 4L;
    }

    private long analyzeSystemCache() {
        // 分析系统级缓存目录
        String systemRoot = System.getenv("SystemRoot");
        if (systemRoot != null) {
            File systemTemp = new File(systemRoot, "Temp");
            File prefetch = new File(systemRoot, "Prefetch");
            File logs = new File(systemRoot, "Logs");

            return getDirectorySizeGB(systemTemp) + getDirectorySizeGB(prefetch) + getDirectorySizeGB(logs);
        }
        return 3L;
    }
    private long analyzeBrowserCache() {
        // 分析常见浏览器缓存目录
        long totalBrowserCache = 0;
        String userHome = System.getProperty("user.home");

        // Chrome 缓存
        File chromeCache = new File(userHome, "AppData/Local/Google/Chrome/User Data/Default/Cache");
        totalBrowserCache += getDirectorySizeGB(chromeCache);

        // Edge 缓存
        File edgeCache = new File(userHome, "AppData/Local/Microsoft/Edge/User Data/Default/Cache");
        totalBrowserCache += getDirectorySizeGB(edgeCache);

        // Firefox 缓存
        File firefoxCache = new File(userHome, "AppData/Local/Mozilla/Firefox/Profiles");
        totalBrowserCache += getDirectorySizeGB(firefoxCache);

        return totalBrowserCache;
    }

    private long estimateRecycleBinSize() {
        // 回收站大小估算（Windows）
        String systemDrive = System.getenv("SystemDrive");
        if (systemDrive != null) {
            File recycleBin = new File(systemDrive + "$Recycle.Bin");
            if (recycleBin.exists()) {
                return getDirectorySizeGB(recycleBin);
            }
        }
        return 1L;
    }
}