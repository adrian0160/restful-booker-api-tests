package tests;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import specs.Specs;
import utils.BookingHelper;

import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

/**
 * GET /booking/{id}: returns a single booking.
 */
public class GetBookingTest {

    @Test(description = "EP - valid partition: GET existing booking returns 200 " +
            "and the same data sent on POST")
    public void shouldReturnBookingDetailsForExistingId() {
        int bookingId = BookingHelper.createBooking();

        given()
                .spec(Specs.request())
                .pathParam("id", bookingId)
                .when()
                .get("/booking/{id}")
                .then()
                .statusCode(200)
                .body("firstname", equalTo("Adrian"))
                .body("lastname", equalTo("Test"))
                .body("totalprice", equalTo(100))
                .body("depositpaid", equalTo(true))
                .body("bookingdates.checkin", equalTo("2026-12-01"))
                .body("bookingdates.checkout", equalTo("2026-12-05"))
                .body("additionalneeds", equalTo("Breakfast"));
    }

    @DataProvider(name = "invalidBookingIds")
    public Object[][] invalidBookingIds() {
        return new Object[][] {
                {0},           // BVA - lower boundary (IDs start at 1)
                {-1},          // BVA - just below boundary / EP negative numbers
                {999999999},   // EP - valid format, non-existent ID
                {"abc"}        // EP - invalid format (non-numeric)
        };
    }

    @Test(dataProvider = "invalidBookingIds",
            description = "EP + BVA - invalid partitions: GET with non-existent or " +
            "malformed ID returns 404")
    public void shouldReturn404ForInvalidBookingId(Object invalidId) {
        given()
                .spec(Specs.request())
                .pathParam("id", invalidId)
        .when()
                .get("/booking/{id}")
        .then()
                .statusCode(404);
    }

    @Test(description = "GET booking matches " + "JSON schema and responds in under 2 seconds")
    public void shouldMatchSchemaAndRespondFast() {
        // Arrange
        int bookingId = BookingHelper.createBooking();

        // Act + Assert
        given()
                .spec(Specs.request())
                .pathParam("id", bookingId)
        .when()
                .get("/booking/{id}")
        .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/booking-schema.json"))
                .time(lessThan(2L), TimeUnit.SECONDS);
    }
}
