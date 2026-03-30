package com.betacom.betacom.integration;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

class RateLimitIT extends BaseIT {

    @Autowired
    private TestDataFactory dataFactory;

    @BeforeEach
    void setUp(){
        dataFactory.cleanDatabase();
    }

    //POST users/login
    //429 Too Many Requests Przekroczono limit prób logowania. Header: Retry-After: <sekundy>
    @Test
    void shouldReturn429WhenRateLimitExceeded() {
        String requestBody = """
                {
                    "login": "user",
                    "password": "test1234"
                }
                """;

        // Wykonanie 5 dopuszczalnych prób
        for (int i = 1; i <= 5; i++) {
            given()
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post("/users/login")
                    .then()
                    .statusCode(not(429));
        }

        // Szósta próba zablokowana
        var response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/users/login");

        // Weryfikacja statusu 429 i nagłówka Retry-After
        response.then()
                .statusCode(429)
                .header("Retry-After", notNullValue())
                .body(containsString("Przekroczono limit prób logowania. Spróbuj ponownie za "
                        + response.getHeader("Retry-After") + " sekund."));
    }
}