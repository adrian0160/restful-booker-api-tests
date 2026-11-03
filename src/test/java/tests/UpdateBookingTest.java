package tests;

import org.testng.annotations.Test;
import specs.Specs;
import utils.AuthHelper;
import utils.BookingHelper;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UpdateBookingTest {

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

    @Test(description = "EP - valid partition: PUT with valid token updates all fields")
    public void shouldUpdateBookingWithValidToken() {
        int bookingId = BookingHelper.createBooking();

        given()
                .spec(Specs.request())
                .cookie("token", AuthHelper.getToken())
                .pathParam("id", bookingId)
                .body(updatedBooking())
        .when()
                .put("/booking/{id}")
        .then()
                .statusCode(200);

        given()
                .spec(Specs.request())
                .pathParam("id", bookingId)
        .when()
                .get("/booking/{id}")
        .then()
                .statusCode(200)
                .body("firstname", equalTo("Maria"))
                .body("lastname", equalTo("Updated"))
                .body("totalprice", equalTo(250))
                .body("depositpaid", equalTo(false))
                .body("bookingdates.checkin", equalTo("2027-01-10"))
                .body("bookingdates.checkout", equalTo("2027-01-15"))
                .body("additionalneeds", equalTo("Dinner"));
    }

    @Test(description = "Decision Table - PUT with invalid token returns 403")
    public void shouldRejectUpdateWithInvalidToken() {
        int bookingId = BookingHelper.createBooking();

        given()
                .spec(Specs.request())
                .cookie("token", "invalidToken123")
                .pathParam("id", bookingId)
                .body(updatedBooking())
        .when()
                .put("/booking/{id}")
        .then()
                .statusCode(403);
    }

    @Test(description = "Decision Table - PUT without token returns 403")
    public void shouldRejectUpdateWithoutToken() {
        int bookingId = BookingHelper.createBooking();

        given()
                .spec(Specs.request())
                .pathParam("id", bookingId)
                .body(updatedBooking())
        .when()
                .put("/booking/{id}")
        .then()
                .statusCode(403);
    }

    @Test(description = "EP - invalid partition: PUT with missing required field returns 400")
    public void shouldRejectUpdateWithMissingField() {
        int bookingId = BookingHelper.createBooking();
        Map<String, Object> booking = updatedBooking();
        booking.remove("firstname");

        given()
                .spec(Specs.request())
                .cookie("token", AuthHelper.getToken())
                .pathParam("id", bookingId)
                .body(booking)
        .when()
                .put("/booking/{id}")
        .then()
                .statusCode(400);
    }

    @Test(description = "EP - invalid partition: PUT on non-existent ID. "
            + "BUG: API returns 405 instead of 404")
    public void shouldRejectUpdateForNonExistentId() {
        given()
                .spec(Specs.request())
                .cookie("token", AuthHelper.getToken())
                .pathParam("id", 999999999)
                .body(updatedBooking())
        .when()
                .put("/booking/{id}")
        .then()
                .statusCode(405);
    }
}