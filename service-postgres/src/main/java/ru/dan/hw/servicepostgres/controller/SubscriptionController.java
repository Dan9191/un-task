package ru.dan.hw.servicepostgres.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dan.hw.servicepostgres.model.ActivateSubscriptionRequest;
import ru.dan.hw.servicepostgres.model.DeactivateSubscriptionRequest;
import ru.dan.hw.servicepostgres.model.SubscriptionResponse;
import ru.dan.hw.servicepostgres.service.SubscriptionService;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "Активация подписки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Подписка успешно активирована",
                    content = @Content(schema = @Schema(implementation = SubscriptionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные: тип подписки, дата в прошлом и т.д."),
            @ApiResponse(responseCode = "409", description = "У пользователя уже есть активная подписка")
    })
    @PostMapping("/activate")
    public ResponseEntity<SubscriptionResponse> activateSubscription(
            @Valid @RequestBody ActivateSubscriptionRequest request) {

        SubscriptionResponse response = subscriptionService.activateSubscription(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Деактивация подписки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Подписка успешно деактивирована"),
            @ApiResponse(responseCode = "400", description = "Некорректный тип подписки"),
            @ApiResponse(responseCode = "409", description = "Активная подписка указанного типа не найдена")
    })
    @PostMapping("/deactivate")
    public ResponseEntity<Void> deactivateSubscription(
            @Valid @RequestBody DeactivateSubscriptionRequest request) {

        subscriptionService.deactivateSubscription(request);
        return ResponseEntity.noContent().build();
    }
}
