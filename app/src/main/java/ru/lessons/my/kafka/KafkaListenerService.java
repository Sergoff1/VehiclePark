package ru.lessons.my.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.lessons.my.dto.LoginRequestDto;
import ru.lessons.my.dto.LoginResponse;
import ru.lessons.my.dto.ReportRequestDto;
import ru.lessons.my.dto.ReportResponse;
import ru.lessons.my.model.Report;
import ru.lessons.my.model.ReportType;
import ru.lessons.my.service.EnterpriseService;
import ru.lessons.my.service.ManagerService;
import ru.lessons.my.service.ReportService;
import ru.lessons.my.service.VehicleService;

@Service
@RequiredArgsConstructor
public class KafkaListenerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final EnterpriseService enterpriseService;
    private final ManagerService managerService;
    private final VehicleService vehicleService;
    private final ReportService reportService;
    private final PasswordEncoder passwordEncoder;

    @KafkaListener(topics = "loginRequests")
    public void listenLoginRequestsTopic(LoginRequestDto requestDto) {
        LoginResponse response;
        try {
            boolean success = passwordEncoder.matches(
                    requestDto.password(),
                    managerService.getManagerByUsername(requestDto.username()).getPassword());

            response = new LoginResponse(requestDto.chatId(), success);
        } catch (Exception e) {
            response = new LoginResponse(requestDto.chatId(), false);
        }

        kafkaTemplate.send("loginResponses", response);
    }

    @KafkaListener(topics = "reportRequests")
    public void listenReportRequestsTopic(ReportRequestDto requestDto) {
        ReportResponse response = new ReportResponse();
        response.setChatId(requestDto.getChatId());

        try {
            Long entityId;
            if (requestDto.getReportType() == ReportType.VEHICLE_MILEAGE) {
                entityId = vehicleService.getByLicensePlateNumber(requestDto.getEntityId()).getId();
            } else {
                entityId = enterpriseService.getByName(requestDto.getEntityId()).getId();
            }

            Report report = reportService.getReport(requestDto.getReportType(),
                    entityId,
                    entityId,
                    requestDto.getReportPeriod(),
                    requestDto.getStartDate(),
                    requestDto.getEndDate());

            response.setType(report.getType());
            response.setPeriod(report.getPeriod());
            response.setValues(report.getValues());
            response.setStartDate(report.getStartDate());
            response.setEndDate(report.getEndDate());
            response.setSuccess(true);
        } catch (Exception e) {
            response.setSuccess(false);
        }

        kafkaTemplate.send("reportResponses", response);
    }

}
