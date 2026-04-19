package com.monitor.monitoring_platform.controller;

import com.monitor.monitoring_platform.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")

//接收前端发来的请求，调用 AI 服务，然后把结果返回给前端。

public class AiChatController {

    @Autowired
    private AiChatService aiChatService;

    /**
     * AI 对话接口
     * 用户在前端输入问题，前端会发请求到这里。
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> request) {
        // 创建一个空盒子，用来装返回给前端的数据
        Map<String, Object> response = new HashMap<>();

        try {
            // 拿到会话ID（用来区分不同用户）
            String sessionId = (String) request.get("sessionId");
            String message = (String) request.get("message");
            Map<String, Object> metrics = (Map<String, Object>) request.get("metrics");

            String reply = aiChatService.chat(sessionId, message, metrics);
            //success/reply是前端的参数名
            response.put("success", true);
            response.put("reply", reply);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    /**
     * 系统诊断接口
     * 和 /chat 类似，只是调用了 getSystemDiagnosis 方法，专门做系统全面诊断。
     * @RequestBody 把前端传过来的 JSON 数据，自动转换成 Java 对象。
     */
    @PostMapping("/diagnose")
    public Map<String, Object> diagnose(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String sessionId = (String) request.get("sessionId");
            Map<String, Object> metrics = (Map<String, Object>) request.get("metrics");

            String diagnosis = aiChatService.getSystemDiagnosis(sessionId, metrics);

            response.put("success", true);
            response.put("reply", diagnosis);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }
}
