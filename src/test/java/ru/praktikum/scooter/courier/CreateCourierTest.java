package ru.praktikum.scooter.courier;

import io.qameta.allure.Description;
import io.qameta.allure.junit5.AllureJunit5;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.praktikum.scooter.client.CourierClient;
import ru.praktikum.scooter.generator.CourierGenerator;
import ru.praktikum.scooter.model.Courier;
import ru.praktikum.scooter.model.CourierCredentials;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(AllureJunit5.class)
@DisplayName("Создание курьера: POST /api/v1/courier")
class CreateCourierTest {
    private static final String NOT_ENOUGH_DATA_MESSAGE = "Недостаточно данных для создания учетной записи";
    private static final String LOGIN_ALREADY_USED_MESSAGE = "Этот логин уже используется. Попробуйте другой.";
    private static final String LOGIN_FIELD = "login";
    private static final String PASSWORD_FIELD = "password";

    private final CourierClient courierClient = new CourierClient();
    private Courier courier;

    @BeforeEach
    void setUp() {
        courier = CourierGenerator.randomCourier();
    }

    @AfterEach
    void tearDown() {
        Integer courierId = courierClient.login(CourierCredentials.from(courier))
                .extract().path("id");
        if (courierId != null) {
            courierClient.delete(courierId);
        }
    }

    @Test
    @DisplayName("Курьера можно создать")
    @Description("Запрос со всеми обязательными полями возвращает 201 и ok: true")
    void courierCanBeCreated() {
        courierClient.create(courier)
                .statusCode(SC_CREATED)
                .body("ok", equalTo(true));
    }

    @Test
    @DisplayName("Созданный курьер может залогиниться")
    @Description("После создания курьера логин с его данными возвращает 200")
    void createdCourierCanLogin() {
        courierClient.create(courier).statusCode(SC_CREATED);

        courierClient.login(CourierCredentials.from(courier))
                .statusCode(SC_OK);
    }

    @Test
    @DisplayName("Нельзя создать двух одинаковых курьеров")
    @Description("Повторное создание курьера с теми же данными возвращает 409")
    void twoIdenticalCouriersCannotBeCreated() {
        courierClient.create(courier).statusCode(SC_CREATED);

        courierClient.create(courier)
                .statusCode(SC_CONFLICT)
                .body("message", equalTo(LOGIN_ALREADY_USED_MESSAGE));
    }

    @Test
    @DisplayName("Нельзя создать курьера с логином, который уже есть")
    @Description("Создание курьера с занятым логином и другими паролем и именем возвращает 409")
    void courierWithExistingLoginCannotBeCreated() {
        courierClient.create(courier).statusCode(SC_CREATED);
        Courier courierWithSameLogin = new Courier(courier.getLogin(),
                CourierGenerator.randomString(), CourierGenerator.randomString());

        courierClient.create(courierWithSameLogin)
                .statusCode(SC_CONFLICT)
                .body("message", equalTo(LOGIN_ALREADY_USED_MESSAGE));
    }

    @ParameterizedTest(name = "Без поля {0}")
    @ValueSource(strings = {LOGIN_FIELD, PASSWORD_FIELD})
    @DisplayName("Нельзя создать курьера без обязательного поля")
    @Description("Если не передать логин или пароль, запрос возвращает 400")
    void courierCannotBeCreatedWithoutRequiredField(String missingField) {
        Courier incompleteCourier = new Courier(courier.getLogin(), courier.getPassword(), courier.getFirstName());
        if (LOGIN_FIELD.equals(missingField)) {
            incompleteCourier.setLogin(null);
        } else {
            incompleteCourier.setPassword(null);
        }

        courierClient.create(incompleteCourier)
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo(NOT_ENOUGH_DATA_MESSAGE));
    }
}
