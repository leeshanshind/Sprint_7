package ru.praktikum.scooter.order;

import io.qameta.allure.Description;
import io.qameta.allure.junit5.AllureJunit5;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.praktikum.scooter.client.OrderClient;
import ru.praktikum.scooter.generator.OrderGenerator;
import ru.praktikum.scooter.model.Order;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(AllureJunit5.class)
@DisplayName("Получение заказа по номеру: GET /api/v1/orders/track")
class GetOrderByTrackTest {
    private static final String NOT_ENOUGH_DATA_MESSAGE = "Недостаточно данных для поиска";
    private static final String ORDER_NOT_FOUND_MESSAGE = "Заказ не найден";
    private static final int NON_EXISTENT_TRACK = Integer.MAX_VALUE;

    private final OrderClient orderClient = new OrderClient();
    private Order order;
    private Integer track;

    @BeforeEach
    void setUp() {
        order = OrderGenerator.randomOrder();
        track = orderClient.create(order)
                .statusCode(SC_CREATED)
                .extract().path("track");
    }

    @AfterEach
    void tearDown() {
        if (track != null) {
            orderClient.cancel(track);
        }
    }

    @Test
    @DisplayName("Заказ можно получить по номеру")
    @Description("Запрос с номером созданного заказа возвращает 200 и объект с этим заказом")
    void orderCanBeFoundByTrack() {
        orderClient.getByTrack(track)
                .statusCode(SC_OK)
                .body("order", notNullValue())
                .body("order.id", notNullValue())
                .body("order.track", equalTo(track))
                .body("order.firstName", equalTo(order.getFirstName()))
                .body("order.lastName", equalTo(order.getLastName()));
    }

    @Test
    @DisplayName("Нельзя получить заказ без номера")
    @Description("Запрос без номера заказа возвращает 400")
    void orderCannotBeFoundWithoutTrack() {
        orderClient.getWithoutTrack()
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo(NOT_ENOUGH_DATA_MESSAGE));
    }

    @Test
    @DisplayName("Нельзя получить несуществующий заказ")
    @Description("Запрос с несуществующим номером заказа возвращает 404")
    void nonExistentOrderCannotBeFound() {
        orderClient.getByTrack(NON_EXISTENT_TRACK)
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo(ORDER_NOT_FOUND_MESSAGE));
    }
}
