package com.betacom.betacom.integration;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

public class ItemPostIT extends BaseIT {

    @Autowired
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp(){
        testDataFactory.cleanDatabase();
        testDataFactory.createTestUsers();
        testDataFactory.createTestItems();
    }

    //POST /items
    //201 Created Notatka została uwtorzona
    @Test
    void shouldCreateNewItem() {
        var requestBody = """
                {
                    "title": "Tytuł",
                    "content": "Treść"
                }
                """;

        given()
                .header("Authorization", "Bearer " + testDataFactory.getOwnerToken())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/items")
                .then()
                .statusCode(201);
    }

    //POST /items
    //400 Bad Request Brakujące lub nieprawidłowe pola
    @Test
    void missingTitle() {
        var requestBody = """
                {
                    "content": "Treść"
                }
                """;

        given()
                .header("Authorization", "Bearer " + testDataFactory.getOwnerToken())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/items")
                .then()
                .statusCode(400)
                .body(containsString("Tytuł jest wymagany"));
    }

    //POST /items
    //401 Unauthorized Brak lub nieważny token JWT
    @Test
    void postItemsRequestMissingToken() {
        var requestBody = """
                {
                    "title": "Tytuł",
                    "content": "Treść"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/items")
                .then()
                .statusCode(401)
                .body(containsString("Brak lub niewazny token JWT"));
    }
}