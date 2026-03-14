package ru.lessons.my.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import ru.lessons.my.model.entity.Driver;
import ru.lessons.my.model.entity.Enterprise;
import ru.lessons.my.model.entity.Vehicle;
import ru.lessons.my.model.entity.VehicleModel;
import ru.lessons.my.repository.DriverRepository;
import ru.lessons.my.repository.EnterpriseRepository;
import ru.lessons.my.repository.VehicleModelRepository;
import ru.lessons.my.repository.VehicleRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Command
@Slf4j
@RequiredArgsConstructor
public class VehicleGenerator {

    private final EnterpriseRepository enterpriseRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final VehicleModelRepository modelRepository;
    private final Random rand = new Random();

    @Command(description = "Fill DB with random vehicles and drivers(if necessary)")
    public String generateVehicles(@Option(arity = CommandRegistration.OptionArity.ONE_OR_MORE, shortNames = 'e', longNames = "enterprises", required = true) long[] enterpriseIds,
                                   @Option(defaultValue = "50", longNames = "vehicleNumber", shortNames = 'n') int vehicleNumber) {

        List<VehicleModel> models = modelRepository.findAll();
        List<String> colors = List.of(
                "Черный",
                "Blue",
                "Белый",
                "Green",
                "Silver",
                "Мокрый асфальт",
                "Красный",
                "Orange",
                "Yellow"
        );

        for (long enterpriseId : enterpriseIds) {
            Optional<Enterprise> enterpriseOpt = enterpriseRepository.findById(enterpriseId);
            if (enterpriseOpt.isEmpty()) {
                log.info("Enterprise with id {} not found. Skip filling it.", enterpriseId);
                continue;
            }

            List<Driver> drivers = generateDrivers((vehicleNumber / 10) + 1, enterpriseOpt.get());
            drivers.forEach(driverRepository::save);

            //todo Избавиться от магических чисел и улучшить генерацию номеров

            int activeDriversCount = 0;
            for (int i = 0; i < vehicleNumber; i++) {
                Driver activeDriver = activeDriversCount < drivers.size() && rand.nextInt(10) == 9
                        ? drivers.get(activeDriversCount)
                        : null;
                if (activeDriver != null) {
                    activeDriversCount++;
                }

                Set<Driver> driversSet = drivers.stream()
                        .limit(rand.nextInt(drivers.size()))
                        .collect(Collectors.toSet());

                Vehicle vehicle = Vehicle.builder()
                        .mileageKm(rand.nextInt(500000))
                        .color(colors.get(rand.nextInt(colors.size())))
                        .licensePlateNumber(String.valueOf(System.nanoTime()))
                        .productionYear(rand.nextInt(1900, 2026))
                        .purchasePriceRub(rand.nextInt(500000, 20000000))
                        .purchaseDateTime(LocalDateTime.now())
                        .enterprise(enterpriseOpt.get())
                        .model(models.get(rand.nextInt(models.size())))
                        .drivers(driversSet)
                        .activeDriver(activeDriver)
                        .build();

                vehicleRepository.save(vehicle);
            }
        }

        return String.format("%d vehicles created for %d enterprises", vehicleNumber * enterpriseIds.length,
                enterpriseIds.length);
    }

    private List<Driver> generateDrivers(long driversNumber, Enterprise enterprise) {
        List<Driver> drivers = new ArrayList<>();
        for (int i = 0; i < driversNumber; i++) {
            drivers.add(Driver.builder()
                            .name("driver" + i)
                            .salaryRub((double) rand.nextInt(25000,200001))
                            .enterprise(enterprise)
                    .build());
        }
        return drivers;
    }
}
