package com.betacom.betacom.integration;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class ItemPatchIT extends BaseIT {

    @Autowired
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp(){
        testDataFactory.cleanDatabase();
        testDataFactory.createTestUsers();
        testDataFactory.createTestItems();
    }

    //PATCH /items/{id}
    //200 OK Notatka zaktualizowana, w response nowa wartość version
    @Test
    void itemShouldBeUpdated() {
        var requestBody = """
                {
                    "title": "Nowy tytuł",
                    "content": "Nowa zawartość",
                    "version": 0
                }
                """;

        given()
                .header("Authorization", "Bearer " + testDataFactory.getOwnerToken())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch("/items/" + testDataFactory.getItemUuid())
                .then()
                .statusCode(200)
                .body("title", equalTo("Nowy tytuł"))
                .body("content", equalTo("Nowa zawartość"))
                .body("version", equalTo(1));

    }

    //PATCH /items/{id}
    //200 OK Notatka zaktualizowana, w response nowa wartość version
    @Test
    void itemShouldBeUpdatedWhenRequestContainsOnlyVersion() {
        var requestBody = """
                {
                    "version": 0
                }
                """;

        given()
                .header("Authorization", "Bearer " + testDataFactory.getOwnerToken())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch("/items/" + testDataFactory.getItemUuid())
                .then()
                .statusCode(200)
                .body("version", equalTo(1));
    }

    //PATCH /items/{id}
    //400 Bad Request Brak pola version lub nieprawidłowe dane
    @Test
    void versionIsMissing() {
        var requestBody = """
                {
                    "title": "Nowy tytuł",
                    "content": "Nowa zawartość"
                }
                """;

        given()
                .header("Authorization", "Bearer " + testDataFactory.getOwnerToken())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch("/items/" + testDataFactory.getItemUuid())
                .then()
                .statusCode(400);
    }

    //PATCH /items/{id}
    //401 Unauthorized Brak lub nieważny token JWT
    @Test
    void patchItemsRequestMissingToken() {
        var requestBody = """
                {
                    "title": "Nowy tytuł",
                    "content": "Nowa zawartość",
                    "version": 0
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch("/items/" + testDataFactory.getItemUuid())
                .then()
                .statusCode(401)
                .body(containsString("Brak lub niewazny token JWT"));
    }


    //PATCH /items/{id}
    //403 Forbidden Brak uprawnień do edycji tej notatki (VIEWER lub obcy zasób)
    @Test
    void viewerIsNotPermittedToPatch() {
        var login = """
                {
                    "login": "viewer",
                    "password": "test9012"
                }
                """;

        var requestBody = """
                {
                    "title": "Nowy tytuł",
                    "content": "Nowa zawartość",
                    "version": 0
                }
                """;

        given()
                .header("Authorization", "Bearer " + testDataFactory.getOwnerToken())
                .contentType(ContentType.JSON)
                .when()
                .get("/items")
                .then()
                .statusCode(200)
                .body("", hasSize(2));

        given()
                .header("Authorization", "Bearer " + testDataFactory.getToken(login))
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch("/items/" + testDataFactory.getItemUuid())
                .then()
                .statusCode(403);
    }

    //PATCH /items/{id}
    //404 Not Found Notatka nie istnieje lub jest usunięta
    @Test
    void itemNotFound() {
        var requestBody = """
                {
                    "title": "Nowy tytuł",
                    "content": "Nowa zawartość",
                    "version": 0
                }
                """;

        given()
                .header("Authorization", "Bearer " + testDataFactory.getOwnerToken())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch("/items/" + "48105c47-4053-4a0e-a78e-7d3b391ba0e9")
                .then()
                .statusCode(404);
    }

    //PATCH /items/{id}
    //409 Conflict Konflikt wersji – ktoś inny zmodyfikował notatkę w międzyczasie.
    // Treść błędu powinna zawierać aktualną version
    @Test
    void versionConflict() {
        var requestBody = """
                {
                    "title": "Nowy tytuł",
                    "content": "Nowa zawartość",
                    "version": 1
                }
                """;

        given()
                .header("Authorization", "Bearer " + testDataFactory.getOwnerToken())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch("/items/" + testDataFactory.getItemUuid())
                .then()
                .statusCode(409)
                .body("version", equalTo(0));
    }
}