package ru.praktikum.scooter.order;

import io.qameta.allure.Description;
import io.qameta.allure.junit5.AllureJunit5;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.praktikum.scooter.client.OrderClient;
import ru.praktikum.scooter.generator.OrderGenerator;
import ru.praktikum.scooter.model.Order;

import java.util.List;
import java.util.stream.Stream;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(AllureJunit5.class)
@DisplayName("Создание заказа: POST /api/v1/orders")
class CreateOrderTest {
    private static final String BLACK = "BLACK";
    private static final String GREY = "GREY";

    private final OrderClient orderClient = new OrderClient();
    private Integer track;

    static Stream<Arguments> colors() {
        return Stream.of(
                Arguments.of("Чёрный", List.of(BLACK)),
                Arguments.of("Серый", List.of(GREY)),
                Arguments.of("Оба цвета", List.of(BLACK, GREY)),
                Arguments.of("Пустой список цветов", List.of()),
                Arguments.of("Цвет не передан", null)
        );
    }

    @AfterEach
    void tearDown() {
        if (track != null) {
            orderClient.cancel(track);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("colors")
    @DisplayName("Заказ можно создать с любым вариантом цвета")
    @Description("Можно указать BLACK, GREY, оба цвета или не указывать цвет: запрос возвращает 201 и track")
    void orderCanBeCreatedWithColor(String caseName, List<String> color) {
        Order order = OrderGenerator.randomOrder(color);

        track = orderClient.create(order)
                .statusCode(SC_CREATED)
                .body("track", notNullValue())
                .extract().path("track");
    }
}
