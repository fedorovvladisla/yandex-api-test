package base;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

public abstract class BaseTest {
    protected static String MY_TOKEN;
    protected final static String BASE_URL = "https://cloud-api.yandex.net";
    protected final static String INVALID_TOKEN = "invalid_token";
    protected final static String BASE_PATH = "disk:/autotest_example";

    protected List<String> createdResources = new ArrayList<>();

    @BeforeAll
    static void setup() {
        MY_TOKEN = System.getenv("YA_DISK_TOKEN");
        if (MY_TOKEN == null || MY_TOKEN.isEmpty()) {
            throw new IllegalStateException("Переменная окружения YA_DISK_TOKEN не задана");
        }
        RestAssured.baseURI = BASE_URL;

        Response check = given()
                .header("Authorization", "OAuth " + MY_TOKEN)
                .queryParam("path", BASE_PATH)
                .when()
                .get("/v1/disk/resources");
        if (check.statusCode() == 404) {
            given()
                    .header("Authorization", "OAuth " + MY_TOKEN)
                    .queryParam("path", BASE_PATH)
                    .when()
                    .put("/v1/disk/resources")
                    .then().statusCode(201);
        }
    }

    protected Response get(String path, String... queryParams) {
        var request = given()
                .header("Authorization", "OAuth " + MY_TOKEN)
                .log().method()
                .log().uri();
        for (int i = 0; i < queryParams.length; i += 2) {
            request.queryParam(queryParams[i], queryParams[i+1]);
        }
        return request.when().get(path).then().log().ifError().extract().response();
    }

    protected Response post(String path, String... queryParams) {
        var request = given()
                .header("Authorization", "OAuth " + MY_TOKEN)
                .log().method()
                .log().uri();
        for (int i = 0; i < queryParams.length; i += 2) {
            request.queryParam(queryParams[i], queryParams[i+1]);
        }
        return request.when().post(path).then().log().ifError().extract().response();
    }

    protected Response put(String url, byte[] body) {
        return given()
                .header("Authorization", "OAuth " + MY_TOKEN)
                .contentType("application/octet-stream")
                .body(body)
                .log().method()
                .log().uri()
                .when()
                .put(url)
                .then().log().ifError()
                .extract().response();
    }

    protected Response delete(String path, String... queryParams) {
        var request = given()
                .header("Authorization", "OAuth " + MY_TOKEN)
                .log().method()
                .log().uri();
        for (int i = 0; i < queryParams.length; i += 2) {
            request.queryParam(queryParams[i], queryParams[i+1]);
        }
        return request.when().delete(path).then().log().ifError().extract().response();
    }

    protected Response patch(String path, Object body, String... queryParams) {
        var request = givenAuth()
                .contentType(ContentType.JSON);
        for (int i = 0; i < queryParams.length; i += 2) {
            request.queryParam(queryParams[i], queryParams[i+1]);
        }
        if (body != null) {
            request.body(body);
        }
        return request.when().patch(path).then().log().ifError().extract().response();
    }

    protected void createFolder(String path) {
        Response response = given()
                .header("Authorization", "OAuth " + MY_TOKEN)
                .queryParam("path", path)
                .log().method()
                .log().uri()
                .when()
                .put("/v1/disk/resources")
                .then().log().ifError()
                .extract().response();
        response.then().statusCode(201);
        createdResources.add(path);
    }

    protected String getUploadUrl(String filePath, boolean overwrite) {
        Response response = get("/v1/disk/resources/upload",
                "path", filePath,
                "overwrite", String.valueOf(overwrite));
        response.then().statusCode(200);
        return response.jsonPath().getString("href");
    }

    protected void uploadFile(String filePath, byte[] content) {
        String uploadUrl = getUploadUrl(filePath, false);
        Response response = put(uploadUrl, content);
        response.then().statusCode(201);
        createdResources.add(filePath);
    }

    protected boolean resourceExists(String path) {
        Response response = get("/v1/disk/resources", "path", path);
        return response.statusCode() == 200;
    }

    protected void ensureFolderExists(String path) {
        if (!resourceExists(path)) {
            createFolder(path);
        }
    }

    protected byte[] downloadFile(String filePath) {
        Response linkResponse = givenAuth()
                .queryParam("path", filePath)
                .get("/v1/disk/resources/download");
        linkResponse.then().statusCode(200);
        String downloadUrl = linkResponse.jsonPath().getString("href");

        return given()
                .log().method()
                .log().uri()
                .when()
                .get(downloadUrl)
                .then().log().ifError()
                .statusCode(200)
                .extract().asByteArray();
    }

    protected void ensureResourceDeleted(String path) {
        if (resourceExists(path)) {
            delete("/v1/disk/resources", "path", path, "permanently", "true");
        }
    }

    protected void deleteResource(String path) {
        delete("/v1/disk/resources", "path", path, "permanently", "false");
    }

    protected void sleep(int millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    protected RequestSpecification givenAuth() {
        return given()
                .header("Authorization", "OAuth " + MY_TOKEN)
                .log().ifValidationFails();
    }
    protected RequestSpecification givenInvalidAuth() {
        return given()
                .header("Authorization", "OAuth " + INVALID_TOKEN)
                .log().ifValidationFails();
    }

    protected RequestSpecification givenNoAuth() {
        return given().log().ifValidationFails();
    }

    @AfterEach
    void cleanup() {
        for (String resource : createdResources) {
            delete("/v1/disk/resources", "path", resource, "permanently", "true");
        }
        createdResources.clear();
    }
}