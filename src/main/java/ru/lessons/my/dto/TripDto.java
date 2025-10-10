package ru.lessons.my.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class TripDto {

    private Long id;
    private long vehicleId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long startPointId;
    private Long endPointId;
    private String startAddress;
    private String endAddress;
}
