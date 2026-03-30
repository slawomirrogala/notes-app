package com.betacom.betacom.integration;

import com.betacom.betacom.dto.item.ItemShareRequest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class ItemPostShareIT extends BaseIT {

    @Autowired
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        testDataFactory.cleanDatabase();
        testDataFactory.createTestUsers();
        testDataFactory.createTestItems();
    }

    //POST /items/{id}/share
    //201 Created Dostęp nadany
    @Test
    void shouldShareAccess() {
        //użytkownik bez uprawnień dostaje pustą listę
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

        //nadanie uprawnień
        var itemUuid = testDataFactory.getItemUuid();
        var itemShareRequest = new ItemShareRequest(testDataFactory.getUserId("userWithoutPermission"),"VIEWER");

        given()
                .header("Authorization", "Bearer " + testDataFactory.getOwnerToken())
                .contentType(ContentType.JSON)
                .body(itemShareRequest)
                .when()
                .post("/items/" + itemUuid + "/share")
                .then()
                .statusCode(201)
                .body("role", equalTo("VIEWER"));

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
    //200 OK Rola zaktualizowana (użytkownik już miał dostęp)
    @Test
    void shouldChangeAccess() {
        var itemUuid = testDataFactory.getItemUuid();
        var itemShareRequest = new ItemShareRequest(testDataFactory.getUserId("editor"),"EDITOR");

        given()
                .header("Authorization", "Bearer " + testDataFactory.getOwnerToken())
                .contentType(ContentType.JSON)
                .body(itemShareRequest)
                .when()
                .post("/items/" + itemUuid + "/share")
                .then()
                .statusCode(201)
                .body("role", equalTo("EDITOR"));
    }

    //POST /items/{id}/share
    //400 Bad Request Nieprawidłowa rola lub brakujące pola
    @Test
    void missingRole() {
        var itemShareRequest = new ItemShareRequest(testDataFactory.getUserId("userWithoutPermission"),"TESTER");
        given()
                .header("Authorization", "Bearer " + testDataFactory.getOwnerToken())
                .contentType(ContentType.JSON)
                .body(itemShareRequest)
                .when()
                .post("/items/" + testDataFactory.getItemUuid() + "/share")
                .then()
                .statusCode(400);
    }

    //POST /items/{id}/share
    //401 Unauthorized Brak lub nieważny token JWT
    @Test
    void postItemShareRequestMissingToken() {
        var itemShareRequest = new ItemShareRequest(testDataFactory.getUserId("userWithoutPermission"),"VIEWER");

        given()
                .contentType(ContentType.JSON)
                .body(itemShareRequest)
                .when()
                .post("/items/" + testDataFactory.getItemUuid() + "/share")
                .then()
                .statusCode(401)
                .body(containsString("Brak lub niewazny token JWT"));
    }


    //POST /items/{id}/share
    //403 Forbidden Tylko owner może zarządzać dostępem
    @Test
    void onlyOwnerCanGiveAccess() {
        var viewer = """
                {
                    "login": "viewer",
                    "password": "test9012"
                }
                """;

        var itemUuid = testDataFactory.getItemUuid();
        var itemShareRequest = new ItemShareRequest(testDataFactory.getUserId("userWithoutPermission"),"VIEWER");

        given()
                .header("Authorization", "Bearer " + testDataFactory.getToken(viewer))
                .contentType(ContentType.JSON)
                .body(itemShareRequest)
                .when()
                .post("/items/" + itemUuid + "/share")
                .then()
                .statusCode(403);
    }


    //POST /items/{id}/share
    //404 Not Found Notatka lub użytkownik docelowy nie istnieje
    @Test
    void itemNotExist() {
        var itemShareRequest = new ItemShareRequest(testDataFactory.getUserId("userWithoutPermission"),"VIEWER");

        given()
                .header("Authorization", "Bearer " + testDataFactory.getOwnerToken())
                .contentType(ContentType.JSON)
                .body(itemShareRequest)
                .when()
                .post("/items/" + "48105c47-4053-4a0e-a78e-7d3b391ba0e9" + "/share")
                .then()
                .statusCode(404);
    }

    //POST /items/{id}/share
    //404 Not Found Notatka lub użytkownik docelowy nie istnieje
    @Test
    void targetUserNotExist() {
        var itemUuid = testDataFactory.getItemUuid();
        var itemShareRequest = new ItemShareRequest(UUID.randomUUID(),"VIEWER");

        given()
                .header("Authorization", "Bearer " + testDataFactory.getOwnerToken())
                .contentType(ContentType.JSON)
                .body(itemShareRequest)
                .when()
                .post("/items/" + itemUuid + "/share")
                .then()
                .statusCode(404);
    }
}