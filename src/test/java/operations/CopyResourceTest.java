package operations;

import base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class CopyResourceTest extends BaseTest {

    @Test
    @DisplayName("POST /v1/disk/resources/copy – скопировать файл")
    void copyFile() {
        String folder = BASE_PATH + "/copy_test_" + System.currentTimeMillis();
        createFolder(folder);
        String sourceFile = folder + "/source.txt";
        byte[] content = "test content".getBytes();
        uploadFile(sourceFile, content);

        String targetFile = folder + "/target.txt";

        Response copyResponse = post("/v1/disk/resources/copy",
                "from", sourceFile,
                "path", targetFile);
        assertThat(copyResponse.statusCode()).isEqualTo(201);
        assertThat(resourceExists(targetFile)).isTrue();
    }
}