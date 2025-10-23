package ru.lessons.my.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.geojson.Feature;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.lessons.my.converter.GeoPointToFeatureConverter;
import ru.lessons.my.dto.PageResult;
import ru.lessons.my.model.entity.Enterprise;
import ru.lessons.my.model.entity.Manager;
import ru.lessons.my.model.entity.Trip;
import ru.lessons.my.model.entity.Vehicle;
import ru.lessons.my.model.entity.VehicleModel;
import ru.lessons.my.security.ManagerDetails;
import ru.lessons.my.security.SecurityUtils;
import ru.lessons.my.service.EnterpriseService;
import ru.lessons.my.service.GeoService;
import ru.lessons.my.service.VehicleModelService;
import ru.lessons.my.service.VehicleService;
import ru.lessons.my.util.DateTimeUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Controller
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    //todo Повысить безопасность добавив проверки на то, что у менеджера есть права на внесение изменений.
    private final VehicleService vehicleService;
    private final EnterpriseService enterpriseService;
    private final VehicleModelService modelService;
    private final GeoService geoService;
    private final SecurityUtils securityUtils;
    private final GeoPointToFeatureConverter toFeatureConverter;

    //Решил пока оставить этот метод, но сменить эндпоинт.
    //А вообще все автомобили получать нельзя, так что через некоторое время его следует убрать.
    @GetMapping("/all")
    public String findAllVehicles(Model model) {
        model.addAttribute("vehicles", vehicleService.findAll());
        return "vehicles/index";
    }

    @GetMapping
    public String findVehiclesByEnterprisePaged(@RequestParam(defaultValue = "1", name = "page") int page,
                                                @RequestParam(defaultValue = "10", name = "size") int size,
                                                @RequestParam(name = "enterpriseId") long enterpriseId,
                                                @AuthenticationPrincipal ManagerDetails managerDetails,
                                                Model model) {

        //todo Проверка на то, что у менеджера есть права на это предприятие.
        Enterprise enterprise = enterpriseService.findById(enterpriseId);
        PageResult<Vehicle> pagedVehicles = vehicleService.findByEnterprises(List.of(enterprise), page, size);

        ZoneId timeZone = managerDetails.getTimeZone().getId().toLowerCase().contains("utc")
                ? ZoneId.of(enterprise.getTimeZone())
                : managerDetails.getTimeZone();

        model.addAttribute("pagedVehicles", pagedVehicles);
        model.addAttribute("enterpriseId", enterpriseId);
        model.addAttribute("clientTimeZone", timeZone);
        return "vehicles/index";
    }

    @GetMapping("/{id}")
    public String findVehicleById(@PathVariable("id") long id,
                                  @AuthenticationPrincipal ManagerDetails managerDetails,
                                  @RequestParam(value = "dateFrom",
                                          defaultValue = "#{T(java.time.LocalDate).now().atStartOfDay()}")
                                      LocalDateTime dateFrom,
                                  @RequestParam(value = "dateTo",
                                          defaultValue = "#{T(java.time.LocalDateTime).now().truncatedTo(T(java.time.temporal.ChronoUnit).MINUTES)}")
                                      LocalDateTime dateTo,
                                  Model model) {

        Vehicle vehicle = vehicleService.findById(id);
        Enterprise enterprise = vehicle.getEnterprise();

        ZoneId timeZone = managerDetails.getTimeZone().getId().toLowerCase().contains("utc")
                ? ZoneId.of(enterprise.getTimeZone())
                : managerDetails.getTimeZone();

        List<Trip> trips = geoService.getTripsByVehicleIdAndTimeRange(id,
                DateTimeUtils.convertToUtc(dateFrom, timeZone),
                DateTimeUtils.convertToUtc(dateTo, timeZone));

        //Чтобы не тащить дополнительные зависимости для Jackson, конвертируем точки самостоятельно.
        List<Feature> tracks = geoService.getGeoPointsByTrips(trips).stream()
                .map(toFeatureConverter::convert)
                .toList();

        model.addAttribute("vehicle", vehicle);
        model.addAttribute("clientTimeZone", timeZone);
        model.addAttribute("trips", trips);
        model.addAttribute("tracks", tracks);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        return "vehicles/vehicle";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Manager manager = securityUtils.getCurrentManager();

        model.addAttribute("vehicle", new Vehicle());
        model.addAttribute("models", modelService.findAll());
        model.addAttribute("enterprises", enterpriseService.findByManager(manager));
        return "vehicles/new";
    }

    @PostMapping("/new")
    public String createVehicleModel(@Valid @ModelAttribute Vehicle vehicle,
                                     BindingResult bindingResult,
                                     @RequestParam("modelId") Long modelId,
                                     @RequestParam("enterpriseId") Long enterpriseId,
                                     @AuthenticationPrincipal ManagerDetails managerDetails) {
        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult.getAllErrors());
            return "vehicles/new";
        }
        VehicleModel model = modelService.findById(modelId);
        Enterprise enterprise = enterpriseService.findById(enterpriseId);
        vehicle.setModel(model);
        vehicle.setEnterprise(enterprise);
        vehicle.setPurchaseDateTime(DateTimeUtils.convertToUtc(vehicle.getPurchaseDateTime(), managerDetails.getTimeZone()));

        vehicleService.save(vehicle);
        return "redirect:/vehicles?enterpriseId=" + enterpriseId;
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") long id, Model model,
                               @AuthenticationPrincipal ManagerDetails managerDetails) {
        Manager manager = securityUtils.getCurrentManager();

        Vehicle vehicle = vehicleService.findById(id);
        vehicle.setPurchaseDateTime(DateTimeUtils.convertFromUtc(vehicle.getPurchaseDateTime(), managerDetails.getTimeZone()));
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("models", modelService.findAll());
        model.addAttribute("enterprises", enterpriseService.findByManager(manager));
        return "vehicles/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateVehicle(@PathVariable("id") long id,
                                @Valid @ModelAttribute Vehicle vehicle,
                                BindingResult bindingResult,
                                @RequestParam("modelId") Long modelId,
                                @RequestParam("enterpriseId") Long enterpriseId,
                                @AuthenticationPrincipal ManagerDetails managerDetails) {
        if (bindingResult.hasErrors()) {
            return "vehicles/edit";
        }

        VehicleModel model = modelService.findById(modelId);
        Enterprise enterprise = enterpriseService.findById(enterpriseId);
        vehicle.setModel(model);
        vehicle.setEnterprise(enterprise);
        vehicle.setId(id);

        vehicle.setPurchaseDateTime(DateTimeUtils.convertToUtc(vehicle.getPurchaseDateTime(), managerDetails.getTimeZone()));
        vehicleService.save(vehicle);
        return "redirect:/vehicles?enterpriseId=" + enterpriseId;
    }

    @GetMapping("/delete/{id}")
    public String deleteVehicle(@PathVariable("id") long id) {
        Vehicle vehicle = vehicleService.findById(id);
        vehicleService.deleteById(id);
        long enterpriseId = vehicle.getEnterprise().getId();
        return "redirect:/vehicles?enterpriseId=" + enterpriseId;
    }
}
