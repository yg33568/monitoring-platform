package com.monitor.monitoring_platform.entity;

import lombok.Data;

@Data
public class SpaceItem {
    private String name;        // "Windows系统"
    private String path;        // "Windows" (实际路径)
    private long size;          // 大小(GB)
    private String description; // 描述信息

    // 手动添加setter方法
    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // getter方法
    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public String getDescription() {
        return description;
    }
}