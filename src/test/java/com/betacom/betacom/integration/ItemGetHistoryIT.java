package com.betacom.betacom.integration;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class ItemGetHistoryIT extends BaseIT {

    @Autowired
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        testDataFactory.cleanDatabase();
        testDataFactory.createTestUsers();
        testDataFactory.createTestItems();
    }

    //GET /items/{id}/history
    //200 OK Lista rewizji
    @Test
    void shouldGetListOfItemHistory() {
        var itemUuid = testDataFactory.getItemUuid();
        var requestBody = """
                {
                    "title": "Nowy tytuł",
                    "content": "Nowa zawartość",
                    "version": 0
                }
                """;
        var login = """
                {
                    "login": "editor",
                    "password": "test5678"
                }
                """;


        //Utworzony item jest już w bazie dlatego teraz editor zmiania item
        given()
                .header("Authorization", "Bearer " + testDataFactory.getToken(login))
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch("/items/" + itemUuid)
                .then()
                .statusCode(200)
                .body("title", equalTo("Nowy tytuł"))
                .body("content", equalTo("Nowa zawartość"))
                .body("version", equalTo(1));

        //Teraz usuwam item
        given()
                .header("Authorization", "Bearer " + testDataFactory.getOwnerToken())
                .contentType(ContentType.JSON)
                .when()
                .delete("/items/" + itemUuid)
                .then()
                .statusCode(204);

        //Powinienem dostać listę 3 obiektów
        given()
                .header("Authorization", "Bearer " + testDataFactory.getToken(login))
                .contentType(ContentType.JSON)
                .when()
                .get("/items/" + itemUuid + "/history")
                .then()
                .statusCode(200)
                .body("", hasSize(3))
                .body("[1].version", equalTo(1))
                .body("[1].revisionType", equalTo("MOD"))
                .body("[1].changedBy", equalTo("editor"))
                .body("[2].version", equalTo(2))
                .body("[2].revisionType", equalTo("MOD"))
                .body("[2].changedBy", equalTo("owner"));
    }

    //GET /items/{id}/history
    //401 Unauthorized Brak lub nieważny token JWT
    @Test
    void getItemHistoryRequestMissingToken() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/items/" + testDataFactory.getItemUuid() + "/history")
                .then()
                .statusCode(401)
                .body(containsString("Brak lub niewazny token JWT"));
    }

    //GET /items/{id}/history
    //403 Forbidden Brak dostępu do tej notatki
    @Test
    void userWithoutPermissionIsNotPermittedToGetItemHistory() {
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
                .get("/items/" + testDataFactory.getItemUuid() + "/history")
                .then()
                .statusCode(403)
                .body(containsString("Brak dostępu do tej notatki"));
    }


    //GET /items/{id}/history
    //404 Not Found Notatka nie istnieje
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