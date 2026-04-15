package publics;

import base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PublishTest extends BaseTest {

    private static final String TEST_DIR = BASE_PATH + "/publish_tests";
    private static final String PUBLISH_FOLDER = TEST_DIR + "/publish_folder";
    private static String publicKey;
    private static String publicUrl;

    @BeforeAll
    static void setUp() {
        PublishTest instance = new PublishTest();
        instance.ensureFolderExists(BASE_PATH);
        instance.ensureFolderExists(TEST_DIR);
        instance.ensureFolderExists(PUBLISH_FOLDER);
    }

    @AfterAll
    static void tearDown() {
        PublishTest instance = new PublishTest();
        instance.givenAuth()
                .queryParam("path", PUBLISH_FOLDER)
                .put("/v1/disk/resources/unpublish");
        instance.sleep(500);
        instance.ensureResourceDeleted(TEST_DIR);
    }

    @Test
    @Order(1)
    @DisplayName("PUT Публикация ресурса (существующая папка) — 200")
    void publishFolder() {
        Response response = givenAuth()
                .queryParam("path", PUBLISH_FOLDER)
                .put("/v1/disk/resources/publish");
        assertEquals(200, response.statusCode());
        assertNotNull(response.jsonPath().getString("href"), "href отсутствует");

        sleep(500);


        Response info = givenAuth()
                .queryParam("path", PUBLISH_FOLDER)
                .get("/v1/disk/resources");
        publicKey = info.jsonPath().getString("public_key");
        publicUrl = info.jsonPath().getString("public_url");
        assertNotNull(publicKey, "public_key не получен");
    }

    @Test
    @Order(2)
    @DisplayName("GET Метаинформация публичного ресурса по public_key — 200")
    void getPublicResourceInfo() {
        assertNotNull(publicKey, "public_key отсутствует");
        Response response = given()
                .queryParam("public_key", publicKey)
                .get("/v1/disk/public/resources");
        assertEquals(200, response.statusCode());
        assertNotNull(response.jsonPath().getString("name"));
    }
}