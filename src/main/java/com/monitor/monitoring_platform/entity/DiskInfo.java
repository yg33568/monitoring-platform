//磁盘信息实体

package com.monitor.monitoring_platform.entity;

public class DiskInfo {
    private String name; // 磁盘名称
    private String mountPoint;// 挂载点 (C:\, D:\, / 等)
    private Long totalSpace;       // 总空间 (GB) - 从OSHI获取真实值
    private Long usedSpace;        // 已用空间 (GB)
    private Long freeSpace;        // 可用空间 (GB) - 新增字段
    private Double usagePercent;   // 使用百分比 - 根据真实数据计算
    private String type; // 文件系统类型

    public DiskInfo() {}

    public DiskInfo(String name, String mountPoint, Long totalSpace, Long usedSpace, Long freeSpace, Double usagePercent, String type) {
        this.name = name;
        this.mountPoint = mountPoint;
        this.totalSpace = totalSpace;
        this.usedSpace = usedSpace;
        this.freeSpace = freeSpace;
        this.usagePercent = usagePercent;
        this.type = type;
    }

    // Getter 和 Setter方法
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getFreeSpace() { return freeSpace; }
    public void setFreeSpace(Long freeSpace) { this.freeSpace = freeSpace; }

    public String getMountPoint() { return mountPoint; }
    public void setMountPoint(String mountPoint) { this.mountPoint = mountPoint; }

    public Long getTotalSpace() { return totalSpace; }
    public void setTotalSpace(Long totalSpace) { this.totalSpace = totalSpace; }

    public Long getUsedSpace() { return usedSpace; }
    public void setUsedSpace(Long usedSpace) { this.usedSpace = usedSpace; }

    public Double getUsagePercent() { return usagePercent; }
    public void setUsagePercent(Double usagePercent) { this.usagePercent = usagePercent; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}