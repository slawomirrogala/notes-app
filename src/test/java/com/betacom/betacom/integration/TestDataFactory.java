package com.betacom.betacom.integration;

import com.betacom.betacom.entity.Item;
import com.betacom.betacom.entity.ItemPermission;
import com.betacom.betacom.entity.User;
import com.betacom.betacom.enums.PermissionRole;
import com.betacom.betacom.exception.UserNotFoundException;
import com.betacom.betacom.repository.ItemPermissionRepository;
import com.betacom.betacom.repository.ItemRepository;
import com.betacom.betacom.repository.UserRepository;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Component
public class TestDataFactory {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemPermissionRepository itemPermissionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String cachedToken;

    @Transactional
    public void createTestUsers() {
        userRepository.saveAll(List.of(
                createUser("owner", "test1234"),
                createUser("editor", "test5678"),
                createUser("viewer", "test9012"),
                createUser("userWithoutPermission", "test3456")
        ));
    }

    private User createUser(String username, String password) {
        return User.builder()
                .login(username)
                .password(passwordEncoder.encode(password))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Transactional
    public void createTestItems() {
        var owner = userRepository.findByLogin("owner")
                .orElseThrow(() -> new IllegalStateException("Uruchom najpierw createTestUsers!"));

        var editor = userRepository.findByLogin("editor")
                .orElseThrow(() -> new IllegalStateException("Uruchom najpierw createTestUsers!"));

        var viewer = userRepository.findByLogin("viewer")
                .orElseThrow(() -> new IllegalStateException("Uruchom najpierw createTestUsers!"));

        var items = List.of(
                buildItem(owner, "Tytuł1", "Treść1", false),
                buildItem(owner, "Tytuł2", "Treść2", false),
                buildItem(owner, "Usunięta notatka", "Treść usuniętej notatki", true)
        );

        var savedItems = itemRepository.saveAll(items);

        itemPermissionRepository.saveAll(saveItemPermissions(savedItems, owner, PermissionRole.OWNER));
        itemPermissionRepository.saveAll(saveItemPermissions(savedItems, editor, PermissionRole.EDITOR));
        itemPermissionRepository.saveAll(saveItemPermissions(savedItems, viewer, PermissionRole.VIEWER));
    }

    @Transactional
    public String getItemUuid() {
        return itemRepository.findAll()
                .stream()
                .filter(item -> !item.getDeleted())
                .toList()
                .getFirst().getId().toString();
    }


    public String getOwnerToken() {
        if (cachedToken == null) {
            var loginBody = """
                {
                    "login": "owner",
                    "password": "test1234"
                }
                """;
            cachedToken = getToken(loginBody);
        }
        return cachedToken;
    }

    public String getToken(String requestBody) {
        return given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/users/login")
                .then()
                .statusCode(200).extract().path("token");
    }

    public void cleanDatabase() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        jdbcTemplate.execute("TRUNCATE TABLE items_aud");

        jdbcTemplate.execute("TRUNCATE TABLE revinfo");

        jdbcTemplate.execute("TRUNCATE TABLE items");
        jdbcTemplate.execute("TRUNCATE TABLE item_permissions");
        jdbcTemplate.execute("TRUNCATE TABLE users");

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    private List<ItemPermission> saveItemPermissions(List <Item> items, User user, PermissionRole role) {
        return items.stream()
                .map(item -> ItemPermission.builder()
                        .item(item)
                        .user(user)
                        .role(role)
                        .build())
                .toList();
    }

    private Item buildItem(User owner,
                           String title,
                           String content,
                           Boolean deleted) {

        return Item.builder()
                .owner(owner)
                .title(title)
                .content(content)
                .deleted(deleted)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(0)
                .build();
    }

    public UUID getUserId(String login) {
        return userRepository.findByLogin(login)
                .map(User::getId)
                .orElseThrow(() -> new UserNotFoundException("Nie znaleziono użytkownika: " + login));
    }
}