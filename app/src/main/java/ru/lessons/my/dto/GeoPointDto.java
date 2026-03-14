package ru.lessons.my.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoPointDto {

    private long vehicleId;

    private long tripId;

    private double longitude;

    private double latitude;

    @JsonFormat
    private LocalDateTime visitedAt;
}
