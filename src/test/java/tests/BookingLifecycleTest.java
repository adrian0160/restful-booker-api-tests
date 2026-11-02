package tests;

import org.testng.annotations.Test;
import specs.Specs;
import utils.AuthHelper;
import utils.BookingHelper;

import static io.restassured.RestAssured.given;

public class BookingLifecycleTest {

    @Test(description = "State Transition - booking lifecycle: created (GET 200) "
            + "-> deleted (GET 404)")
    public void shouldReturn404AfterBookingIsDeleted() {
        // Stare 1: booking created
        int bookingId = BookingHelper.createBooking();

        given()
                .spec(Specs.request())
                .pathParam("id", bookingId)
        .when()
                .get("/booking/{id}")
        .then()
                .statusCode(200);

        // Transition: DELETE
        given()
                .spec(Specs.request())
                .cookie("token", AuthHelper.getToken())
                .pathParam("id", bookingId)
        .when()
                .delete("/booking/{id}")
        .then()
                .statusCode(201);

        // State 2: booking deleted
        given()
                .spec(Specs.request())
                .pathParam("id", bookingId)
        .when()
                .get("/booking/{id}")
        .then()
                .statusCode(404);
    }
}
