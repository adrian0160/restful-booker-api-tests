package tests;

import config.Config;
import models.AuthRequest;
import org.testng.annotations.Test;
import specs.Specs;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AuthTest {

    @Test(description = "EP - valid partition: valid credentials return a token")
    public void shouldReturnTokenForValidCredentials() {
        AuthRequest credentials = new AuthRequest(Config.username(), Config.password());

        given()
                .spec(Specs.request())
                .body(credentials)
        .when()
                .post("/auth")
        .then()
                .statusCode(200)
                .body("token", not(emptyOrNullString()))
                .body("$", not(hasKey("reason")));
    }

    @Test(description = "EP - invalid partition: wrong password returns 'Bad credetials'. " +
            "BUG: API returns 200 instead of 401")
    public void shouldRejectWrongPassword() {
        AuthRequest credentials = new AuthRequest(Config.username(), "wrongPassword");

        given()
                .spec(Specs.request())
                .body(credentials)
        .when()
                .post("/auth")
        .then()
                .statusCode(200)
                .body("reason", equalTo("Bad credentials"))
                .body("$", not(hasKey("token")));
    }

    @Test(description = "empty username and password (length 0) " +
            "return 'Bad credentials'. BUG: API returns 200 instead of 401")
    public void shouldRejectEmptyUsernameAndPassword() {
        AuthRequest credentials = new AuthRequest("", "");

        given()
                .spec(Specs.request())
                .body(credentials)
        .when()
                .post("/auth")
        .then()
                .statusCode(200)
                .body("reason", equalTo("Bad credentials"))
                .body("$", not(hasKey("token")));
    }
}
