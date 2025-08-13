package ru.lessons.my.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import ru.lessons.my.ShellApp;

@Configuration
@ComponentScan(value = "ru.lessons.my", excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = ShellApp.class
))
public class BaseConfig {
}
