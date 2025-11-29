package com.xzd.motherboardguider.bean;

import java.util.List;

public class CPUBean {
    private String brandId;
    private String seriesId;
    private String modelId;

    private String cpuName;
    private List<String> supportedMotherboards;
    private List<String> recommendedMotherboards;

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(String seriesId) {
        this.seriesId = seriesId;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public List<String> getSupportedMotherboards() {
        return supportedMotherboards;
    }

    public void setSupportedMotherboards(List<String> supportedMotherboards) {
        this.supportedMotherboards = supportedMotherboards;
    }

    public List<String> getRecommendedMotherboards() {
        return recommendedMotherboards;
    }

    public void setRecommendedMotherboards(List<String> recommendedMotherboards) {
        this.recommendedMotherboards = recommendedMotherboards;
    }

    public String getCpuName() {
        return cpuName;
    }

    public void setCpuName(String cpuName) {
        this.cpuName = cpuName;
    }
}
