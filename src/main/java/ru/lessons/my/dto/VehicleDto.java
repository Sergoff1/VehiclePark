package ru.lessons.my.dto;

import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDto {

    private Long id;
    private long modelId;
    private Long enterpriseId;
    private Long activeDriverId;
    private String licensePlateNumber;
    private int productionYear;
    private int mileageKm;
    private String color;
    private int purchasePriceRub;
    @CsvDate("MM/dd/yyyy HH:mm:ss")
    private LocalDateTime purchaseDateTime;
    @CsvIgnore
    private List<Long> driverIds;
}
