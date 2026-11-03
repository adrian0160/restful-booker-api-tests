package tests;

import org.testng.annotations.Test;
import specs.Specs;
import utils.AuthHelper;
import utils.BookingHelper;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class PartialUpdateBookingTest {

    // PATCH body with only one field
    private Map<String, Object> firstnameOnly() {
        Map<String, Object> booking = new LinkedHashMap<>();
        booking.put("firstname", "Maria");
        return booking;
    }

    @Test(description = "EP - valid partition: PATCH firstname only changes firstname, "
            + "all other fields stay the same"
            + "Decision Table - Token sent, Token valid, Booking modified")
    public void shouldUpdateOnlyFirstname() {
        int bookingId = BookingHelper.createBooking();

        given()
                .spec(Specs.request())
                .cookie("token", AuthHelper.getToken())
                .pathParam("id", bookingId)
                .body(firstnameOnly())
        .when()
                .patch("/booking/{id}")
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
                .body("lastname", equalTo("Test"))
                .body("totalprice", equalTo(100))
                .body("depositpaid", equalTo(true))
                .body("bookingdates.checkin", equalTo("2026-12-01"))
                .body("bookingdates.checkout", equalTo("2026-12-05"))
                .body("additionalneeds", equalTo("Breakfast"));
    }

    @Test(description = "BVA - lower boundary: PATCH with 0 fields (empty body) "
            + "keeps the booking unchanged")
    public void shouldKeepBookingUnchangedWithEmptyBody() {
        int bookingId = BookingHelper.createBooking();

        given()
                .spec(Specs.request())
                .cookie("token", AuthHelper.getToken())
                .pathParam("id", bookingId)
                .body(new LinkedHashMap<String, Object>())
        .when()
                .patch("/booking/{id}")
        .then()
                .statusCode(200);

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

    @Test(description = "Decision Table - Token sent, Token not valid, Booking not modified")
    public void shouldRejectPatchWithInvalidToken() {
        int bookingId = BookingHelper.createBooking();

        given()
                .spec(Specs.request())
                .cookie("token", "invalidToken123")
                .pathParam("id", bookingId)
                .body(firstnameOnly())
        .when()
                .patch("/booking/{id}")
        .then()
                .statusCode(403);
    }

    @Test(description = "Decision Table - Token not sent, Token not valid or invalid, Booking not modified")
    public void shouldRejectPatchWithoutToken() {
        int bookingId = BookingHelper.createBooking();

        given()
                .spec(Specs.request())
                .pathParam("id", bookingId)
                .body(firstnameOnly())
        .when()
                .patch("/booking/{id}")
        .then()
                .statusCode(403);
    }

    @Test(description = "EP - invalid partition: PATCH on non-existent ID. "
            + "BUG: API returns 405 instead of 404")
    public void shouldRejectPatchForNonExistentId() {
        given()
                .spec(Specs.request())
                .cookie("token", AuthHelper.getToken())
                .pathParam("id", 999999999)
                .body(firstnameOnly())
        .when()
                .patch("/booking/{id}")
        .then()
                .statusCode(405);
    }
}