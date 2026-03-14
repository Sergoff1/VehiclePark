package ru.lessons.my.dto;

import com.opencsv.bean.CsvIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnterpriseDto {

    private Long id;
    private String name;
    private String city;
    @CsvIgnore
    private List<Long> vehicleIds;
    @CsvIgnore
    private List<Long> driverIds;
    @CsvIgnore
    private List<Long> managerIds;
}
