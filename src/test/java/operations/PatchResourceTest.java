package operations;

import base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

public class PatchResourceTest extends BaseTest {

    @Test
    @DisplayName("PATCH /v1/disk/resources – добавить кастомные свойства папке")
    void addCustomPropertiesToFolder() {
        String folder = BASE_PATH + "/patch_folder_" + System.currentTimeMillis();
        createFolder(folder);

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, String> customProps = new HashMap<>();
        customProps.put("testKey", "testValue");
        customProps.put("author", "AQA Engineer");
        requestBody.put("custom_properties", customProps);

        Response patchResponse = patch("/v1/disk/resources", requestBody, "path", folder);
        assertThat(patchResponse.statusCode()).isEqualTo(200);

        Response getResponse = get("/v1/disk/resources", "path", folder);
        assertThat(getResponse.jsonPath().getMap("custom_properties"))
                .containsEntry("testKey", "testValue")
                .containsEntry("author", "AQA Engineer");
    }

    @Test
    @DisplayName("PATCH /v1/disk/resources – обновить кастомные свойства")
    void updateCustomProperties() {
        String folder = BASE_PATH + "/patch_update_" + System.currentTimeMillis();
        createFolder(folder);

        Map<String, Object> addBody = new HashMap<>();
        addBody.put("custom_properties", Map.of("key1", "value1"));
        patch("/v1/disk/resources", addBody, "path", folder).then().statusCode(200);

        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("custom_properties", Map.of("key1", "newValue"));
        Response patchResponse = patch("/v1/disk/resources", updateBody, "path", folder);
        assertThat(patchResponse.statusCode()).isEqualTo(200);

        Response getResponse = get("/v1/disk/resources", "path", folder);
        assertThat(getResponse.jsonPath().getString("custom_properties.key1")).isEqualTo("newValue");
    }

    @Test
    @DisplayName("PATCH /v1/disk/resources – несуществующий ресурс → 404")
    void patchNonExistentResource() {
        Map<String, Object> body = Map.of("custom_properties", Map.of("key", "value"));
        Response response = patch("/v1/disk/resources", body, "path", BASE_PATH + "/nonexistent_" + System.currentTimeMillis());
        assertThat(response.statusCode()).isEqualTo(404);
    }
}