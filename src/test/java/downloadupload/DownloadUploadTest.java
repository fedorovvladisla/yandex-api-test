package downloadupload;

import base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DownloadUploadTest extends BaseTest {

    @Test
    @DisplayName("PUT – загрузить файл")
    void uploadFile() {
        String folder = BASE_PATH + "/upload_test_" + System.currentTimeMillis();
        createFolder(folder);
        String filePath = folder + "/hello.txt";
        byte[] content = "Hello from Yandex Disk".getBytes();

        uploadFile(filePath, content);

        assertThat(resourceExists(filePath)).isTrue();
        var response = get("/v1/disk/resources", "path", filePath);
        assertThat(response.jsonPath().getLong("size")).isEqualTo(content.length);
    }

    @Test
    @DisplayName("PUT – загрузить файл с перезаписью существующего")
    void uploadFileOverwrite() {
        String folder = BASE_PATH + "/upload_overwrite_" + System.currentTimeMillis();
        createFolder(folder);
        String filePath = folder + "/test.txt";
        byte[] content1 = "First version".getBytes();
        byte[] content2 = "Second version".getBytes();

        uploadFile(filePath, content1);
        String uploadUrl = getUploadUrl(filePath, true);
        Response putResponse = givenAuth()
                .contentType("application/octet-stream")
                .body(content2)
                .when()
                .put(uploadUrl);
        assertThat(putResponse.statusCode()).isEqualTo(201);
    }

    @Test
    @DisplayName("PUT – загрузка в несуществующую папку → 409")
    void uploadToNonExistentFolder() {
        String folder = BASE_PATH + "/no_such_folder_" + System.currentTimeMillis();
        String filePath = folder + "/file.txt";
        byte[] content = "Content".getBytes();

        Response response = givenAuth()
                .queryParam("path", filePath)
                .get("/v1/disk/resources/upload");
        assertThat(response.statusCode()).isEqualTo(409);
        assertThat(response.jsonPath().getString("error")).isEqualTo("DiskPathDoesntExistsError");
    }

    @Test
    @DisplayName("PUT – загрузить пустой файл")
    void uploadEmptyFile() {
        String folder = BASE_PATH + "/empty_test_" + System.currentTimeMillis();
        createFolder(folder);
        String filePath = folder + "/empty.txt";
        byte[] emptyContent = new byte[0];

        uploadFile(filePath, emptyContent);
        assertThat(resourceExists(filePath)).isTrue();
        var response = get("/v1/disk/resources", "path", filePath);
        assertThat(response.jsonPath().getLong("size")).isEqualTo(0);
    }
}