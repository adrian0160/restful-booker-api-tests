package tests;

import org.testng.annotations.Test;
import specs.Specs;

import static io.restassured.RestAssured.given;

public class HealthCheckTest {

    @Test(description = "GET /ping returns 201 when the API is up")
    public void pingReturns201() {
        given()
                .spec(Specs.request())
        .when()
                .get("/ping")
        .then()
                .statusCode(201);
    }
}
