package ru.praktikum.scooter.courier;

import io.qameta.allure.Description;
import io.qameta.allure.junit5.AllureJunit5;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.praktikum.scooter.client.CourierClient;
import ru.praktikum.scooter.generator.CourierGenerator;
import ru.praktikum.scooter.model.Courier;
import ru.praktikum.scooter.model.CourierCredentials;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(AllureJunit5.class)
@DisplayName("Логин курьера: POST /api/v1/courier/login")
class LoginCourierTest {
    private static final String NOT_ENOUGH_DATA_MESSAGE = "Недостаточно данных для входа";
    private static final String ACCOUNT_NOT_FOUND_MESSAGE = "Учетная запись не найдена";

    private final CourierClient courierClient = new CourierClient();
    private Courier courier;
    private Integer courierId;

    @BeforeEach
    void setUp() {
        courier = CourierGenerator.randomCourier();
        courierClient.create(courier).statusCode(SC_CREATED);
        courierId = courierClient.login(CourierCredentials.from(courier)).extract().path("id");
    }

    @AfterEach
    void tearDown() {
        if (courierId != null) {
            courierClient.delete(courierId);
        }
    }

    @Test
    @DisplayName("Курьер может авторизоваться")
    @Description("Логин с верными данными возвращает 200 и id курьера")
    void courierCanLogin() {
        courierClient.login(CourierCredentials.from(courier))
                .statusCode(SC_OK)
                .body("id", notNullValue())
                .body("id", equalTo(courierId));
    }

    @Test
    @DisplayName("Нельзя авторизоваться с неверным логином")
    @Description("Логин с неверным логином возвращает 404")
    void courierCannotLoginWithWrongLogin() {
        CourierCredentials credentials = new CourierCredentials(
                courier.getLogin() + CourierGenerator.randomString(), courier.getPassword());

        courierClient.login(credentials)
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo(ACCOUNT_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("Нельзя авторизоваться с неверным паролем")
    @Description("Логин с неверным паролем возвращает 404")
    void courierCannotLoginWithWrongPassword() {
        CourierCredentials credentials = new CourierCredentials(
                courier.getLogin(), courier.getPassword() + CourierGenerator.randomString());

        courierClient.login(credentials)
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo(ACCOUNT_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("Нельзя авторизоваться без логина")
    @Description("Запрос без поля login возвращает 400")
    void courierCannotLoginWithoutLogin() {
        CourierCredentials credentials = new CourierCredentials(null, courier.getPassword());

        courierClient.login(credentials)
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo(NOT_ENOUGH_DATA_MESSAGE));
    }

    @Test
    @DisplayName("Нельзя авторизоваться без пароля")
    @Description("Запрос без поля password должен вернуть 400. Известный баг: стенд не отвечает на такой запрос")
    void courierCannotLoginWithoutPassword() {
        CourierCredentials credentials = new CourierCredentials(courier.getLogin(), null);

        courierClient.login(credentials)
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo(NOT_ENOUGH_DATA_MESSAGE));
    }

    @Test
    @DisplayName("Нельзя авторизоваться с пустым паролем")
    @Description("Запрос с пустым паролем возвращает 400")
    void courierCannotLoginWithEmptyPassword() {
        CourierCredentials credentials = new CourierCredentials(courier.getLogin(), "");

        courierClient.login(credentials)
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo(NOT_ENOUGH_DATA_MESSAGE));
    }

    @Test
    @DisplayName("Нельзя авторизоваться под несуществующим курьером")
    @Description("Логин несуществующего курьера возвращает 404")
    void nonExistentCourierCannotLogin() {
        Courier nonExistentCourier = CourierGenerator.randomCourier();

        courierClient.login(CourierCredentials.from(nonExistentCourier))
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo(ACCOUNT_NOT_FOUND_MESSAGE));
    }
}
