package com.monitor.monitoring_platform.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import jakarta.annotation.Resource;

@Component
public class AiSmartService {

    @Resource
    private ChatClient.Builder chatClientBuilder;

    /**
     * 问 AI 任何问题，返回清理后的答案
     */
    public String askAi(String prompt) {
        try {
            ChatClient chatClient = chatClientBuilder.build();
            String rawResponse = chatClient.prompt()
                    .system("你是一个专业的、热心的智能助手，你的名字叫飞飞，要以飞飞的身份和语气回答问题。")
                    .user(prompt)
                    .call()
                    .content();
            return cleanMarkdown(rawResponse);
        } catch (Exception e) {
            return "AI服务暂时不可用，请稍后再试。";
        }
    }

    /**
     * 清理 Markdown 符号，让回复更自然
     */
    private String cleanMarkdown(String text) {
        if (text == null) return "";

        text = text.replaceAll("(?m)^#{1,6}\\s+", "");
        text = text.replaceAll("\\*\\*([^*]+)\\*\\*", "$1");
        text = text.replaceAll("\\*([^*]+)\\*", "$1");
        text = text.replaceAll("(?m)^\\s*-\\s+", "• ");
        text = text.replaceAll("(?m)^\\s*\\d+\\.\\s+", "");
        text = text.replaceAll("```\\w*\\n?", "");
        text = text.replaceAll("```\\n?", "");

        return text.trim();
    }
}