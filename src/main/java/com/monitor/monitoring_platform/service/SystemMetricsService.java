package com.monitor.monitoring_platform.service;

import com.monitor.monitoring_platform.entity.SystemMetrics;
import com.monitor.monitoring_platform.mapper.SystemMetricsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemMetricsService {

    @Autowired
    private SystemMetricsMapper systemMetricsMapper;

    public List<SystemMetrics> getRecentMetrics(int count) {
        // 获取最近的数据点，这里假设您有相应的方法
        // 如果还没有，可以先返回模拟数据用于测试
        return getRecentMetricsFromDB(count);
    }

    private List<SystemMetrics> getRecentMetricsFromDB(int count) {
        // 这里实现从数据库获取最近的数据
        // 暂时可以先返回空列表或模拟数据
        return systemMetricsMapper.selectRecentMetrics(count);
    }
}