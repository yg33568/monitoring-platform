package com.monitor.monitoring_platform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AiChatService {

    @Autowired
    private AiSmartService aiSmartService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String REDIS_HISTORY_KEY = "chat:history:";  // Redis 中存历史的 key 前缀
    private static final int EXPIRE_HOURS = 24;  // 保存24小时

    //加入历史记录
    public String chat(String sessionId, String userMessage, Map<String, Object> metrics) {
        // 构建系统上下文
        String systemContext = buildSystemContext(metrics);
        // 从 Redis 获取历史对话
        String history = getHistoryFromRedis(sessionId);

        String prompt = String.format(
                "%s\n\n【历史对话】\n%s\n\n【用户问题】\n%s\n\n【回复要求】\n" +
                        "1. 如果用户问的是电脑/系统/监控相关的问题，结合【系统状态】回答\n" +
                        "2. 如果用户问的是闲聊（你好、天气、新闻等），正常聊天，不要说电脑的事\n" +
                        "3. 不要主动提系统状态，除非用户问\n" +
                        "4. 用自然、流畅的中文回复",
                systemContext, history, userMessage
        );

        // 调用 AI
        String aiResponse = aiSmartService.askAi(prompt);
        // 保存到 Redis
        saveToRedis(sessionId, userMessage, aiResponse);
        return aiResponse;
    }

    //每次重新对话，不加入历史记录
    public String getSystemDiagnosis(String sessionId, Map<String, Object> metrics) {
        String systemContext = buildSystemContext(metrics);
        String prompt = String.format(
                "%s\n\n请对当前系统进行全面诊断，包括：\n" +
                        "1. 系统健康度评估（给出分数0-100）\n" +
                        "2. 发现的问题和风险\n" +
                        "3. 具体的优化建议\n\n" +
                        "请用简洁专业的语言回复。",
                systemContext
        );

        return aiSmartService.askAi(prompt);
    }

    // 从 Redis 获取历史
    private String getHistoryFromRedis(String sessionId) {
        String key = REDIS_HISTORY_KEY + sessionId;
        String history = redisTemplate.opsForValue().get(key);
        if (history == null || history.isEmpty()) {
            return "（这是我们的第一次对话）";
        }
        return history;
    }

    // 保存到 Redis
    private void saveToRedis(String sessionId, String userMsg, String aiMsg) {
        String key = REDIS_HISTORY_KEY + sessionId;

        // 获取现有历史
        String existingHistory = redisTemplate.opsForValue().get(key);
        StringBuilder newHistory = new StringBuilder();

        if (existingHistory != null && !existingHistory.isEmpty()) {
            newHistory.append(existingHistory);
        }

        // 追加新对话
        newHistory.append("用户：").append(userMsg).append("\n");
        newHistory.append("助手：").append(aiMsg).append("\n\n");

        // 限制长度（防止太长）
        String historyStr = newHistory.toString();
        if (historyStr.length() > 8000) {
            historyStr = historyStr.substring(historyStr.length() - 6000);
        }

        // 存入 Redis，24小时过期
        redisTemplate.opsForValue().set(key, historyStr, EXPIRE_HOURS, TimeUnit.HOURS);
    }

    private String buildSystemContext(Map<String, Object> metrics) {
        if (metrics == null) {
            return "【系统状态】当前无法获取实时监控数据。";
        }

        Double cpu = 0.0;
        Double memory = 0.0;

        try {
            if (metrics.get("cpu") != null) {
                cpu = Double.valueOf(metrics.get("cpu").toString());
            }
            if (metrics.get("memory") != null) {
                memory = Double.valueOf(metrics.get("memory").toString());
            }
        } catch (Exception e) {}

        // 构建磁盘信息
        StringBuilder diskInfo = new StringBuilder();
        Object disksObj = metrics.get("disks");
        if (disksObj instanceof List) {
            List<?> disks = (List<?>) disksObj;
            for (Object disk : disks) {
                if (disk instanceof Map) {
                    Map<?, ?> diskMap = (Map<?, ?>) disk;
                    String mountPoint = diskMap.get("mountPoint").toString();
                    Object total = diskMap.get("totalSpace");
                    Object used = diskMap.get("usedSpace");
                    Object free = diskMap.get("freeSpace");
                    Object percent = diskMap.get("usagePercent");

                    if (total != null && used != null) {
                        diskInfo.append(String.format("\n  %s: %dGB/%dGB (%.1f%%)，剩余 %dGB",
                                mountPoint,
                                ((Number) used).longValue(),
                                ((Number) total).longValue(),
                                ((Number) percent).doubleValue(),
                                ((Number) free).longValue()));
                    }
                }
            }
        }

        String cpuLevel = cpu > 80 ? "过高" : (cpu > 60 ? "偏高" : "正常");
        String memLevel = memory > 85 ? "过高" : (memory > 70 ? "偏高" : "正常");

        return String.format(
                "【当前系统状态】\nCPU: %.1f%% (%s) | 内存: %.1f%% (%s)\n【磁盘状态】%s",
                cpu, cpuLevel, memory, memLevel, diskInfo.toString()
        );
    }
}