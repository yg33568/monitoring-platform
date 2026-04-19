package com.monitor.monitoring_platform.entity;
/**
 * DiskSpaceAnalysis (整份报告)
 *     └── SpaceCategory (分类：系统文件)
 *             └── SpaceItem (子项：Windows系统)  ← 就是这个
 *             └── SpaceItem (子项：Program Files)
 *             └── SpaceItem (子项：系统缓存)
 *             但是好像未被使用
 */

import lombok.Data;

@Data
public class SpaceItem {
    private String name;        // "Windows系统"
    private String path;        // "Windows"
    private long size;          // 大小(GB)
    private String description; // 描述信息

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