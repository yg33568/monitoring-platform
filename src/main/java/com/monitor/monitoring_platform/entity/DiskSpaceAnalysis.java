package com.monitor.monitoring_platform.entity;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class DiskSpaceAnalysis {
    private String mountPoint;          // C:
    private long totalSpace;           // 总空间(GB)
    private long usedSpace;            // 已用空间(GB)
    private double usagePercent;       // 使用百分比
    private List<SpaceCategory> categories; // 4个主要分类
    private Date analyzeTime;          // 分析时间

    public DiskSpaceAnalysis() {
        this.analyzeTime = new Date();
    }

    // 手动添加setter方法（如果Lombok不工作）
    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
    }

    public void setTotalSpace(long totalSpace) {
        this.totalSpace = totalSpace;
    }

    public void setUsedSpace(long usedSpace) {
        this.usedSpace = usedSpace;
    }

    public void setUsagePercent(double usagePercent) {
        this.usagePercent = usagePercent;
    }

    public void setCategories(List<SpaceCategory> categories) {
        this.categories = categories;
    }

    public void setAnalyzeTime(Date analyzeTime) {
        this.analyzeTime = analyzeTime;
    }

    // getter方法
    public String getMountPoint() {
        return mountPoint;
    }

    public long getTotalSpace() {
        return totalSpace;
    }

    public long getUsedSpace() {
        return usedSpace;
    }

    public double getUsagePercent() {
        return usagePercent;
    }

    public List<SpaceCategory> getCategories() {
        return categories;
    }

    public Date getAnalyzeTime() {
        return analyzeTime;
    }
}