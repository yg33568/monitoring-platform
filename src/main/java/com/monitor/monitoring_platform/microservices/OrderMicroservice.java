package com.monitor.monitoring_platform.microservices;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/microservice/order")
public class OrderMicroservice {

    private List<Map<String, Object>> orders = new ArrayList<>();
    private int orderIdCounter = 1;
    private int requestCount = 0;

    public OrderMicroservice() {
        // 初始化一些模拟订单
        initializeSampleOrders();
    }

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> orderRequest) {
        requestCount++;

        Map<String, Object> order = new HashMap<>();
        order.put("id", orderIdCounter++);
        order.put("userId", orderRequest.get("userId"));
        order.put("productId", orderRequest.get("productId"));
        order.put("quantity", orderRequest.get("quantity"));
        order.put("totalAmount", Math.round((Math.random() * 1000 + 10) * 100) / 100.0);
        order.put("status", "PENDING");
        order.put("createdAt", new Date());
        order.put("orderNumber", "ORD" + System.currentTimeMillis());

        orders.add(order);

        // 模拟订单处理
        simulateProcessing(100);

        return Map.of(
                "success", true,
                "order", order,
                "message", "订单创建成功",
                "responseTime", (50 + Math.random() * 100) + "ms"
        );
    }

    /**
     * 获取订单列表
     */
    @GetMapping("/list")
    public Map<String, Object> getOrders(@RequestParam(defaultValue = "10") int limit) {
        requestCount++;
        simulateProcessing(50);

        return Map.of(
                "orders", orders.stream().limit(limit).toArray(),
                "total", orders.size(),
                "responseTime", (30 + Math.random() * 70) + "ms"
        );
    }

    /**
     * 订单详情
     */
    @GetMapping("/{orderId}")
    public Map<String, Object> getOrderDetail(@PathVariable String orderId) {
        requestCount++;
        simulateProcessing(40);

        Map<String, Object> order = orders.stream()
                .filter(o -> o.get("id").toString().equals(orderId))
                .findFirst()
                .orElse(Map.of("error", "订单不存在"));

        return Map.of(
                "order", order,
                "responseTime", (20 + Math.random() * 50) + "ms"
        );
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "service", "order-microservice",
                "status", "UP",
                "timestamp", System.currentTimeMillis(),
                "metrics", Map.of(
                        "totalOrders", orders.size(),
                        "totalRequests", requestCount,
                        "memoryUsage", (65 + Math.random() * 20) + "%",
                        "cpuUsage", (50 + Math.random() * 25) + "%"
                )
        );
    }

    /**
     * 性能指标
     */
    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        return Map.of(
                "service", "order-service",
                "cpuUsage", 55 + Math.random() * 25,  // 55-80%
                "memoryUsage", 70 + Math.random() * 20, // 70-90%
                "responseTime", 120 + Math.random() * 150, // 120-270ms
                "errorRate", Math.random() * 3, // 0-3%
                "throughput", 150 + Math.random() * 300, // 150-450 req/min
                "timestamp", System.currentTimeMillis()
        );
    }

    private void initializeSampleOrders() {
        for (int i = 0; i < 5; i++) {
            Map<String, Object> order = new HashMap<>();
            order.put("id", orderIdCounter++);
            order.put("userId", "user_" + (i + 1));
            order.put("productId", "product_" + (i + 1));
            order.put("quantity", i + 1);
            order.put("totalAmount", (i + 1) * 99.99);
            order.put("status", i % 2 == 0 ? "COMPLETED" : "PENDING");
            order.put("createdAt", new Date(System.currentTimeMillis() - i * 3600000L));
            order.put("orderNumber", "ORD" + (System.currentTimeMillis() - i));
            orders.add(order);
        }
    }

    private void simulateProcessing(int maxDelay) {
        try {
            Thread.sleep((long) (Math.random() * maxDelay));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}