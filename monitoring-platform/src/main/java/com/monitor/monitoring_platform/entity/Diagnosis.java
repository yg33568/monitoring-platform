package com.monitor.monitoring_platform.entity;

public class Diagnosis {
    private String type;        // 问题类型
    private Integer confidence; // 置信度 0-100
    private String evidence;    // 分析依据

    public Diagnosis() {
    }

    public Diagnosis(String type, Integer confidence, String evidence) {
        this.type = type;
        this.confidence = confidence;
        this.evidence = evidence;
    }

    // 手动添加getter和setter
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getConfidence() {
        return confidence;
    }

    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    @Override
    public String toString() {
        return "Diagnosis{" +
                "type='" + type + '\'' +
                ", confidence=" + confidence +
                ", evidence='" + evidence + '\'' +
                '}';
    }
}