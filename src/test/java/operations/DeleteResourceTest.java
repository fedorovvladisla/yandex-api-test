package operations;

import base.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteResourceTest extends BaseTest {

    @Test
    @DisplayName("DELETE /v1/disk/resources – удалить папку безвозвратно")
    void deleteFolder() {
        String folder = BASE_PATH + "/to_delete_" + System.currentTimeMillis();
        createFolder(folder);
        createdResources.remove(folder);

        var response = delete("/v1/disk/resources", "path", folder, "permanently", "true");
        assertThat(response.statusCode()).isEqualTo(204);

        assertThat(resourceExists(folder)).isFalse();
    }
}