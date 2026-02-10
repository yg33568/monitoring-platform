//系统指标实体
package com.monitor.monitoring_platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("system_metrics")
public class SystemMetrics {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 改为监控电脑组件
    private String componentName; // 组件名称 (CPU, Memory, Disk-C 等)
    private Double cpuUsage;      // CPU使用率
    private Double memUsage;      // 内存使用率
    private Long diskUsage;       // 磁盘使用量(GB)
    private Double networkRate;   // 网络速率(MB/s)
    private Integer processCount; // 进程数量
    private Integer responseTimeMS; // 进程数量
    private LocalDateTime timestamp;// 时间戳

    // 必须添加 getter 和 setter 方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getComponentName() { return componentName; }
    public void setComponentName(String componentName) { this.componentName = componentName; }

    public Double getCpuUsage() { return cpuUsage; }
    public void setCpuUsage(Double cpuUsage) { this.cpuUsage = cpuUsage; }

    public Double getMemUsage() { return memUsage; }
    public void setMemUsage(Double memUsage) { this.memUsage = memUsage; }

    public Long getDiskUsage() { return diskUsage; }
    public void setDiskUsage(Long diskUsage) { this.diskUsage = diskUsage; }

    public Double getNetworkRate() { return networkRate; }
    public void setNetworkRate(Double networkRate) { this.networkRate = networkRate; }

    public Integer getProcessCount() { return processCount; }
    public void setProcessCount(Integer processCount) { this.processCount = processCount; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Integer getResponseTimeMs() {
        return responseTimeMS;
    }
    public void setResponseTimeMS(Integer responseTimeMS){
        this.responseTimeMS=responseTimeMS;
    }
}