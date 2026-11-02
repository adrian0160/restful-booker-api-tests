package utils;

import specs.Specs;

import static io.restassured.RestAssured.given;

public final class BookingHelper {

    private BookingHelper() {
    }

    public static int createBooking() {
        String body = """
        {
          "firstname": "Adrian",
          "lastname": "Test",
          "totalprice": 100,
          "depositpaid": true,
          "bookingdates": {
            "checkin": "2026-12-01",
            "checkout": "2026-12-05"
          },
          "additionalneeds": "Breakfast"
        }
        """;

        return given()
                    .spec(Specs.request())
                    .body(body)
                .when()
                    .post("/booking")
                .then()
                    .statusCode(200)
                    .extract().path("bookingid");
    }
}
