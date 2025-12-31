# Data caching service

## Описание
Сервис кэширования чеков пользователя. 
Если во время работы сервиса redis был не доступен, то чтение из очереди будет остановлено.

## Swagger
Доступен по
http://localhost:8099/api/swagger-ui/index.html

## Cтек
Требуется Java 21 и Redis, RabbitMQ

## Конфигурация

Настройки в `src/main/resources/application.yaml`:


| Переменная              | Значение по умолчанию | Описание                           |
|-------------------------|-----------------------|------------------------------------|
| SERVER_PORT             | 8099                  | Порт сервиса                       |
| SPRING_APPLICATION_NAME | service-postgres      | Имя приложения                     |
| RABBITMQ_HOST           | localhost             | Хост RABBITMQ                      |
| RABBITMQ_PORT           | 5672                  | Порт RABBITMQ                      |
| RABBITMQ_USERNAME       | guest                 | Пользователь RABBITMQ              |
| RABBITMQ_PASSWORD       | guest                 | Пароль RABBITMQ                    |
| RABBITMQ_VIRTUAL_HOST   | /                     | Виртуальный хост RABBITMQ          |
| RABBITMQ_CON_TIMEOUT    | 5000                  | Время ожидания соединения RABBITMQ |
| BILLING_QUEUE           | billing.checks.queue  | Название очереди RABBITMQ          |
| REDIS_HOST              | 127.0.0.1             | Хост REDIS                         |
| REDIS_PORT              | 6379                  | Порт REDIS                         |
| REDIS_PASSWORD          | authentik             | Пароль REDIS                       |


## Пример запросов
### 1. Получение данных пользователя о счетах и подписке (`GET /api/cache/users/557e8410-e29b-41d4-a718-146622444244/info`)

### Пример ответа
```json
{
  "userId": "557e8410-e29b-41d4-a718-146622444244",
  "activeSubscriptions": {
    "activationDate": "2025-12-31",
    "expiryDate": "2026-01-31",
    "price": 200
  },
  "receipts": [
    {
      "receiptId": 32,
      "issueDate": "2025-12-31",
      "activationDate": "2025-12-31",
      "price": 200
    }
  ],
  "totalReceipts": 1
}
```