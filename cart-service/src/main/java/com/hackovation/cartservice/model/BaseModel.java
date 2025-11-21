package com.hackovation.cartservice.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
public abstract class BaseModel {

    @NotNull
    @Field("created_at")
    private Long createdAt;

    @NotNull
    @Field("created_time_zone")
    private String createdTimeZone;

    @NotNull
    @Field("updated_at")
    private Long updatedAt;

    @NotNull
    @Field("updated_time_zone")
    private String updatedTimeZone;


    public void beforeConvert() {
        long now = Instant.now().toEpochMilli();
        String zone = ZoneId.systemDefault().getId();

        this.createdAt = now;
        this.updatedAt = now;
        this.createdTimeZone = zone;
        this.updatedTimeZone = zone;
    }

    public void beforeSave() {
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
