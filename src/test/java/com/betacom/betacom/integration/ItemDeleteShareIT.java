package com.betacom.betacom.integration;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;

public class ItemDeleteShareIT extends BaseIT {

    @Autowired
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        testDataFactory.cleanDatabase();
        testDataFactory.createTestUsers();
        testDataFactory.createTestItems();
    }

    //DELETE /items/{id}/share/{userId}
    //204 No Content Dostęp cofnięty
    @Test
    void shouldDeleteAccess() {
        //editor powinien dostać listę 2 elementów
        var login = """
                {
                    "login": "editor",
                    "password": "test5678"
                }
                """;

        given()
                .header("Authorization", "Bearer " + testDataFactory.getToken(login))
                .contentType(ContentType.JSON)
                .when()
                .get("/items")
                .then()
                .statusCode(200)
                .body("", hasSize(2));

        //owner odbiera uprawnienia
        given()
                .header("Authorization", "Bearer " + testDataFactory.getOwnerToken())
                .contentType(ContentType.JSON)
                .when()
                .delete("/items/" + testDataFactory.getItemUuid() + "/share/" + testDataFactory.getUserId("editor"))
                .then()
                .statusCode(204);

        //użytkownik dostaje listę z jednym elementem
        given()
                .header("Authorization", "Bearer " + testDataFactory.getToken(login))
                .contentType(ContentType.JSON)
                .when()
                .get("/items")
                .then()
                .statusCode(200)
                .body("", hasSize(1));
    }

    //POST /items/{id}/share
    //401 Unauthorized Brak lub nieważny token JWT
    @Test
    void deleteItemShareRequestMissingToken() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/items/" + testDataFactory.getItemUuid() + "/share/" + testDataFactory.getUserId("editor"))
                .then()
                .statusCode(401)
                .body(containsString("Brak lub niewazny token JWT"));
    }

    //POST /items/{id}/share
    //403 Forbidden Tylko owner może zarządzać dostępem
    @Test
    void editorIsNotOwner() {
        var login = """
                {
                    "login": "editor",
                    "password": "test5678"
                }
                """;

        given()
                .header("Authorization", "Bearer " + testDataFactory.getToken(login))
                .contentType(ContentType.JSON)
                .when()
                .delete("/items/" + testDataFactory.getItemUuid() + "/share/" + testDataFactory.getUserId("editor"))
                .then()
                .statusCode(403);
    }

    //POST /items/{id}/share
    //404 Not Found Notatka nie istnieje lub użytkownik nie ma nadanego dostępu
    @Test
    void itemNotFound() {
        given()
                .header("Authorization", "Bearer " + testDataFactory.getOwnerToken())
                .contentType(ContentType.JSON)
                .when()
                .delete("/items/" + UUID.randomUUID() + "/share/" + testDataFactory.getUserId("editor"))
                .then()
                .statusCode(404);
    }

}