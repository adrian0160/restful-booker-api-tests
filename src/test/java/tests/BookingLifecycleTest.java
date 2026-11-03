package tests;

import org.testng.annotations.Test;
import specs.Specs;
import utils.AuthHelper;
import utils.BookingHelper;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class BookingLifecycleTest {

    // New data used for PUT - every field is different from the created booking
    private Map<String, Object> updatedBooking() {
        Map<String, Object> dates = new LinkedHashMap<>();
        dates.put("checkin", "2027-01-10");
        dates.put("checkout", "2027-01-15");

        Map<String, Object> booking = new LinkedHashMap<>();
        booking.put("firstname", "Maria");
        booking.put("lastname", "Updated");
        booking.put("totalprice", 250);
        booking.put("depositpaid", false);
        booking.put("bookingdates", dates);
        booking.put("additionalneeds", "Dinner");
        return booking;
    }

    @Test(description = "State Transition - booking lifecycle: created (GET 200) "
            + "-> deleted (GET 404)")
    public void shouldReturn404AfterBookingIsDeleted() {
        int bookingId = BookingHelper.createBooking();

        given()
                .spec(Specs.request())
                .pathParam("id", bookingId)
        .when()
                .get("/booking/{id}")
        .then()
                .statusCode(200);

        given()
                .spec(Specs.request())
                .cookie("token", AuthHelper.getToken())
                .pathParam("id", bookingId)
        .when()
                .delete("/booking/{id}")
        .then()
                .statusCode(201);

        given()
                .spec(Specs.request())
                .pathParam("id", bookingId)
        .when()
                .get("/booking/{id}")
        .then()
                .statusCode(404);
    }

    @Test(description = "State Transition - create -> read -> update "
            + "-> read (new data) -> delete -> read (404)")
    public void shouldCompleteFullCrudFlow() {
        int bookingId = BookingHelper.createBooking();
        String token = AuthHelper.getToken();

        // Read - original data
        given()
                .spec(Specs.request())
                .pathParam("id", bookingId)
        .when()
                .get("/booking/{id}")
        .then()
                .statusCode(200)
                .body("firstname", equalTo("Adrian"));

        // Update
        given()
                .spec(Specs.request())
                .cookie("token", token)
                .pathParam("id", bookingId)
                .body(updatedBooking())
        .when()
                .put("/booking/{id}")
        .then()
                .statusCode(200);

        // Read - updated data
        given()
                .spec(Specs.request())
                .pathParam("id", bookingId)
        .when()
                .get("/booking/{id}")
        .then()
                .statusCode(200)
                .body("firstname", equalTo("Maria"))
                .body("lastname", equalTo("Updated"))
                .body("totalprice", equalTo(250));

        // Delete
        given()
                .spec(Specs.request())
                .cookie("token", token)
                .pathParam("id", bookingId)
        .when()
                .delete("/booking/{id}")
        .then()
                .statusCode(201);

        // Read - booking no longer exists
        given()
                .spec(Specs.request())
                .pathParam("id", bookingId)
        .when()
                .get("/booking/{id}")
        .then()
                .statusCode(404);
    }

    @Test(description = "State Transition - invalid transition: a deleted booking "
            + "cannot be updated. BUG: API returns 405 instead of 404")
    public void shouldNotUpdateDeletedBooking() {
        int bookingId = BookingHelper.createBooking();
        String token = AuthHelper.getToken();

        // Delete
        given()
                .spec(Specs.request())
                .cookie("token", token)
                .pathParam("id", bookingId)
        .when()
                .delete("/booking/{id}")
        .then()
                .statusCode(201);

        // Update the deleted booking
        given()
                .spec(Specs.request())
                .cookie("token", token)
                .pathParam("id", bookingId)
                .body(updatedBooking())
        .when()
                .put("/booking/{id}")
        .then()
                .statusCode(405);
    }

    @Test(description = "Rejected update (invalid token) "
            + "leaves the booking data unchanged")
    public void shouldKeepDataUnchangedAfterUnauthorizedUpdate() {
        int bookingId = BookingHelper.createBooking();

        // Update with invalid token
        given()
                .spec(Specs.request())
                .cookie("token", "invalidToken123")
                .pathParam("id", bookingId)
                .body(updatedBooking())
        .when()
                .put("/booking/{id}")
        .then()
                .statusCode(403);

        // Read - data must be the original one
        given()
                .spec(Specs.request())
                .pathParam("id", bookingId)
        .when()
                .get("/booking/{id}")
        .then()
                .statusCode(200)
                .body("firstname", equalTo("Adrian"))
                .body("lastname", equalTo("Test"))
                .body("totalprice", equalTo(100));
    }
}
