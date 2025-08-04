package com.hackovation.menu_service.model;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@MappedSuperclass
@Data
public abstract class BaseModel {

    @NotNull
    private Long createdAt;

    @NotNull
    private String createdTimeZone;

    @NotNull
    private Long updatedAt;

    @NotNull
    private String updatedTimeZone;

    @PrePersist
    public void prePersist() {
        long now = Instant.now().toEpochMilli();
        String zone = ZoneId.systemDefault().getId();

        this.createdAt = now;
        this.updatedAt = now;
        this.createdTimeZone = zone;
        this.updatedTimeZone = zone;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now().toEpochMilli();
        this.updatedTimeZone = ZoneId.systemDefault().getId();
    }

    public ZonedDateTime getCreatedZonedDateTime() {
        return Instant.ofEpochMilli(createdAt).atZone(ZoneId.of(createdTimeZone));
    }

    public ZonedDateTime getUpdatedZonedDateTime() {
        return Instant.ofEpochMilli(updatedAt).atZone(ZoneId.of(updatedTimeZone));
    }

}
