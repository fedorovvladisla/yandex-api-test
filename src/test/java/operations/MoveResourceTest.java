package operations;

import base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class MoveResourceTest extends BaseTest {

    @Test
    @DisplayName("POST /v1/disk/resources/move – переместить файл")
    void moveFile() {
        String folderSrc = BASE_PATH + "/move_src_" + System.currentTimeMillis();
        createFolder(folderSrc);
        String file = folderSrc + "/move_me.txt";
        uploadFile(file, "move".getBytes());

        String folderDst = BASE_PATH + "/move_dst_" + System.currentTimeMillis();
        createFolder(folderDst);
        String target = folderDst + "/moved.txt";

        Response moveResponse = post("/v1/disk/resources/move",
                "from", file,
                "path", target);
        assertThat(moveResponse.statusCode()).isEqualTo(201);

        assertThat(resourceExists(file)).isFalse();
        assertThat(resourceExists(target)).isTrue();
    }
}