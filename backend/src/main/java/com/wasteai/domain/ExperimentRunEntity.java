package com.wasteai.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "experiment_runs")
public class ExperimentRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private ImageEntity image;

    @Column(nullable = false)
    private String modelVersion;

    @Column(nullable = false)
    private Integer imgsz;

    @Column(nullable = false)
    private Float conf;

    @Column(nullable = false)
    private Float iou;

    @Column(nullable = false)
    private Long latencyMs;

    @Column(nullable = false)
    private Integer boxCount;

    @Column(nullable = false)
    private Double avgConfidence;

    @Column(length = 128)
    private String topClass;

    @Column(nullable = false)
    private Float minConfidenceFilter;

    @Column(length = 128)
    private String classKeywordFilter;

    @Column(length = 500)
    private String note;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public ImageEntity getImage() {
        return image;
    }

    public void setImage(ImageEntity image) {
        this.image = image;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public Integer getImgsz() {
        return imgsz;
    }

    public void setImgsz(Integer imgsz) {
        this.imgsz = imgsz;
    }

    public Float getConf() {
        return conf;
    }

    public void setConf(Float conf) {
        this.conf = conf;
    }

    public Float getIou() {
        return iou;
    }

    public void setIou(Float iou) {
        this.iou = iou;
    }

    public Long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public Integer getBoxCount() {
        return boxCount;
    }

    public void setBoxCount(Integer boxCount) {
        this.boxCount = boxCount;
    }

    public Double getAvgConfidence() {
        return avgConfidence;
    }

    public void setAvgConfidence(Double avgConfidence) {
        this.avgConfidence = avgConfidence;
    }

    public String getTopClass() {
        return topClass;
    }

    public void setTopClass(String topClass) {
        this.topClass = topClass;
    }

    public Float getMinConfidenceFilter() {
        return minConfidenceFilter;
    }

    public void setMinConfidenceFilter(Float minConfidenceFilter) {
        this.minConfidenceFilter = minConfidenceFilter;
    }

    public String getClassKeywordFilter() {
        return classKeywordFilter;
    }

    public void setClassKeywordFilter(String classKeywordFilter) {
        this.classKeywordFilter = classKeywordFilter;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
