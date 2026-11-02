package tests;

import org.testng.annotations.Test;
import specs.Specs;
import utils.BookingHelper;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

/**
 * GET /booking: returns booking IDs, optionally filtered by firstname, lastname, checkin, checkout.
 */
public class GetBookingIdsTest {

    @Test(description = "Decision Table - rule 1: firstname + lastname match -> booking found")
    public void shouldFindBookingByFirstnameAndLastname() {
        String firstname = "Adrian" + System.currentTimeMillis();
        int bookingId = BookingHelper.createBooking(firstname, "Test");

        given()
                .spec(Specs.request())
                .queryParam("firstname", firstname)
                .queryParam("lastname", "Test")
        .when()
                .get("/booking")
        .then()
                .statusCode(200)
                .body("bookingid", hasItem(bookingId));
    }

    @Test(description = "Decision Table - rule 2: only firstname sent -> booking found")
    public void shouldFindBookingByFirstnameOnly() {
        String firstname = "Adrian" + System.currentTimeMillis();
        int bookingId = BookingHelper.createBooking(firstname, "Test");

        given()
                .spec(Specs.request())
                .queryParam("firstname", firstname)
        .when()
                .get("/booking")
        .then()
                .statusCode(200)
                .body("bookingid", hasItem(bookingId));
    }

    @Test(description = "Decision Table - rule 3: only lastname sent -> booking found")
    public void shouldFindBookingByLastnameOnly() {
        String lastname = "Test" + System.currentTimeMillis();
        int bookingId = BookingHelper.createBooking("Adrian", lastname);

        given()
                .spec(Specs.request())
                .queryParam("lastname", lastname)
        .when()
                .get("/booking")
        .then()
                .statusCode(200)
                .body("bookingid", hasItem(bookingId));
    }

    @Test(description = "Decision Table - rule 4: firstname matches, lastname wrong -> booking NOT found")
    public void shouldNotFindBookingWhenLastnameIsWrong() {
        String firstname = "Adrian" + System.currentTimeMillis();
        int bookingId = BookingHelper.createBooking(firstname, "Test");

        given()
                .spec(Specs.request())
                .queryParam("firstname", firstname)
                .queryParam("lastname", "WrongName")
        .when()
                .get("/booking")
        .then()
                .statusCode(200)
                .body("bookingid", not(hasItem(bookingId)));
    }
}
