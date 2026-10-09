package ru.praktikum.scooter.order;

import io.qameta.allure.Description;
import io.qameta.allure.junit5.AllureJunit5;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.praktikum.scooter.client.CourierClient;
import ru.praktikum.scooter.client.OrderClient;
import ru.praktikum.scooter.generator.CourierGenerator;
import ru.praktikum.scooter.generator.OrderGenerator;
import ru.praktikum.scooter.model.Courier;
import ru.praktikum.scooter.model.CourierCredentials;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(AllureJunit5.class)
@DisplayName("Принятие заказа: PUT /api/v1/orders/accept/:id")
class AcceptOrderTest {
    private static final String NOT_ENOUGH_DATA_MESSAGE = "Недостаточно данных для поиска";
    private static final String ORDER_NOT_FOUND_MESSAGE = "Заказа с таким id не существует";
    private static final String COURIER_NOT_FOUND_MESSAGE = "Курьера с таким id не существует";
    private static final int NON_EXISTENT_ORDER_ID = Integer.MAX_VALUE;

    private final CourierClient courierClient = new CourierClient();
    private final OrderClient orderClient = new OrderClient();
    private Integer courierId;
    private Integer track;
    private int orderId;

    @BeforeEach
    void setUp() {
        Courier courier = CourierGenerator.randomCourier();
        courierClient.create(courier).statusCode(SC_CREATED);
        courierId = courierClient.login(CourierCredentials.from(courier)).extract().path("id");

        track = orderClient.create(OrderGenerator.randomOrder())
                .statusCode(SC_CREATED)
                .extract().path("track");
        orderId = orderClient.getByTrack(track)
                .statusCode(SC_OK)
                .extract().path("order.id");
    }

    @AfterEach
    void tearDown() {
        if (track != null) {
            orderClient.cancel(track);
        }
        if (courierId != null) {
            courierClient.delete(courierId);
        }
    }

    @Test
    @DisplayName("Курьер может принять заказ")
    @Description("Запрос с id заказа и id курьера возвращает 200 и ok: true")
    void courierCanAcceptOrder() {
        orderClient.accept(orderId, courierId)
                .statusCode(SC_OK)
                .body("ok", equalTo(true));
    }

    @Test
    @DisplayName("Нельзя принять заказ без id курьера")
    @Description("Запрос без id курьера возвращает 400")
    void orderCannotBeAcceptedWithoutCourierId() {
        orderClient.acceptWithoutCourier(orderId)
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo(NOT_ENOUGH_DATA_MESSAGE));
    }

    @Test
    @DisplayName("Нельзя принять заказ с неверным id курьера")
    @Description("Запрос с id удалённого курьера возвращает 404")
    void orderCannotBeAcceptedWithWrongCourierId() {
        int deletedCourierId = courierId;
        courierClient.delete(deletedCourierId).statusCode(SC_OK);
        courierId = null;

        orderClient.accept(orderId, deletedCourierId)
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo(COURIER_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("Нельзя принять заказ без номера заказа")
    @Description("Запрос без номера заказа должен вернуть 400. Известный баг: стенд возвращает 404 Not Found")
    void orderCannotBeAcceptedWithoutOrderId() {
        orderClient.acceptWithoutOrder(courierId)
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo(NOT_ENOUGH_DATA_MESSAGE));
    }

    @Test
    @DisplayName("Нельзя принять заказ с неверным номером заказа")
    @Description("Запрос с несуществующим номером заказа возвращает 404")
    void orderCannotBeAcceptedWithWrongOrderId() {
        orderClient.accept(NON_EXISTENT_ORDER_ID, courierId)
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo(ORDER_NOT_FOUND_MESSAGE));
    }
}
