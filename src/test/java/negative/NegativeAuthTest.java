package negative;

import base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class NegativeAuthTest extends BaseTest {

    @Test
    @DisplayName("GET /v1/disk с невалидным токеном — ожидаем 401")
    void getDiskInfo_invalidToken_returns401() {
        Response response = givenInvalidAuth()
                .get("/v1/disk");
        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("PUT /v1/disk/resources с невалидным токеном — ожидаем 401")
    void createFolder_invalidToken_returns401() {
        Response response = givenInvalidAuth()
                .queryParam("path", BASE_PATH + "/some_folder")
                .put("/v1/disk/resources");
        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("GET /v1/disk без токена — ожидаем 401")
    void getDiskInfo_noAuth_returns401() {
        Response response = givenNoAuth().get("/v1/disk");
        assertThat(response.statusCode()).isEqualTo(401);
    }
}