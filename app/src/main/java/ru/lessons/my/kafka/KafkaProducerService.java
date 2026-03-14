package ru.lessons.my.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.lessons.my.dto.NotificationInfo;
import ru.lessons.my.model.entity.Manager;
import ru.lessons.my.service.EnterpriseService;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final EnterpriseService enterpriseService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendNotification(String topic, String message, Long enterpriseId) {
        NotificationInfo notification = new NotificationInfo();
        notification.setMessage(message);
        notification.setManagerUsernames(
                enterpriseService.findById(enterpriseId).getManagers()
                        .stream()
                        .map(Manager::getUsername)
                        .collect(Collectors.toSet())
        );

        kafkaTemplate.send(topic, notification);
    }
}
