package ru.lessons.my.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EnterpriseDto {

    private Long id;
    private String name;
    private String city;
    private List<Long> vehicleIds;
    private List<Long> driverIds;
    private List<Long> managerIds;
}
