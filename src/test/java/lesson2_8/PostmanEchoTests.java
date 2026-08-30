package lesson2_8;

import lesson2_8.models.EchoResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class PostmanEchoTests {

    private static final String BASE_URL = "https://postman-echo.com";
    private static final int EXPECTED_STATUS = 200;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    // ============================================================
    // 1. GET-запрос
    // ============================================================
    @Test
    @DisplayName("GET запрос - проверка кода и всех полей ответа")
    public void testGetRequest() {
        System.out.println("\n========== GET Request Test ==========");

        Response response = given()
                .queryParam("param1", "value1")
                .queryParam("param2", "value2")
                .when()
                .get("/get")
                .then()
                .statusCode(EXPECTED_STATUS)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        EchoResponse actualResponse = response.as(EchoResponse.class);
        System.out.println("Response: " + actualResponse);

        assertAll("Проверка GET ответа",
                () -> assertEquals(EXPECTED_STATUS, response.getStatusCode(), "Статус код не совпадает"),
                () -> assertNotNull(actualResponse.getHeaders(), "Headers не должны быть null"),
                () -> assertEquals(BASE_URL + "/get?param1=value1&param2=value2", actualResponse.getUrl(), "URL не совпадает"),
                () -> assertNotNull(actualResponse.getArgs(), "Args не должны быть null"),
                () -> assertEquals("value1", actualResponse.getArgs().get("param1"), "param1 не совпадает"),
                () -> assertEquals("value2", actualResponse.getArgs().get("param2"), "param2 не совпадает")
        );

        System.out.println("✅ GET тест пройден успешно");
    }

    // ============================================================
    // 2. POST-запрос с JSON
    // ============================================================
    @Test
    @DisplayName("POST запрос с JSON - проверка кода и всех полей ответа")
    public void testPostRequestWithJson() {
        System.out.println("\n========== POST Request with JSON Test ==========");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "John Doe");
        requestBody.put("age", 30);
        requestBody.put("active", true);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/post")
                .then()
                .statusCode(EXPECTED_STATUS)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        EchoResponse actualResponse = response.as(EchoResponse.class);
        System.out.println("Response: " + actualResponse);

        assertAll("Проверка POST ответа",
                () -> assertEquals(EXPECTED_STATUS, response.getStatusCode(), "Статус код не совпадает"),
                () -> assertNotNull(actualResponse.getHeaders(), "Headers не должны быть null"),
                () -> assertEquals(BASE_URL + "/post", actualResponse.getUrl(), "URL не совпадает"),
                () -> assertNotNull(actualResponse.getJson(), "Json не должен быть null"),
                () -> assertEquals(requestBody, actualResponse.getJson(), "Json не совпадает"),
                () -> assertNotNull(actualResponse.getData(), "Data не должен быть null")
        );

        System.out.println("✅ POST JSON тест пройден успешно");
    }

    // ============================================================
    // 3. POST-запрос с Form Data
    // ============================================================
    @Test
    @DisplayName("POST запрос с Form Data - проверка кода и всех полей ответа")
    public void testPostRequestWithFormData() {
        System.out.println("\n========== POST Request with Form Data Test ==========");

        Map<String, String> formData = new HashMap<>();
        formData.put("username", "testuser");
        formData.put("password", "secret123");

        Response response = given()
                .contentType(ContentType.URLENC)
                .formParams(formData)
                .when()
                .post("/post")
                .then()
                .statusCode(EXPECTED_STATUS)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        EchoResponse actualResponse = response.as(EchoResponse.class);
        System.out.println("Response: " + actualResponse);

        assertAll("Проверка POST Form Data ответа",
                () -> assertEquals(EXPECTED_STATUS, response.getStatusCode(), "Статус код не совпадает"),
                () -> assertNotNull(actualResponse.getHeaders(), "Headers не должны быть null"),
                () -> assertEquals(BASE_URL + "/post", actualResponse.getUrl(), "URL не совпадает"),
                () -> assertNotNull(actualResponse.getForm(), "Form не должен быть null"),
                () -> assertEquals("testuser", actualResponse.getForm().get("username"), "Username не совпадает"),
                () -> assertEquals("secret123", actualResponse.getForm().get("password"), "Password не совпадает")
        );

        System.out.println("✅ POST Form Data тест пройден успешно");
    }

    // ============================================================
    // 4. PUT-запрос
    // ============================================================
    @Test
    @DisplayName("PUT запрос - проверка кода и всех полей ответа")
    public void testPutRequest() {
        System.out.println("\n========== PUT Request Test ==========");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", 123);
        requestBody.put("name", "Updated Resource");
        requestBody.put("status", "active");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/put")
                .then()
                .statusCode(EXPECTED_STATUS)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        EchoResponse actualResponse = response.as(EchoResponse.class);
        System.out.println("Response: " + actualResponse);

        assertAll("Проверка PUT ответа",
                () -> assertEquals(EXPECTED_STATUS, response.getStatusCode(), "Статус код не совпадает"),
                () -> assertNotNull(actualResponse.getHeaders(), "Headers не должны быть null"),
                () -> assertEquals(BASE_URL + "/put", actualResponse.getUrl(), "URL не совпадает"),
                () -> assertNotNull(actualResponse.getJson(), "Json не должен быть null"),
                () -> assertEquals(requestBody, actualResponse.getJson(), "Json не совпадает"),
                () -> assertEquals(123, actualResponse.getJson().get("id"), "ID не совпадает")
        );

        System.out.println("✅ PUT тест пройден успешно");
    }

    // ============================================================
    // 5. PATCH-запрос
    // ============================================================
    @Test
    @DisplayName("PATCH запрос - проверка кода и всех полей ответа")
    public void testPatchRequest() {
        System.out.println("\n========== PATCH Request Test ==========");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("status", "in-progress");
        requestBody.put("priority", 5);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch("/patch")
                .then()
                .statusCode(EXPECTED_STATUS)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        EchoResponse actualResponse = response.as(EchoResponse.class);
        System.out.println("Response: " + actualResponse);

        assertAll("Проверка PATCH ответа",
                () -> assertEquals(EXPECTED_STATUS, response.getStatusCode(), "Статус код не совпадает"),
                () -> assertNotNull(actualResponse.getHeaders(), "Headers не должны быть null"),
                () -> assertEquals(BASE_URL + "/patch", actualResponse.getUrl(), "URL не совпадает"),
                () -> assertNotNull(actualResponse.getJson(), "Json не должен быть null"),
                () -> assertEquals(requestBody, actualResponse.getJson(), "Json не совпадает"),
                () -> assertEquals("in-progress", actualResponse.getJson().get("status"), "Status не совпадает")
        );

        System.out.println("✅ PATCH тест пройден успешно");
    }

    // ============================================================
    // 6. DELETE-запрос
    // ============================================================
    @Test
    @DisplayName("DELETE запрос - проверка кода и всех полей ответа")
    public void testDeleteRequest() {
        System.out.println("\n========== DELETE Request Test ==========");

        Response response = given()
                .queryParam("id", "789")
                .queryParam("force", "true")
                .when()
                .delete("/delete")
                .then()
                .statusCode(EXPECTED_STATUS)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        EchoResponse actualResponse = response.as(EchoResponse.class);
        System.out.println("Response: " + actualResponse);

        assertAll("Проверка DELETE ответа",
                () -> assertEquals(EXPECTED_STATUS, response.getStatusCode(), "Статус код не совпадает"),
                () -> assertNotNull(actualResponse.getHeaders(), "Headers не должны быть null"),
                () -> assertNotNull(actualResponse.getArgs(), "Args не должны быть null"),
                () -> assertEquals("789", actualResponse.getArgs().get("id"), "ID не совпадает"),
                () -> assertEquals("true", actualResponse.getArgs().get("force"), "Force не совпадает"),
                () -> assertNotNull(actualResponse.getData(), "Data не должен быть null"),
                () -> assertNotNull(actualResponse.getFiles(), "Files не должен быть null")
        );
        System.out.println("✅ DELETE тест пройден успешно");
    }

    // ============================================================
    // 7. GET без параметров
    // ============================================================
    @Test
    @DisplayName("GET запрос без параметров - проверка всех полей")
    public void testGetRequestWithoutParams() {
        System.out.println("\n========== GET Request Without Params Test ==========");

        Response response = given()
                .when()
                .get("/get")
                .then()
                .statusCode(EXPECTED_STATUS)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        EchoResponse actualResponse = response.as(EchoResponse.class);
        System.out.println("Response: " + actualResponse);

        assertAll("Проверка GET без параметров",
                () -> assertEquals(EXPECTED_STATUS, response.getStatusCode(), "Статус код не совпадает"),
                () -> assertEquals(BASE_URL + "/get", actualResponse.getUrl(), "URL не совпадает"),
                () -> assertNotNull(actualResponse.getArgs(), "Args не должны быть null"),
                () -> assertTrue(actualResponse.getArgs().isEmpty(), "Args должен быть пустым")
        );

        System.out.println("✅ GET без параметров тест пройден");
    }
}