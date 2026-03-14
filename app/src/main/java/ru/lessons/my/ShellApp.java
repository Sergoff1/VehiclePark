package ru.lessons.my;

import org.jline.reader.LineReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.shell.InputProvider;
import org.springframework.shell.Shell;
import org.springframework.shell.boot.CommandCatalogAutoConfiguration;
import org.springframework.shell.boot.CompleterAutoConfiguration;
import org.springframework.shell.boot.ExitCodeAutoConfiguration;
import org.springframework.shell.boot.JLineAutoConfiguration;
import org.springframework.shell.boot.JLineShellAutoConfiguration;
import org.springframework.shell.boot.LineReaderAutoConfiguration;
import org.springframework.shell.boot.ParameterResolverAutoConfiguration;
import org.springframework.shell.boot.ShellContextAutoConfiguration;
import org.springframework.shell.boot.SpringShellAutoConfiguration;
import org.springframework.shell.boot.StandardAPIAutoConfiguration;
import org.springframework.shell.boot.StandardCommandsAutoConfiguration;
import org.springframework.shell.boot.ThemingAutoConfiguration;
import org.springframework.shell.boot.UserConfigAutoConfiguration;
import org.springframework.shell.command.annotation.CommandScan;
import org.springframework.shell.jline.InteractiveShellRunner;
import org.springframework.shell.jline.PromptProvider;
import ru.lessons.my.config.DbConfig;

@Configuration
@ComponentScan("ru.lessons.my.repository")
@CommandScan("ru.lessons.my.util")
@Import({
        SpringShellAutoConfiguration.class,
        JLineShellAutoConfiguration.class,
        JLineAutoConfiguration.class,
        StandardAPIAutoConfiguration.class,
        StandardCommandsAutoConfiguration.class,
        CommandCatalogAutoConfiguration.class,
        ShellContextAutoConfiguration.class,
        ExitCodeAutoConfiguration.class,
        ParameterResolverAutoConfiguration.class,
        LineReaderAutoConfiguration.class,
        CompleterAutoConfiguration.class,
        UserConfigAutoConfiguration.class,
        ThemingAutoConfiguration.class
})
public class ShellApp {

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                DbConfig.class, ShellApp.class);

        context.registerShutdownHook();
        Shell shell = context.getBean(Shell.class);
        shell.run(context.getBean(InputProvider.class));
    }

    @Bean
    public InputProvider inputProvider(LineReader lineReader, PromptProvider promptProvider) {
        return new InteractiveShellRunner.JLineInputProvider(lineReader, promptProvider);
    }
}
