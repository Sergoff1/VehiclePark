package ru.lessons.my.bot;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.lessons.my.model.Report;
import ru.lessons.my.model.ReportPeriod;
import ru.lessons.my.model.ReportType;
import ru.lessons.my.model.entity.Manager;
import ru.lessons.my.service.ManagerService;
import ru.lessons.my.service.ReportService;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@PropertySource("classpath:application.properties")
public class TelegramBot extends TelegramLongPollingBot {

    private final ManagerService managerService;
    private final ReportService reportService;
    private final PasswordEncoder passwordEncoder;

    private final Map<Long, Manager> authorizedManagers = new HashMap<>();
    private final Map<Long, LoginStates> loginStatesMap = new HashMap<>();
    private final Map<Long, String> usernamesMap = new HashMap<>();
    private final Map<Long, ReportParameters> reportParametersMap = new HashMap<>();
    private final Map<Long, ReportStates> reportStatesMap = new HashMap<>();

    @Value("${bot.tg.name}")
    private String botName;

    @Autowired
    public TelegramBot(@Value("${bot.tg.token}") String botToken,
                       ManagerService managerService,
                       ReportService reportService,
                       PasswordEncoder passwordEncoder) {

        super(botToken);
        this.managerService =  managerService;
        this.reportService = reportService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    @SneakyThrows
    public void registerCommands() {
        execute(SetMyCommands.builder()
                .command(new BotCommand("/start", "Начать работу с ботом"))
                .command(new BotCommand("/login", "Авторизация"))
                .command(new BotCommand("/report", "Сформировать отчёт"))
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
            case "/report" -> handleReportCommand(update.getMessage());
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
        authorizedManagers.remove(message.getChatId());
        loginStatesMap.remove(message.getChatId());
        reportStatesMap.remove(message.getChatId());
        usernamesMap.remove(message.getChatId());
        sendMessage(message.getChatId(), "Вы вышли из системы.");
    }

    private void handleHelpCommand(Message message) {
        sendMessage(message.getChatId(),
                """
                        Для начала работы необходимо авторизоваться с помощью команды /login .
                        Для выхода из системы используйте команду /logout .
                        Для запроса отчёта по пробегу автомобилей используйте команду /report .""");
    }

    private void handleLoginCommand(Message message) {
        if (authorizedManagers.containsKey(message.getChatId())) {
            sendMessage(message.getChatId(),
                    "Вы уже авторизованы как " + authorizedManagers.get(message.getChatId()).getUsername() +
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
                usernamesMap.put(message.getChatId(), message.getText().trim());
                sendMessage(message.getChatId(), "Введите ваш пароль:");
            }
            case AWAITING_PASSWORD -> {
                loginStatesMap.remove(message.getChatId());
                Manager manager = managerService.getManagerByUsername(usernamesMap.get(message.getChatId()));
                if (passwordEncoder.matches(message.getText().trim(), manager.getPassword())) {
                    authorizedManagers.put(message.getChatId(), manager);
                    sendMessage(message.getChatId(), "Добро пожаловать " + manager.getUsername() + "!");
                } else {
                    sendMessage(message.getChatId(), "Пользователь с указанными данными не найден. Для повторной попытки входа используйте команду /login ещё раз");
                }
            }
        }

    }

    private void handleReportCommand(Message message) {
        if (!authorizedManagers.containsKey(message.getChatId())) {
            sendMessage(message.getChatId(),
                    "Сперва вам необходимо авторизоваться. Используйте команду /login .");
            return;
        }

        switch (reportStatesMap.get(message.getChatId())) {
            case null -> {
                reportStatesMap.put(message.getChatId(), ReportStates.AWAITING_REPORT_TYPE);
                reportParametersMap.put(message.getChatId(), new ReportParameters());
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
                sendMessage(message.getChatId(), "Введите идентификатор сущности(автомобиля или предприятия), по которой нужно сформировать отчёт");
            }
            case AWAITING_ENTITY_ID -> {
                try {
                    Long entityId = Long.parseLong(message.getText().trim());
                    reportParametersMap.get(message.getChatId()).setEntityId(entityId);
                } catch (NumberFormatException e) {
                    sendMessage(message.getChatId(), "Не удалось распознать идентификатор(это должно быть число), попробуйте ещё раз.");
                    return;
                }

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

                try {
                    PipedInputStream is = new PipedInputStream();
                    PipedOutputStream os = new PipedOutputStream(is);

                    ReportParameters reportParameters = reportParametersMap.get(message.getChatId());

                    Report report = reportService.getReport(reportParameters.getReportType(),
                            reportParameters.getEntityId(),
                            reportParameters.getEntityId(),
                            reportParameters.getReportPeriod(),
                            reportParameters.getStartDate(),
                            reportParameters.getEndDate());

                    //Piped стримы нормально работают в разных потоках, поэтому созадаём новый, а то будут дедлоки.
                    new Thread(() -> {
                        try (PipedOutputStream out = os) {
                            reportService.writePdfToOutputStream(out, report);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();

                    execute(SendDocument.builder()
                            .chatId(message.getChatId())
                            .document(new InputFile(is, "report.pdf"))
                            .caption("Файл с отчётом")
                            .build());
                } catch (Exception e) {
                    sendMessage(message.getChatId(), "Не удалось сформировать отчёт, убедитесь в корректности введённых данных и попробуйте ещё раз." +
                                                     " Для повторной попытки введите команду /report");
                }

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

    private enum LoginStates {
        AWAITING_USERNAME, AWAITING_PASSWORD
    }

    private enum ReportStates {
        AWAITING_REPORT_TYPE, AWAITING_PERIOD_TYPE, AWAITING_ENTITY_ID, AWAITING_REPORT_START_DATE, AWAITING_REPORT_END_DATE
    }
}
