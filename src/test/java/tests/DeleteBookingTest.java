package tests;

import io.restassured.specification.RequestSpecification;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import specs.Specs;
import utils.AuthHelper;
import utils.BookingHelper;

import static io.restassured.RestAssured.given;

public class DeleteBookingTest {

    @DataProvider(name = "deleteAuthScenarios")
    public Object[][] deleteAuthScenarios() {
        return new Object[][] {
                {"VALID", 201},
                {"INVALID", 403},
                {"EMPTY", 403},
                {"NONE", 403}
        };
    }

    @Test(dataProvider = "deleteAuthScenarios",
    description = "Decision Table - DELETE /booking with valid / invalid / missing token." +
    "BUG: successful DELETE returns 201 instead of 200/204")
    public void deleteBookingAuthorization(String tokenType, int expectedStatus) {
        int bookingId = BookingHelper.createBooking();

        RequestSpecification request = given().spec(Specs.request());
        switch (tokenType) {
            case "VALID"    -> request.cookie("token", AuthHelper.getToken());
            case "INVALID"  -> request.cookie("token", "invalidToken");
            case "EMPTY"    -> request.cookie("token", "");
            case "NONE"     -> { }
            default         -> throw new IllegalArgumentException("Unkown tokenType: " + tokenType);
        }

        request
        .when()
                .delete("/booking/" + bookingId)
        .then()
                .statusCode(expectedStatus);
    }
}
