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

@ExtendWith(AllureJunit5.class)
@DisplayName("Удаление курьера: DELETE /api/v1/courier/:id")
class DeleteCourierTest {
    private static final String NOT_ENOUGH_DATA_MESSAGE = "Недостаточно данных для удаления курьера";
    private static final String COURIER_NOT_FOUND_MESSAGE = "Курьера с таким id нет.";
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
    @DisplayName("Курьера можно удалить")
    @Description("Удаление существующего курьера возвращает 200 и ok: true, после этого он не может залогиниться")
    void courierCanBeDeleted() {
        courierClient.delete(courierId)
                .statusCode(SC_OK)
                .body("ok", equalTo(true));
        courierId = null;

        courierClient.login(CourierCredentials.from(courier))
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo(ACCOUNT_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("Нельзя удалить курьера без id")
    @Description("Запрос без id должен вернуть 400. Известный баг: стенд возвращает 404 Not Found")
    void courierCannotBeDeletedWithoutId() {
        courierClient.deleteWithoutId()
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo(NOT_ENOUGH_DATA_MESSAGE));
    }

    @Test
    @DisplayName("Нельзя удалить курьера с несуществующим id")
    @Description("Повторное удаление уже удалённого курьера возвращает 404")
    void courierWithNonExistentIdCannotBeDeleted() {
        int deletedCourierId = courierId;
        courierClient.delete(deletedCourierId).statusCode(SC_OK);
        courierId = null;

        courierClient.delete(deletedCourierId)
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo(COURIER_NOT_FOUND_MESSAGE));
    }
}
