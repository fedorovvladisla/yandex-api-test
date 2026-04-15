package operations;

import base.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GetResourcesTest extends BaseTest {

    @Test
    @DisplayName("GET /v1/disk/resources – метаданные папки")
    void getFolderMetadata() {
        String folder = BASE_PATH + "/meta_folder_" + System.currentTimeMillis();
        createFolder(folder);

        var response = get("/v1/disk/resources", "path", folder);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("type")).isEqualTo("dir");
        assertThat(response.jsonPath().getString("name")).isEqualTo(folder.substring(folder.lastIndexOf('/') + 1));
    }

    @Test
    @DisplayName("GET /v1/disk/resources – метаданные файла")
    void getFileMetadata() {
        String folder = BASE_PATH + "/meta_file_test_" + System.currentTimeMillis();
        createFolder(folder);
        String filePath = folder + "/data.txt";
        byte[] content = "Some content".getBytes();
        uploadFile(filePath, content);

        var response = get("/v1/disk/resources", "path", filePath);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("type")).isEqualTo("file");
        assertThat(response.jsonPath().getLong("size")).isEqualTo(content.length);
    }

    @Test
    @DisplayName("GET /v1/disk/resources – несуществующий ресурс → 404")
    void getNonExistentResource() {
        String path = BASE_PATH + "/non_existent_" + System.currentTimeMillis();
        var response = get("/v1/disk/resources", "path", path);
        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("error")).isEqualTo("DiskNotFoundError");
    }

    @Test
    @DisplayName("GET /v1/disk/resources – с параметром limit (глубина вложенности)")
    void getResourcesWithLimit() {
        String folder = BASE_PATH + "/limit_test_" + System.currentTimeMillis();
        createFolder(folder);
        String sub1 = folder + "/sub1";
        String sub2 = folder + "/sub2";
        createFolder(sub1);
        createFolder(sub2);

        var response = get("/v1/disk/resources", "path", folder, "limit", "1");
        assertThat(response.statusCode()).isEqualTo(200);
        var items = response.jsonPath().getList("_embedded.items");
        assertThat(items).hasSizeLessThanOrEqualTo(1);
    }
}