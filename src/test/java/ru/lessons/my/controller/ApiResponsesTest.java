package ru.lessons.my.controller;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.lessons.my.config.BaseConfig;
import ru.lessons.my.config.SecurityConfig;
import ru.lessons.my.config.TestDbConfig;
import ru.lessons.my.config.WebConfig;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BaseConfig.class, TestDbConfig.class, WebConfig.class, SecurityConfig.class})
@WebAppConfiguration
@Transactional
public class ApiResponsesTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.wac)
                .apply(springSecurity())
                .build();
    }

    @Test
    void whenNotAuthorized_then401() throws Exception {
        mockMvc.perform(get("/api/v1/enterprises"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    void whenNotManager_then403() throws Exception {
        mockMvc.perform(get("/api/v1/enterprises").with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenManagerSendBadCredentials_then401() throws Exception {
        String body = "{\"username\": \"manager2\", \"password\": \"wrongPassword\"}";
        mockMvc.perform(post("/api/v1/login")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenManagerAccessToUnavailableResource_then403() throws Exception {
        mockMvc.perform(get("/api/v1/enterprises/3")
                        .with(jwt().jwt(jwt -> jwt.subject("manager1")).authorities(() -> "SCOPE_API")))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenIncorrectRequestFields_then400() throws Exception {
        String body = """
                {
                    "modelId": 1,
                    "enterpriseId": 2,
                    "activeDriverId": "WrongType",
                    "licensePlateNumber": "T001EC44",
                    "productionYear": "WrongType",
                    "mileageKm": 100000,
                    "color": "Тестовый",
                    "purchasePriceRub": 100,
                    "driverIds": [2]
                }
                """;
        mockMvc.perform(post("/api/v1/vehicles")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(jwt -> jwt.subject("manager1")).authorities(() -> "SCOPE_API")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenInconsistentRemove_then409() throws Exception {
        mockMvc.perform(delete("/api/v1/enterprises/1")
                        .with(jwt().jwt(jwt -> jwt.subject("manager1")).authorities(() -> "SCOPE_API")))
                .andExpect(status().isConflict());
    }

    @Test
    void whenInconsistentRemoveWhenOnlyManagersExists_then409() throws Exception {
        mockMvc.perform(delete("/api/v1/enterprises/4")
                        .with(jwt().jwt(jwt -> jwt.subject("manager1")).authorities(() -> "SCOPE_API")))
                .andExpect(status().isConflict());
    }

    @Test
    void whenCorrectDelete_then204() throws Exception {
        mockMvc.perform(delete("/api/v1/vehicles/1")
                        .with(jwt().jwt(jwt -> jwt.subject("manager1")).authorities(() -> "SCOPE_API")))
                .andExpect(status().isNoContent());
    }

    @Test
    void whenCorrectPut_then204() throws Exception {
        String body = """
                {
                    "name": "UpdatedName",
                    "city": "UpdatedCity"
                }
                """;
        mockMvc.perform(put("/api/v1/enterprises/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().jwt(jwt -> jwt.subject("manager1")).authorities(() -> "SCOPE_API")))
                .andExpect(status().isNoContent());
    }

    @Test
    void whenCorrectPost_then201() throws Exception {
        String body = """
                {
                    "name": "newName",
                    "city": "newCity"
                }
                """;
        mockMvc.perform(post("/api/v1/enterprises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().jwt(jwt -> jwt.subject("manager1")).authorities(() -> "SCOPE_API")))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/enterprises/5"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("newName"))
                .andExpect(jsonPath("$.managerIds[0]").value(1));
    }
}
