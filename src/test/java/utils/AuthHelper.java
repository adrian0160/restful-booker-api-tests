package utils;

import config.Config;
import models.AuthRequest;
import specs.Specs;

import static io.restassured.RestAssured.given;

public final class AuthHelper {

    private AuthHelper() {
    }

    public static String getToken() {
        return given()
                    .spec(Specs.request())
                    .body(new AuthRequest(Config.username(), Config.password()))
                .when()
                    .post("/auth")
                .then()
                    .statusCode(200)
                    .extract().path("token");
    }
}
