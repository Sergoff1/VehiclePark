package ru.lessons.my.dto;

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
    private List<Long> vehicleIds;
    private List<Long> driverIds;
    private List<Long> managerIds;
}
