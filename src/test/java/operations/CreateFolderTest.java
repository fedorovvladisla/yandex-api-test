package operations;

import base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateFolderTest extends BaseTest {

    @Test
    @DisplayName("PUT /v1/disk/resources – создать папку")
    void createFolder() {
        String folder = BASE_PATH + "/new_folder_" + System.currentTimeMillis();
        createFolder(folder);
        assertThat(resourceExists(folder)).isTrue();
    }

    @Test
    @DisplayName("PUT /v1/disk/resources – создать вложенную папку (родительская создана заранее)")
    void createNestedFolder() {
        String parent = BASE_PATH + "/parent_" + System.currentTimeMillis();
        createFolder(parent);
        String nested = parent + "/child";
        createFolder(nested);
        assertThat(resourceExists(nested)).isTrue();
    }

    @Test
    @DisplayName("PUT /v1/disk/resources – создать вложенную папку без родителя 409")
    void createNestedFolderWithoutParent() {
        String nested = BASE_PATH + "/parent_" + System.currentTimeMillis() + "/child";
        Response response = givenAuth()
                .queryParam("path", nested)
                .put("/v1/disk/resources");
        assertThat(response.statusCode()).isEqualTo(409);
    }

    @Test
    @DisplayName("PUT /v1/disk/resources – создать существующую папку  409")
    void createDuplicateFolder() {
        String folder = BASE_PATH + "/dup_" + System.currentTimeMillis();
        createFolder(folder);
        Response response = givenAuth()
                .queryParam("path", folder)
                .put("/v1/disk/resources")
                .then().log().ifError()
                .extract().response();
        assertThat(response.statusCode()).isEqualTo(409);
    }

    @Test
    @DisplayName("PUT /v1/disk/resources – невалидный путь (пустая строка)  400")
    void createFolderInvalidPath() {
        Response response = givenAuth()
                .queryParam("path", "")
                .put("/v1/disk/resources");
        assertThat(response.statusCode()).isBetween(400, 404);
    }

    @Test
    @DisplayName("PUT /v1/disk/resources – невалидный токен  401")
    void createFolderInvalidToken() {
        Response response = givenInvalidAuth()
                .queryParam("path", BASE_PATH + "/some_folder")
                .put("/v1/disk/resources");
        assertThat(response.statusCode()).isEqualTo(401);
    }
}