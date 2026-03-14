package ru.lessons.my.bot;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.lessons.my.dto.LoginRequestDto;
import ru.lessons.my.dto.LoginResponse;
import ru.lessons.my.dto.NotificationInfo;
import ru.lessons.my.dto.ReportPeriod;
import ru.lessons.my.dto.ReportRequestDto;
import ru.lessons.my.dto.ReportResponse;
import ru.lessons.my.dto.ReportType;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//todo Тут бардак, многое можно рефакторить, но это будет потом.
// Пока просто делаю нечто рабочее и проверяю идеи
@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Map<String, Long> authorizedUsers = new ConcurrentHashMap<>();
    private final Map<Long, LoginStates> loginStatesMap = new ConcurrentHashMap<>();
    private final Map<Long, String> usersMap = new ConcurrentHashMap<>();
    private final Map<Long, ReportRequestDto> reportParametersMap = new ConcurrentHashMap<>();
    private final Map<Long, ReportStates> reportStatesMap = new ConcurrentHashMap<>();

    @Value("${bot.tg.name}")
    private String botName;

    @Autowired
    public TelegramBot(@Value("${bot.tg.token}") String botToken,
                       KafkaTemplate<String, Object> kafkaTemplate) {
        super(botToken);
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    @SneakyThrows
    public void registerCommands() {
        execute(SetMyCommands.builder()
                .command(new BotCommand("/start", "Начать работу с ботом"))
                .command(new BotCommand("/login", "Авторизация"))
                .command(new BotCommand("/report_response", "Сформировать отчёт"))
                .command(new BotCommand("/logout", "Выход из системы"))
                .command(new BotCommand("/help", "Список доступных команд"))
                .build());
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(!update.hasMessage() && !update.getMessage().hasText()) {
            return;
        }

        switch (update.getMessage().getText()) {
            case "/start" -> handleStartCommand(update.getMessage());
            case "/login" -> handleLoginCommand(update.getMessage());
            case "/logout" -> handleLogoutCommand(update.getMessage());
            case "/report_response" -> handleReportCommand(update.getMessage());
            case "/help" -> handleHelpCommand(update.getMessage());
            default -> handleText(update.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    private void handleStartCommand(Message message) {
        sendMessage(message.getChatId(),
                """
                        Здравствуйте! Я бот автопарка VehiclePark.
                        Для начала работы вам нужно авторизоваться.
                        Чтобы это сделать используйте команду /login""");
    }

    private void handleLogoutCommand(Message message) {
        authorizedUsers.remove(usersMap.get(message.getChatId()));
        loginStatesMap.remove(message.getChatId());
        reportStatesMap.remove(message.getChatId());
        usersMap.remove(message.getChatId());
        sendMessage(message.getChatId(), "Вы вышли из системы.");
    }

    private void handleHelpCommand(Message message) {
        sendMessage(message.getChatId(),
                """
                        Для начала работы необходимо авторизоваться с помощью команды /login .
                        Для выхода из системы используйте команду /logout .
                        Для запроса отчёта по пробегу автомобилей используйте команду /report_response .""");
    }

    private void handleLoginCommand(Message message) {
        if (usersMap.get(message.getChatId()) != null && authorizedUsers.containsKey(usersMap.get(message.getChatId()))) {
            sendMessage(message.getChatId(),
                    "Вы уже авторизованы как " + usersMap.get(message.getChatId()) +
                    ". Если вы хотите войти под другой учётной записью сначала выйдите из системы с помощью команды /logout");
            return;
        }
        switch (loginStatesMap.get(message.getChatId())) {
            case null -> {
                loginStatesMap.put(message.getChatId(), LoginStates.AWAITING_USERNAME);
                sendMessage(message.getChatId(), "Введите ваш логин:");
            }
            case AWAITING_USERNAME -> {
                loginStatesMap.put(message.getChatId(), LoginStates.AWAITING_PASSWORD);
                usersMap.put(message.getChatId(), message.getText().trim());
                sendMessage(message.getChatId(), "Введите ваш пароль:");
            }
            case AWAITING_PASSWORD -> {
                loginStatesMap.remove(message.getChatId());

                LoginRequestDto requestDto = new LoginRequestDto(message.getChatId(),
                        usersMap.get(message.getChatId()),
                        message.getText().trim());

                kafkaTemplate.send("loginRequests", requestDto);
            }
        }
    }

    @KafkaListener(topics = "loginResponses", id = "bot-login-group")
    public void listenLoginResponseTopic(LoginResponse response) {
        if (response.success()) {
            authorizedUsers.put(usersMap.get(response.chatId()), response.chatId());
            sendMessage(response.chatId(), "Добро пожаловать " + usersMap.get(response.chatId()) + "!");
        } else {
            sendMessage(response.chatId(), "Пользователь с указанными данными не найден. Для повторной попытки входа используйте команду /login ещё раз");
        }
    }

    @KafkaListener(topics = "reportResponses", id = "bot-report-group")
    public void listenReportResponseTopic(ReportResponse response) {
        if (response.isSuccess()) {
            sendMessage(response.getChatId(), formatReport(response));
        } else {
            sendMessage(response.getChatId(), "Не удалось сформировать отчёт, убедитесь в корректности введённых данных и попробуйте ещё раз." +
                                             " Для повторной попытки введите команду /report_response");
        }
    }

    @KafkaListener(topics = "notifications", id = "bot-notifications-group")
    public void listenNotificationTopic(NotificationInfo notificationInfo) {
        notificationInfo.getManagerUsernames().stream()
                .filter(authorizedUsers::containsKey)
                .forEach(user -> sendMessage(authorizedUsers.get(user), notificationInfo.getMessage()));
    }

    private void handleReportCommand(Message message) {
        if (usersMap.get(message.getChatId()) == null || !authorizedUsers.containsKey(usersMap.get(message.getChatId()))) {
            sendMessage(message.getChatId(),
                    "Сперва вам необходимо авторизоваться. Используйте команду /login .");
            return;
        }

        switch (reportStatesMap.get(message.getChatId())) {
            case null -> {
                reportStatesMap.put(message.getChatId(), ReportStates.AWAITING_REPORT_TYPE);
                reportParametersMap.put(message.getChatId(), new ReportRequestDto());
                sendMessage(message.getChatId(), "Выберите тип отчёта", getReportTypeKeyboardRow());
            }
            case AWAITING_REPORT_TYPE -> {
                if ("По автомобилю".equalsIgnoreCase(message.getText().trim())) {
                    reportParametersMap.get(message.getChatId()).setReportType(ReportType.VEHICLE_MILEAGE);
                } else if ("По предприятию".equalsIgnoreCase(message.getText().trim())) {
                    reportParametersMap.get(message.getChatId()).setReportType(ReportType.ENTERPRISE_MILEAGE);
                } else {
                    sendMessage(message.getChatId(), "Некорректный тип отчёта, попробуйте ещё раз.", getReportTypeKeyboardRow());
                    return;
                }

                reportStatesMap.put(message.getChatId(), ReportStates.AWAITING_PERIOD_TYPE);
                sendMessage(message.getChatId(), "Выберите период отчёта", getReportPeriodKeyboardRow());
            }
            case AWAITING_PERIOD_TYPE -> {
                if ("день".equalsIgnoreCase(message.getText().trim())) {
                    reportParametersMap.get(message.getChatId()).setReportPeriod(ReportPeriod.DAY);
                } else if ("месяц".equalsIgnoreCase(message.getText().trim())) {
                    reportParametersMap.get(message.getChatId()).setReportPeriod(ReportPeriod.MONTH);
                } else if ("год".equalsIgnoreCase(message.getText().trim())) {
                    reportParametersMap.get(message.getChatId()).setReportPeriod(ReportPeriod.YEAR);
                } else {
                    sendMessage(message.getChatId(), "Некорректный период отчёта, попробуйте ещё раз.", getReportPeriodKeyboardRow());
                    return;
                }

                reportStatesMap.put(message.getChatId(), ReportStates.AWAITING_ENTITY_ID);
                sendMessage(message.getChatId(), "Введите номер автомобиля или название предприятия, по которому нужно сформировать отчёт");
            }
            case AWAITING_ENTITY_ID -> {
                reportParametersMap.get(message.getChatId()).setEntityId(message.getText().trim());
                reportStatesMap.put(message.getChatId(), ReportStates.AWAITING_REPORT_START_DATE);
                sendMessage(message.getChatId(), "Укажите дату начала отчёта в формате ГГГГ-ММ-ДД");
            }
            case AWAITING_REPORT_START_DATE -> {
                try {
                    LocalDate startDate = LocalDate.parse(message.getText().trim());
                    reportParametersMap.get(message.getChatId()).setStartDate(startDate);
                } catch (DateTimeParseException e) {
                    sendMessage(message.getChatId(), "Не удалось распознать дату, попробуйте ещё раз.");
                    return;
                }
                reportStatesMap.put(message.getChatId(), ReportStates.AWAITING_REPORT_END_DATE);
                sendMessage(message.getChatId(), "Укажите дату окончания отчёта в формате ГГГГ-ММ-ДД");
            }
            case AWAITING_REPORT_END_DATE -> {
                try {
                    LocalDate endDate = LocalDate.parse(message.getText().trim());
                    reportParametersMap.get(message.getChatId()).setEndDate(endDate);
                } catch (DateTimeParseException e) {
                    sendMessage(message.getChatId(), "Не удалось распознать дату, попробуйте ещё раз.");
                    return;
                }

                ReportRequestDto reportRequestDto = reportParametersMap.get(message.getChatId());
                reportRequestDto.setChatId(message.getChatId());

                kafkaTemplate.send("reportRequests", reportRequestDto);

                reportStatesMap.remove(message.getChatId());
            }
        }
    }

    private void handleText(Message message) {
        Long chatId = message.getChatId();
        if(loginStatesMap.containsKey(chatId)) {
            handleLoginCommand(message);
        } else if (reportStatesMap.containsKey(chatId)) {
            handleReportCommand(message);
        } else {
            sendMessage(chatId, "Я вас не понимаю." +
                                " Используйте /help, чтобы увидеть список доступных команд.");
        }
    }

    private void sendMessage(Long chatId, String message) {
        SendMessage newMessage = new SendMessage(chatId.toString(), message);
        try {
            execute(newMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }

    private void sendMessage(Long chatId, String message, ReplyKeyboardMarkup keyboardMarkup) {
        SendMessage newMessage = new SendMessage(chatId.toString(), message);
        newMessage.setReplyMarkup(keyboardMarkup);
        try {
            execute(newMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }

    private ReplyKeyboardMarkup getReportTypeKeyboardRow() {
        KeyboardRow row = new KeyboardRow();
        row.add("По автомобилю");
        row.add("По предприятию");
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(List.of(row));
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);
        return markup;
    }

    private ReplyKeyboardMarkup getReportPeriodKeyboardRow() {
        KeyboardRow row = new KeyboardRow();
        row.add("День");
        row.add("Месяц");
        row.add("Год");
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(List.of(row));
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);
        return markup;
    }

    private String formatReport(ReportResponse reportResponse) {
        StringBuilder sb = new StringBuilder();
        sb.append("Тип отчета: ").append(reportResponse.getType().getDescription()).append("\n");
        sb.append("Период: ").append(reportResponse.getPeriod().getName()).append("\n");
        sb.append("С ").append(reportResponse.getStartDate()).append(" по ").append(reportResponse.getEndDate()).append("\n");
        sb.append("------\n");
        reportResponse.getValues().forEach((k, v) -> sb.append(k).append(" : ").append(v).append(" км\n"));
        return sb.toString();
    }

    private enum LoginStates {
        AWAITING_USERNAME, AWAITING_PASSWORD
    }

    private enum ReportStates {
        AWAITING_REPORT_TYPE, AWAITING_PERIOD_TYPE, AWAITING_ENTITY_ID, AWAITING_REPORT_START_DATE, AWAITING_REPORT_END_DATE
    }
}
