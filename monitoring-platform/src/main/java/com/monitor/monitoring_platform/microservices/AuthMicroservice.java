package com.monitor.monitoring_platform.microservices;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/microservice/auth")
public class AuthMicroservice {

    // 模拟用户数据库
    private Map<String, String> userDatabase = new HashMap<>();
    private int requestCount = 0;
    private long totalResponseTime = 0;

    public AuthMicroservice() {
        // 初始化测试用户
        userDatabase.put("admin", "admin123");
        userDatabase.put("user", "user123");
        userDatabase.put("test", "test123");
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginRequest) {
        long startTime = System.currentTimeMillis();
        requestCount++;

        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        Map<String, Object> response = new HashMap<>();

        // 模拟处理时间
        simulateProcessing(50);

        if (userDatabase.containsKey(username) && userDatabase.get(username).equals(password)) {
            response.put("success", true);
            response.put("token", UUID.randomUUID().toString());
            response.put("message", "登录成功");
            response.put("user", Map.of(
                    "username", username,
                    "role", "admin".equals(username) ? "ADMIN" : "USER",
                    "loginTime", System.currentTimeMillis()
            ));
        } else {
            response.put("success", false);
            response.put("message", "用户名或密码错误");
        }

        long responseTime = System.currentTimeMillis() - startTime;
        totalResponseTime += responseTime;
        response.put("responseTime", responseTime + "ms");

        return response;
    }

    /**
     * 用户信息查询
     */
    @GetMapping("/users/{id}")
    public Map<String, Object> getUserInfo(@PathVariable String id) {
        long startTime = System.currentTimeMillis();
        requestCount++;

        // 模拟处理时间
        simulateProcessing(30);

        Map<String, Object> userInfo = Map.of(
                "id", id,
                "username", "user_" + id,
                "email", "user" + id + "@example.com",
                "createdAt", "2024-01-01",
                "status", "ACTIVE",
                "lastLogin", System.currentTimeMillis() - 1000 * 60 * 30 // 30分钟前
        );

        long responseTime = System.currentTimeMillis() - startTime;
        totalResponseTime += responseTime;

        userInfo = new HashMap<>(userInfo);
        userInfo.put("responseTime", responseTime + "ms");
        return userInfo;
    }

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        double avgResponseTime = requestCount > 0 ? (double) totalResponseTime / requestCount : 0;

        return Map.of(
                "service", "auth-microservice",
                "status", "UP",
                "timestamp", System.currentTimeMillis(),
                "metrics", Map.of(
                        "activeUsers", userDatabase.size(),
                        "totalRequests", requestCount,
                        "averageResponseTime", String.format("%.2fms", avgResponseTime),
                        "memoryUsage", (70 + Math.random() * 20) + "%",  // 模拟内存使用
                        "cpuUsage", (40 + Math.random() * 30) + "%"      // 模拟CPU使用
                )
        );
    }

    /**
     * 模拟服务性能指标（用于监控演示）
     */
    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        return Map.of(
                "service", "auth-service",
                "cpuUsage", 45 + Math.random() * 20,  // 45-65%
                "memoryUsage", 60 + Math.random() * 25, // 60-85%
                "responseTime", 80 + Math.random() * 100, // 80-180ms
                "errorRate", Math.random() * 5, // 0-5%
                "throughput", 100 + Math.random() * 200, // 100-300 req/min
                "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * 模拟处理延迟
     */
    private void simulateProcessing(int maxDelay) {
        try {
            Thread.sleep((long) (Math.random() * maxDelay));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}