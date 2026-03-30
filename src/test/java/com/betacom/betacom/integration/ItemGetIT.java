package com.betacom.betacom.integration;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class ItemGetIT extends BaseIT {

    @Autowired
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp(){
        testDataFactory.cleanDatabase();
        testDataFactory.createTestUsers();
        testDataFactory.createTestItems();
    }

    //GET /items
    //200 OK Lista notatek
    @Test
    void shouldGetListOfItems() {
        given()
                .header("Authorization", "Bearer " + testDataFactory.getOwnerToken())
                .contentType(ContentType.JSON)
                .when()
                .get("/items")
                .then()
                .statusCode(200)
                .body("", hasSize(2));
    }

    //GET /items
    //200 OK Lista notatek
    @Test
    void shouldGetEmptyListOfItems() {
        var login = """
                {
                    "login": "userWithoutPermission",
                    "password": "test3456"
                }
                """;

        given()
                .header("Authorization", "Bearer " + testDataFactory.getToken(login))
                .contentType(ContentType.JSON)
                .when()
                .get("/items")
                .then()
                .statusCode(200)
                .body("", is(empty()));
    }

    //GET /items
    //401 Unauthorized Brak lub nieważny token JWT
    @Test
    void getItemsRequestMissingToken() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/items")
                .then()
                .statusCode(401)
                .body(containsString("Brak lub niewazny token JWT"));
    }
}