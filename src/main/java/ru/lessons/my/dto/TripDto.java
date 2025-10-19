package ru.lessons.my.dto;

import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TripDto {

    private Long id;
    private long vehicleId;
    @CsvDate("MM/dd/yyyy HH:mm:ss")
    private LocalDateTime startTime;
    @CsvDate("MM/dd/yyyy HH:mm:ss")
    private LocalDateTime endTime;
    private Long startPointId;
    private Long endPointId;
    private String startAddress;
    private String endAddress;
}
