# Subscription storage service

## Описание
Сервис работы с подписками и обработкой чеков

## Swagger
Доступен по
http://localhost:8098/api/swagger-ui/index.html

## Cтек
Требуется Java 21 и Postgresql, RabbitMQ

## Конфигурация

Настройки в `src/main/resources/application.yaml`:


| Переменная                 | Значение по умолчанию                                  | Описание                                           |
|----------------------------|--------------------------------------------------------|----------------------------------------------------|
| SERVER_PORT                | 8098                                                   | Порт сервиса                                       |
| SPRING_APPLICATION_NAME    | service-postgres                                       | Имя приложения                                     |
| SPRING_DATASOURCE_URL      | jdbc:postgresql://localhost:5432/test?currentSchema=un | URL базы данных PostgreSQL                         |
| DATASOURCE_NAME            | test                                                   | Пользователь базы данных                           |
| DATASOURCE_PASSWORD        | test                                                   | Пароль базы данных                                 |
| DEFAULT_SCHEMA             | un                                                     | Название схемы                                     |
| RABBITMQ_HOST              | localhost                                              | Хост RABBITMQ                                      |
| RABBITMQ_PORT              | 5672                                                   | Порт RABBITMQ                                      |
| RABBITMQ_USERNAME          | guest                                                  | Пользователь RABBITMQ                              |
| RABBITMQ_PASSWORD          | guest                                                  | Пароль RABBITMQ                                    |
| RABBITMQ_VIRTUAL_HOST      | /                                                      | Виртуальный хост RABBITMQ                          |
| RABBITMQ_CON_TIMEOUT       | 5000                                                   | Время ожидания соединения RABBITMQ                 |
| BILLING_QUEUE              | billing.checks.queue                                   | Название очереди RABBITMQ                          |
| DAILY_CRON                 | 0 50 17 * * *                                          | Время запуска ежедневной задачи по обработке чеков |
| RETRY_DELAY                | 5000                                                   | Период запуска задачи отправки сообщений в брокер  |
