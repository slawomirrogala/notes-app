package com.betacom.betacom.integration;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

class UserLoginIT extends BaseIT {

    @Autowired
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp(){
        testDataFactory.cleanDatabase();
    }

    //POST /users/login
    //200 OK Logowanie udane, token w response body
    @Test
    void successfulLogin() {
        var requestBody = """
                {
                    "login": "owner",
                    "password": "test1234"
                }
                """;

        testDataFactory.createTestUsers();

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/users/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("token", startsWith("eyJ"))
                .body("expiresIn", instanceOf(Number.class))
                .body("expiresIn", greaterThan(0));
    }

    //POST /users/login
    //400 Bad Request Brakujące pola
    @Test
    void loginRequestRequiresPasswordField() {
        var requestBody = """
                {
                    "login": "user"
                }
                """;

        testDataFactory.createTestUsers();

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/users/login")
                .then()
                .statusCode(400)
                .body(containsString("Hasło jest wymagane"));
    }

    //POST /users/login
    //400 Bad Request Brakujące pola
    @Test
    void loginRequestRequiresLoginField() {
        var requestBody = """
                {
                    "password": "test1234"
                }
                """;

        testDataFactory.createTestUsers();

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/users/login")
                .then()
                .statusCode(400)
                .body(containsString("Login jest wymagany"));
    }

    //POST /users/login
    //401 Unauthorized Nieprawidłowy login lub hasło
    @Test
    void invalidCredentials() {
        var requestBody = """
                {
                    "login": "owner",
                    "password": "test12345"
                }
                """;

        testDataFactory.createTestUsers();

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/users/login")
                .then()
                .statusCode(401)
                .body(containsString("Nieprawidłowy login lub hasło"));
    }
}