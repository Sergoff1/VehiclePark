package ru.lessons.my.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.lessons.my.BaseIntegrationTest;
import ru.lessons.my.config.WebConfig;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {WebConfig.class})
@WebAppConfiguration
public class CsrfTest extends BaseIntegrationTest {

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp(WebApplicationContext applicationContext) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void loginWhenInvalidCsrfTokenThenForbidden() throws Exception {
        this.mockMvc.perform(post("/login").with(csrf().useInvalidToken())
                        .accept(MediaType.TEXT_HTML)
                        .param("username", "user")
                        .param("password", "password"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void loginWhenMissingCsrfTokenThenForbidden() throws Exception {
        this.mockMvc.perform(post("/login")
                        .accept(MediaType.TEXT_HTML)
                        .param("username", "user")
                        .param("password", "password"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void loginWhenCorrectCsrfTokenThenRedirectToEnterprises() throws Exception {
        this.mockMvc.perform(post("/login").with(csrf())
                        .accept(MediaType.TEXT_HTML)
                        .param("username", "manager1")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/enterprises"));
    }
}
