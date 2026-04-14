package com.monitor.monitoring_platform.entity;

import lombok.Data;
import java.util.List;

@Data
public class SpaceCategory {
    private String name;        // "系统文件"
    private String icon;
    private long size;          // 大小(GB)
    private double percent;     // 占比百分比
    private List<SpaceItem> items; // 子项列表

    // 手动添加setter方法
    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public void setItems(List<SpaceItem> items) {
        this.items = items;
    }

    // getter方法
    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public long getSize() {
        return size;
    }

    public double getPercent() {
        return percent;
    }

    public List<SpaceItem> getItems() {
        return items;
    }
}