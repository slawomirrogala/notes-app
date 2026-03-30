package com.betacom.betacom.integration;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;

public class ItemDeleteIT extends BaseIT {

    @Autowired
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp(){
        testDataFactory.cleanDatabase();
        testDataFactory.createTestUsers();
        testDataFactory.createTestItems();
    }

    //DELETE /items/{id}
    //204 No Content Notatka miękko usunięta
    @Test
    void itemShouldBeSoftDeleted() {
        given()
                .header("Authorization", "Bearer " + testDataFactory.getOwnerToken())
                .contentType(ContentType.JSON)
                .when()
                .delete("/items/" + testDataFactory.getItemUuid())
                .then()
                .statusCode(204);

        //sprawdzenie czy liczba item się zmieniła
        given()
                .header("Authorization", "Bearer " + testDataFactory.getOwnerToken())
                .contentType(ContentType.JSON)
                .when()
                .get("/items")
                .then()
                .statusCode(200)
                .body("", hasSize(1));

    }

    //DELETE /items/{id}
    //401 Unauthorized Brak lub nieważny token JWT
    @Test
    void deleteItemsRequestMissingToken() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/items/" + testDataFactory.getItemUuid())
                .then()
                .statusCode(401)
                .body(containsString("Brak lub niewazny token JWT"));
    }

    //DELETE /items/{id}
    //403 Forbidden Użytkownik nie jest ownerem notatki
    @Test
    void viewerIsNotPermittedToDelete() {
        var login = """
                {
                    "login": "viewer",
                    "password": "test9012"
                }
                """;

        given()
                .header("Authorization", "Bearer " + testDataFactory.getToken(login))
                .contentType(ContentType.JSON)
                .when()
                .delete("/items/" + testDataFactory.getItemUuid())
                .then()
                .statusCode(403)
                .body(containsString("Brak dostępu do zasobu"));
    }

    //DELETE /items/{id}
    //403 Forbidden Użytkownik nie jest ownerem notatki
    @Test
    void editorIsNotPermittedToDelete() {
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
                .delete("/items/" + testDataFactory.getItemUuid())
                .then()
                .statusCode(403)
                .body(containsString("Brak dostępu do zasobu"));
    }


    //DELETE /items/{id}
    //404 Not Found Notatka nie istnieje lub jest już usunięta
    @Test
    void itemNotFound() {
        given()
                .header("Authorization", "Bearer " + testDataFactory.getOwnerToken())
                .contentType(ContentType.JSON)
                .when()
                .delete("/items/" + "48105c47-4053-4a0e-a78e-7d3b391ba0e9")
                .then()
                .statusCode(404);
    }
}
