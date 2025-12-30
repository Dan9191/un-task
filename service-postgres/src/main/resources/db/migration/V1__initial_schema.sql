CREATE TABLE IF NOT EXISTS subscription_type (
                                   id    SERIAL PRIMARY KEY,
                                   name  VARCHAR(100) NOT NULL UNIQUE,
                                   price INTEGER NOT NULL
);

COMMENT ON TABLE subscription_type        IS 'Типы подписок.';
COMMENT ON COLUMN subscription_type.id    IS 'Идентификатор типа подписки.';
COMMENT ON COLUMN subscription_type.name  IS 'Наименование типа подписки.';
COMMENT ON COLUMN subscription_type.price IS 'Цена подписки.';

CREATE TABLE IF NOT EXISTS "user" (
                        user_id UUID PRIMARY KEY
);

COMMENT ON TABLE "user" IS 'Пользователи системы.';
COMMENT ON COLUMN "user".user_id IS 'Идентификатор пользователя (UUID).';

CREATE TABLE IF NOT EXISTS receipt (
                         id                     SERIAL PRIMARY KEY,
                         user_id                UUID NOT NULL,
                         issue_date             DATE NOT NULL,
                         activation_date        DATE,
                         subscription_type_id   INTEGER NOT NULL,
                         sent_to_broker         BOOLEAN DEFAULT FALSE,

                         CONSTRAINT fk_check_user
                             FOREIGN KEY (user_id)
                                 REFERENCES "user"(user_id),

                         CONSTRAINT fk_check_subscription_type
                             FOREIGN KEY (subscription_type_id)
                                 REFERENCES subscription_type(id)
);

COMMENT ON TABLE receipt                       IS 'Счета пользователей.';
COMMENT ON COLUMN receipt.id                   IS 'Идентификатор счета.';
COMMENT ON COLUMN receipt.user_id              IS 'Идентификатор пользователя.';
COMMENT ON COLUMN receipt.issue_date           IS 'Дата выставления счета.';
COMMENT ON COLUMN receipt.activation_date      IS 'Дата активации подписки.';
COMMENT ON COLUMN receipt.subscription_type_id IS 'Тип подписки.';
COMMENT ON COLUMN receipt.sent_to_broker       IS 'Отправлена ли в топик.';

CREATE TABLE IF NOT EXISTS subscription (
                              id                    SERIAL PRIMARY KEY,
                              user_id               UUID NOT NULL,
                              subscription_type_id  INTEGER NOT NULL,
                              activation_date       DATE NOT NULL,
                              active                BOOLEAN DEFAULT TRUE,
                              processed             BOOLEAN DEFAULT FALSE,

                              CONSTRAINT fk_subscription_user
                                  FOREIGN KEY (user_id)
                                      REFERENCES "user"(user_id),

                              CONSTRAINT fk_subscription_subscription_type
                                  FOREIGN KEY (subscription_type_id)
                                      REFERENCES subscription_type(id)
);

COMMENT ON TABLE subscription                       IS 'Подписки пользователей.';
COMMENT ON COLUMN subscription.id                   IS 'Идентификатор подписки.';
COMMENT ON COLUMN subscription.user_id              IS 'Идентификатор пользователя.';
COMMENT ON COLUMN subscription.subscription_type_id IS 'Тип подписки.';
COMMENT ON COLUMN subscription.activation_date      IS 'Дата активации подписки.';
COMMENT ON COLUMN subscription.active               IS 'Активна ли подписка.';
COMMENT ON COLUMN subscription.processed            IS 'Обработана ли подписка.';

INSERT INTO subscription_type (id, name, price)
VALUES
    (1, 'BASIC', 100),
    (2, 'PRO', 200)
    ON CONFLICT (name) DO NOTHING;