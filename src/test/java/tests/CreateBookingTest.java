package tests;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import specs.Specs;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CreateBookingTest {

    private static final LocalDate CHECKIN = LocalDate.of(2026, 12, 10);

    // Valid booking with a custom checkout date
    private Map<String, Object> validBooking(LocalDate checkout) {
        Map<String, Object> dates = new LinkedHashMap<>();
        dates.put("checkin", CHECKIN.toString());
        dates.put("checkout", checkout.toString());

        Map<String, Object> booking = new LinkedHashMap<>();
        booking.put("firstname", "Adrian");
        booking.put("lastname", "Test");
        booking.put("totalprice", 100);
        booking.put("depositpaid", true);
        booking.put("bookingdates", dates);
        booking.put("additionalneeds", "Breakfast");
        return booking;
    }

    @Test(description = "EP - valid partition: all valid fields create a booking")
    public void shouldCreateBookingWithValidData() {
        Map<String, Object> booking = validBooking(CHECKIN.plusDays(3));

        given()
                .spec(Specs.request())
                .body(booking)
        .when()
                .post("/booking")
        .then()
                .statusCode(200)
                .body("bookingid", notNullValue())
                .body("booking.firstname", equalTo("Adrian"))
                .body("booking.lastname", equalTo("Test"))
                .body("booking.totalprice", equalTo(100))
                .body("booking.depositpaid", equalTo(true))
                .body("booking.bookingdates.checkin", equalTo("2026-12-10"))
                .body("booking.bookingdates.checkout", equalTo("2026-12-13"))
                .body("booking.additionalneeds", equalTo("Breakfast"));
    }

    @DataProvider(name = "requiredFields")
    public Object[][] requiredFields() {
        return new Object[][] {
                {"firstname"},
                {"lastname"},
                {"totalprice"},
                {"depositpaid"},
                {"bookingdates"}
        };
    }

    @Test(dataProvider = "requiredFields",
            description = "EP - invalid partition: missing required field is rejected. "
                    + "BUG: API returns 500 instead of 400")
    public void shouldRejectBookingWithMissingRequiredField(String missingField) {
        Map<String, Object> booking = validBooking(CHECKIN.plusDays(3));
        booking.remove(missingField);

        given()
                .spec(Specs.request())
                .body(booking)
        .when()
                .post("/booking")
        .then()
                .statusCode(500);
    }

    @DataProvider(name = "totalpriceBoundaries")
    public Object[][] totalpriceBoundaries() {
        return new Object[][] {
                {-1, 200},  // just below boundary - invalid. BUG: accepted (should be 400)
                {0,  200}   // boundary - lowest valid price
        };
    }

    @Test(dataProvider = "totalpriceBoundaries",
            description = "BVA (2-value) - totalprice lower boundary: -1 / 0. "
                    + "BUG: negative price -1 is accepted with 200 instead of 400")
    public void shouldValidateTotalpriceLowerBoundary(int totalprice, int expectedStatus) {
        Map<String, Object> booking = validBooking(CHECKIN.plusDays(3));
        booking.put("totalprice", totalprice);

        given()
                .spec(Specs.request())
                .body(booking)
        .when()
                .post("/booking")
        .then()
                .statusCode(expectedStatus);
    }

    @Test(description = "EP - invalid partition (wrong type): totalprice as text. "
            + "BUG: accepted with 200 instead of 400")
    public void shouldRejectTotalpriceAsText() {
        Map<String, Object> booking = validBooking(CHECKIN.plusDays(3));
        booking.put("totalprice", "abc");

        given()
                .spec(Specs.request())
                .body(booking)
        .when()
                .post("/booking")
        .then()
                .statusCode(200)
                .body("booking.totalprice", nullValue());
    }

    @DataProvider(name = "checkoutBoundaries")
    public Object[][] checkoutBoundaries() {
        return new Object[][] {
                {-1, 200},  // checkout 1 day before checkin - invalid. BUG: accepted (should be 400)
                {0,  200},  // checkout same day as checkin (0 nights). BUG: accepted (should be 400)
                {1,  200}   // checkout 1 day after checkin - first valid value
        };
    }

    @Test(dataProvider = "checkoutBoundaries",
            description = "BVA (3-value) - checkout relative to checkin: -1 / 0 / +1 days. "
                    + "BUG: checkout before or on checkin day is accepted with 200 instead of 400")
    public void shouldValidateCheckoutAgainstCheckin(int daysAfterCheckin, int expectedStatus) {
        Map<String, Object> booking = validBooking(CHECKIN.plusDays(daysAfterCheckin));

        given()
                .spec(Specs.request())
                .body(booking)
        .when()
                .post("/booking")
        .then()
                .statusCode(expectedStatus);
    }
}