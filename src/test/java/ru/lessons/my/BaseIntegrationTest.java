package ru.lessons.my;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;
import ru.lessons.my.bot.TelegramBot;
import ru.lessons.my.config.BaseTestConfig;
import ru.lessons.my.config.BotConfig;
import ru.lessons.my.config.SecurityConfig;
import ru.lessons.my.config.TestDbConfig;

@SpringJUnitConfig(classes = {BaseTestConfig.class, TestDbConfig.class, SecurityConfig.class})
@MockitoBean(types = {TelegramBot.class, BotConfig.class})
@Transactional
public abstract class BaseIntegrationTest {
}
