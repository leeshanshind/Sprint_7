package ru.praktikum.scooter.client;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import ru.praktikum.scooter.model.Courier;
import ru.praktikum.scooter.model.CourierCredentials;

import static io.restassured.RestAssured.given;

public class CourierClient extends BaseClient {
    private static final String COURIER_PATH = "/courier";
    private static final String LOGIN_PATH = COURIER_PATH + "/login";
    private static final String COURIER_BY_ID_PATH = COURIER_PATH + "/{id}";

    @Step("Создать курьера")
    public ValidatableResponse create(Courier courier) {
        return given()
                .spec(getSpec())
                .body(courier)
                .when()
                .post(COURIER_PATH)
                .then();
    }

    @Step("Залогиниться курьером")
    public ValidatableResponse login(CourierCredentials credentials) {
        return given()
                .spec(getSpec())
                .body(credentials)
                .when()
                .post(LOGIN_PATH)
                .then();
    }

    @Step("Удалить курьера с id {courierId}")
    public ValidatableResponse delete(int courierId) {
        return given()
                .spec(getSpec())
                .pathParam("id", courierId)
                .when()
                .delete(COURIER_BY_ID_PATH)
                .then();
    }

    @Step("Удалить курьера без id")
    public ValidatableResponse deleteWithoutId() {
        return given()
                .spec(getSpec())
                .when()
                .delete(COURIER_PATH)
                .then();
    }
}
