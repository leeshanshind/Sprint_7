package ru.praktikum.scooter.client;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import ru.praktikum.scooter.model.Order;

import static io.restassured.RestAssured.given;

public class OrderClient extends BaseClient {
    private static final String ORDERS_PATH = "/orders";
    private static final String TRACK_PATH = ORDERS_PATH + "/track";
    private static final String CANCEL_PATH = ORDERS_PATH + "/cancel";
    private static final String ACCEPT_PATH = ORDERS_PATH + "/accept/";
    private static final String ACCEPT_BY_ID_PATH = ACCEPT_PATH + "{id}";

    private static final String TRACK_PARAM = "t";
    private static final String CANCEL_TRACK_PARAM = "track";
    private static final String COURIER_ID_PARAM = "courierId";
    private static final String LIMIT_PARAM = "limit";

    @Step("Создать заказ")
    public ValidatableResponse create(Order order) {
        return given()
                .spec(getSpec())
                .body(order)
                .when()
                .post(ORDERS_PATH)
                .then();
    }

    @Step("Получить список заказов, не более {limit}")
    public ValidatableResponse getList(int limit) {
        return given()
                .spec(getSpec())
                .queryParam(LIMIT_PARAM, limit)
                .when()
                .get(ORDERS_PATH)
                .then();
    }

    @Step("Получить заказ по номеру {track}")
    public ValidatableResponse getByTrack(int track) {
        return given()
                .spec(getSpec())
                .queryParam(TRACK_PARAM, track)
                .when()
                .get(TRACK_PATH)
                .then();
    }

    @Step("Получить заказ без номера")
    public ValidatableResponse getWithoutTrack() {
        return given()
                .spec(getSpec())
                .when()
                .get(TRACK_PATH)
                .then();
    }

    @Step("Принять заказ {orderId} курьером {courierId}")
    public ValidatableResponse accept(int orderId, int courierId) {
        return given()
                .spec(getSpec())
                .pathParam("id", orderId)
                .queryParam(COURIER_ID_PARAM, courierId)
                .when()
                .put(ACCEPT_BY_ID_PATH)
                .then();
    }

    @Step("Принять заказ {orderId} без id курьера")
    public ValidatableResponse acceptWithoutCourier(int orderId) {
        return given()
                .spec(getSpec())
                .pathParam("id", orderId)
                .when()
                .put(ACCEPT_BY_ID_PATH)
                .then();
    }

    @Step("Принять заказ без номера заказа курьером {courierId}")
    public ValidatableResponse acceptWithoutOrder(int courierId) {
        return given()
                .spec(getSpec())
                .queryParam(COURIER_ID_PARAM, courierId)
                .when()
                .put(ACCEPT_PATH)
                .then();
    }

    @Step("Отменить заказ {track}")
    public ValidatableResponse cancel(int track) {
        return given()
                .spec(getSpec())
                .queryParam(CANCEL_TRACK_PARAM, track)
                .when()
                .put(CANCEL_PATH)
                .then();
    }
}
