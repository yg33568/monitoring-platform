package com.monitor.monitoring_platform.entity;

import java.util.Date;
import java.util.List;

//存放ai根因分析结果的

public class SmartAnalysisResult {
    private List<Diagnosis> diagnoses;
    private Date analysisTime;
    private String aiAnalysis;

    public SmartAnalysisResult() {
    }

    public SmartAnalysisResult(List<Diagnosis> diagnoses, Date analysisTime) {
        this.diagnoses = diagnoses;
        this.analysisTime = analysisTime;
    }

    // getter/setter
    public List<Diagnosis> getDiagnoses() {
        return diagnoses;
    }

    public void setDiagnoses(List<Diagnosis> diagnoses) {
        this.diagnoses = diagnoses;
    }

    public Date getAnalysisTime() {
        return analysisTime;
    }

    public void setAnalysisTime(Date analysisTime) {
        this.analysisTime = analysisTime;
    }

    public String getAiAnalysis() {
        return aiAnalysis;
    }

    public void setAiAnalysis(String aiAnalysis) {
        this.aiAnalysis = aiAnalysis;
    }
}