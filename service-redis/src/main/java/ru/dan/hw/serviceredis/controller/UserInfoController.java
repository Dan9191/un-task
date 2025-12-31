package ru.dan.hw.serviceredis.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.dan.hw.serviceredis.model.UserInfoResponse;
import ru.dan.hw.serviceredis.service.UserCacheService;

import java.util.UUID;

@RestController
@RequestMapping("/api/cache/users")
@RequiredArgsConstructor
public class UserInfoController {

    private final UserCacheService userCacheService;

    @Operation(summary = "Получить список чеков пользователя")
    @GetMapping("/{userId}/info")
    public UserInfoResponse getUserInfo(
            @Parameter(description = "ID пользователя")
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }

        try {
            return userCacheService.getUserInfo(userId, page, size);
        } catch (RedisConnectionFailureException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "User cache is unavailable");
        }
    }
}
