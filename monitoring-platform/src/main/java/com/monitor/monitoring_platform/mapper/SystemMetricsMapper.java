package com.monitor.monitoring_platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monitor.monitoring_platform.entity.SystemMetrics;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SystemMetricsMapper extends BaseMapper<SystemMetrics> {

    // 查询组件的最新数据
    @Select("SELECT * FROM system_metrics WHERE component_name = #{componentName} ORDER BY timestamp DESC LIMIT 1")
    SystemMetrics selectLatestByComponentName(@Param("componentName") String componentName);

    // 查询时间范围内的数据
    @Select("SELECT * FROM system_metrics WHERE timestamp BETWEEN #{startTime} AND #{endTime} ORDER BY timestamp DESC")
    List<SystemMetrics> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    // 查询所有组件的最新指标
    @Select("SELECT m1.* FROM system_metrics m1 " +
            "INNER JOIN ( " +
            "    SELECT component_name, MAX(timestamp) as max_timestamp " +
            "    FROM system_metrics " +
            "    GROUP BY component_name " +
            ") m2 ON m1.component_name = m2.component_name AND m1.timestamp = m2.max_timestamp")
    List<SystemMetrics> selectLatestMetricsForAllComponents();

    /**
     * 查询最近的磁盘数据（用于拓扑图）
     */
    @Select("SELECT DISTINCT component_name, disk_usage, timestamp " +
            "FROM system_metrics " +
            "WHERE component_name LIKE 'Disk-%' " +
            "AND timestamp >= DATE_SUB(NOW(), INTERVAL 1 HOUR) " +
            "ORDER BY timestamp DESC")
    List<SystemMetrics> selectRecentDisks();

    // 添加获取最近数据的方法
    @Select("SELECT * FROM system_metrics ORDER BY timestamp DESC LIMIT #{count}")
    List<SystemMetrics> selectRecentMetrics(int count);
}