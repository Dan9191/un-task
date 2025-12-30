package ru.dan.hw.servicepostgres;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.dan.hw.servicepostgres.config.BaseTestWithContext;
import ru.dan.hw.servicepostgres.entity.Subscription;
import ru.dan.hw.servicepostgres.repository.SubscriptionRepository;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ServicePostgresApplicationTests extends BaseTestWithContext {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private static final UUID TEST_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final String BASIC_TYPE = "BASIC";
    private static final String PRO_TYPE = "PRO";
    private static final LocalDate FUTURE_DATE = LocalDate.of(2026, 1, 1);


    @Test
    @Order(1)
    @DisplayName("Успешная активация подписки")
    void activateSubscription_Success() throws Exception {
        String requestJson = """
            {
                "userId": "%s",
                "subscriptionType": "%s",
                "activationDate": "%s"
            }
            """.formatted(TEST_USER_ID, BASIC_TYPE, FUTURE_DATE);

        mockMvc.perform(post("/api/v1/subscriptions/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.userId", is(TEST_USER_ID.toString())))
                .andExpect(jsonPath("$.subscriptionType", is(BASIC_TYPE)))
                .andExpect(jsonPath("$.activationDate", is(FUTURE_DATE.toString())));

        // Проверяем, что подписка сохранена
        Subscription sub = subscriptionRepository.findByUserIdAndActiveTrue(TEST_USER_ID).getFirst();
        Assertions.assertTrue(sub.isActive());
        Assertions.assertEquals(BASIC_TYPE, sub.getSubscriptionType().getName());
        Assertions.assertEquals(FUTURE_DATE, sub.getActivationDate());
    }

    @Test
    @Order(2)
    @DisplayName("Повторная активация подписки — конфликт")
    void activateSubscription_WhenAlreadyActive_Conflict() throws Exception {
        String requestJson = """
            {
                "userId": "%s",
                "subscriptionType": "%s",
                "activationDate": "%s"
            }
            """.formatted(TEST_USER_ID, PRO_TYPE, FUTURE_DATE.plusDays(1));

        mockMvc.perform(post("/api/v1/subscriptions/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title", is("Конфликт")))
                .andExpect(jsonPath("$.detail", containsString("У пользователя уже есть активная подписка")));
    }

    @Test
    @Order(3)
    @DisplayName("Активация с неизвестным типом подписки — Bad Request")
    void activateSubscription_UnknownType_BadRequest() throws Exception {
        String requestJson = """
            {
                "userId": "%s",
                "subscriptionType": "PREMIUM",
                "activationDate": "%s"
            }
            """.formatted(TEST_USER_ID, FUTURE_DATE);

        mockMvc.perform(post("/api/v1/subscriptions/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Некорректные данные")))
                .andExpect(jsonPath("$.detail", containsString("Тип подписки не найден")));
    }

    @Test
    @Order(4)
    @DisplayName("Успешная деактивация подписки")
    void deactivateSubscription_Success() throws Exception {
        // активная подписка есть из теста 1
        Assertions.assertEquals(1, subscriptionRepository.findByUserIdAndActiveTrue(TEST_USER_ID).size());

        String deactivateRequest = """
            {
                "userId": "%s",
                "subscriptionType": "%s"
            }
            """.formatted(TEST_USER_ID, BASIC_TYPE);

        mockMvc.perform(post("/api/v1/subscriptions/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deactivateRequest))
                .andExpect(status().isNoContent());

        // Проверяем, что подписка стала неактивной
        Subscription sub = subscriptionRepository.findByUserIdAndActiveTrue(TEST_USER_ID)
                .stream().findFirst().orElse(null);
        Assertions.assertNull(sub);

        // Но сама запись осталась
        Subscription deactivated = subscriptionRepository
                .findByUserIdAndSubscriptionTypeName(TEST_USER_ID, BASIC_TYPE).getFirst();
        Assertions.assertFalse(deactivated.isActive());
    }

    @Test
    @Order(5)
    @DisplayName("Деактивация несуществующей (уже деактивированной) подписки — конфликт")
    void deactivateSubscription_WhenNotActive_Conflict() throws Exception {
        String requestJson = """
            {
                "userId": "%s",
                "subscriptionType": "%s"
            }
            """.formatted(TEST_USER_ID, BASIC_TYPE);

        mockMvc.perform(post("/api/v1/subscriptions/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title", is("Конфликт")))
                .andExpect(jsonPath("$.detail", containsString("Активная подписка типа BASIC не найдена")));
    }

}
