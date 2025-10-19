package ru.lessons.my.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.lessons.my.dto.VehicleDto;
import ru.lessons.my.model.Driver;
import ru.lessons.my.model.Enterprise;
import ru.lessons.my.model.Vehicle;
import ru.lessons.my.model.VehicleModel;
import ru.lessons.my.service.DriverService;
import ru.lessons.my.service.EnterpriseService;
import ru.lessons.my.service.VehicleModelService;
import ru.lessons.my.util.DateTimeUtils;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VehicleDtoToVehicleConverter implements Converter<VehicleDto, Vehicle> {

    private final EnterpriseService enterpriseService;
    private final DriverService driverService;
    private final VehicleModelService modelService;

    @Override
    public Vehicle convert(VehicleDto source) {
        Enterprise enterprise = enterpriseService.findById(source.getEnterpriseId());
        VehicleModel model = modelService.findById(source.getModelId());

        Driver activeDriver = source.getActiveDriverId() != null
                ? driverService.findById(source.getActiveDriverId())
                : null;
        //Лучше отдельный запрос в БД, а то когда водителей станет много будем долго ждать.
        //Отрефакторить
        Set<Driver> drivers = source.getDriverIds() != null
                ? driverService.findAll().stream()
                    .filter(d -> source.getDriverIds().contains(d.getId()))
                    //Также пока будем тихо отфильтровывать водителей, которые не принадлежат нашему предприятию.
                    // Возможно стоит бросать исключение в таком случае. Подумать.
                    .filter(d -> enterprise.getId().equals(d.getEnterprise().getId()))
                    .collect(Collectors.toSet())
                : new HashSet<>();

        //Вдруг юзер передал водителя, которого нет в этом предприятии.
        if (activeDriver != null && !activeDriver.getEnterprise().getId().equals(enterprise.getId())) {
            throw new RuntimeException("Этого водителя нет на указанном предприятии");
        }

        ZoneId enterpriseTimeZone = ZoneId.of(enterprise.getTimeZone());

        return Vehicle.builder()
                .id(source.getId())
                .licensePlateNumber(source.getLicensePlateNumber())
                .mileageKm(source.getMileageKm())
                .productionYear(source.getProductionYear())
                .purchasePriceRub(source.getPurchasePriceRub())
                .purchaseDateTime(DateTimeUtils.convertToUtc(source.getPurchaseDateTime(), enterpriseTimeZone))
                .color(source.getColor())
                .enterprise(enterprise)
                .model(model)
                .activeDriver(activeDriver)
                .drivers(drivers)
                .build();
    }
}
