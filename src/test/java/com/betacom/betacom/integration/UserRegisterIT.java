package com.betacom.betacom.integration;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

public class UserRegisterIT extends BaseIT {

    @Autowired
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp(){
        testDataFactory.cleanDatabase();
    }

    //POST /users/register
    //201 Created Konto zostało pomyślnie utworzone
    @Test
    void shouldRegisterNewUser() {
        var requestBody = """
                {
                    "login": "user",
                    "password": "test1234"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/users/register")
                .then()
                .statusCode(201)
                .body(containsString("Konto zostało pomyślnie utworzone"));
    }

    //POST /users/register
    //400 Bad Request Brakujące pola, login za krótki/długi, hasło za słabe
    //Brak pola login
    @Test
    void registerRequestRequiresLoginField() {
        var requestBody = """
                {
                    "password": "test1234"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/users/register")
                .then()
                .statusCode(400)
                .body(containsString("Login jest wymagany"));
    }

    //POST /users/register
    //400 Bad Request Brakujące pola, login za krótki/długi, hasło za słabe
    //Brak pola hasło
    @Test
    void registerRequestRequiresPasswordField() {
        var requestBody = """
                {
                    "login": "user"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/users/register")
                .then()
                .statusCode(400)
                .body(containsString("Hasło jest wymagane"));
    }

    //POST /users/register
    //400 Bad Request Brakujące pola, login za krótki/długi, hasło za słabe
    //Za krótki login
    @Test
    void loginIsToShort() {
        var requestBody = """
                {
                    "login": "us",
                    "password": "test1234"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/users/register")
                .then()
                .statusCode(400)
                .body(containsString("Login jest za krótki. Minimalna długość 3 znaki"));
    }

    //POST /users/register
    //400 Bad Request Brakujące pola, login za krótki/długi, hasło za słabe
    //Za długi login
    @Test
    void loginIsToLong() {
        var requestBody = """
                {
                    "login": "user123412341234123412341234123412341234123412341234123412341234112313212",
                    "password": "test1234"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/users/register")
                .then()
                .statusCode(400)
                .body(containsString("Login jest za długi. Maksymalna długość 64 znaki"));
    }

    //POST /users/register
    //400 Bad Request Brakujące pola, login za krótki/długi, hasło za słabe Za krótkie hasło
    @Test
    void passwordIsToShort() {
        var requestBody = """
                {
                    "login": "user",
                    "password": "test"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/users/register")
                .then()
                .statusCode(400)
                .body(containsString("Hasło jest za słabe. Minimalna długość 8 znaków"));
    }

    //POST /users/register
    //409 Conflict Login jest już zajęty
    @Test
    void loginIsAlreadyTaken() {
        var requestBody = """
                {
                    "login": "user",
                    "password": "test1234"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/users/register")
                .then()
                .statusCode(201)
                .body(containsString("Konto zostało pomyślnie utworzone"));

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/users/register")
                .then()
                .statusCode(409)
                .body(containsString("Login jest już zajęty"));
    }
}
