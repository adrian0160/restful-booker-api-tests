package utils;

import specs.Specs;

import static io.restassured.RestAssured.given;

public final class BookingHelper {

    private BookingHelper() {
    }

    public static int createBooking(String firstname, String lastname) {
        String body = """
        {
          "firstname": "%s",
          "lastname": "%s",
          "totalprice": 100,
          "depositpaid": true,
          "bookingdates": {
            "checkin": "2026-12-01",
            "checkout": "2026-12-05"
          },
          "additionalneeds": "Breakfast"
        }
        """.formatted(firstname, lastname);

        return given().spec(Specs.request()).body(body)
                .when().post("/booking")
                .then().statusCode(200)
                .extract().path("bookingid");
    }

    public static int createBooking() {
        return createBooking("Adrian", "Test");
    }
}
