package ru.lessons.my;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.lessons.my.config.BaseConfig;
import ru.lessons.my.config.DbConfig;
import ru.lessons.my.config.SecurityConfig;
import ru.lessons.my.config.WebConfig;

public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                BaseConfig.class,
                WebConfig.class,
                DbConfig.class,
                SecurityConfig.class);
    }
}