package com.wasteai.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "detections")
public class DetectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image_id", nullable = false)
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
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "detection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetectionItemEntity> items = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<DetectionItemEntity> getItems() {
        return items;
    }

    public void setItems(List<DetectionItemEntity> items) {
        this.items = items;
    }
}
