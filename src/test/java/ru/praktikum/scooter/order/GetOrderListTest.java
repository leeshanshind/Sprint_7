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

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import java.util.List;

@ExtendWith(AllureJunit5.class)
@DisplayName("Список заказов: GET /api/v1/orders")
class GetOrderListTest {
    private static final int LIMIT = 5;

    private final OrderClient orderClient = new OrderClient();
    private Integer track;

    @BeforeEach
    void setUp() {
        // создаём заказ, чтобы список гарантированно не был пустым
        track = orderClient.create(OrderGenerator.randomOrder())
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
    @DisplayName("В теле ответа возвращается список заказов")
    @Description("Запрос возвращает 200 и непустой массив orders, у каждого заказа есть id и track")
    void orderListIsReturned() {
        orderClient.getList(LIMIT)
                .statusCode(SC_OK)
                .body("orders", instanceOf(List.class))
                .body("orders", not(empty()))
                .body("orders.size()", lessThanOrEqualTo(LIMIT))
                .body("orders", everyItem(hasKey("id")))
                .body("orders", everyItem(hasKey("track")));
    }
}
