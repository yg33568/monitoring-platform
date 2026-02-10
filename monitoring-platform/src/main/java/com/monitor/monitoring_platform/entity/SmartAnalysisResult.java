package com.monitor.monitoring_platform.entity;

import java.util.Date;
import java.util.List;

public class SmartAnalysisResult {
    private List<Diagnosis> diagnoses;
    private Date analysisTime;

    public SmartAnalysisResult() {
    }

    public SmartAnalysisResult(List<Diagnosis> diagnoses, Date analysisTime) {
        this.diagnoses = diagnoses;
        this.analysisTime = analysisTime;
    }

    // 手动添加 getter 方法
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
}