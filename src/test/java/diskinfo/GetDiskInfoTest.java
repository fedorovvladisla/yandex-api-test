package diskinfo;

import base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GetDiskInfoTest extends BaseTest {

    @Test
    @DisplayName("GET /v1/disk – получить информацию о диске")
    void getDiskInfo() {
        var response = get("/v1/disk");
        assertEquals(200, response.statusCode());
        assertNotNull(response.jsonPath().getString("user.login"));
        assertTrue(response.jsonPath().getLong("total_space") > 0);
        assertNotNull(response.jsonPath().getString("system_folders.downloads"));
        assertNotNull(response.jsonPath().getString("system_folders.photostream"));
        assertTrue(response.jsonPath().getLong("used_space") >= 0);
    }

    @Test
    @DisplayName("GET /v1/disk – невалидный токен → 401")
    void getDiskInfoInvalidToken() {
        Response response = givenInvalidAuth()
                .get("/v1/disk");
        assertEquals(401, response.statusCode());
    }

    @Test
    @DisplayName("GET /v1/disk – без токена → 401")
    void getDiskInfoNoAuth() {
        Response response = givenNoAuth()
                .get("/v1/disk");
        assertEquals(401, response.statusCode());
    }
}