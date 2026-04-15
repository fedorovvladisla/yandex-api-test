package trash;

import base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TrashTest extends BaseTest {

    private static final String TEST_DIR = BASE_PATH + "/trash_tests";
    private static final String RESTORE_FOLDER_NAME = "restore_test_folder";
    private static final String RESTORE_FOLDER = TEST_DIR + "/" + RESTORE_FOLDER_NAME;
    private static final String CLEAR_FOLDER_NAME = "clear_test_folder";
    private static final String CLEAR_FOLDER = TEST_DIR + "/" + CLEAR_FOLDER_NAME;

    @BeforeAll
    static void setUpTestData() {
        TrashTest instance = new TrashTest();
        instance.ensureFolderExists(BASE_PATH);
        instance.ensureFolderExists(TEST_DIR);

        instance.ensureResourceDeleted(RESTORE_FOLDER);
        instance.ensureFolderExists(RESTORE_FOLDER);
        instance.sleep(500);
        instance.deleteResource(RESTORE_FOLDER);
        instance.sleep(1500);

        instance.ensureResourceDeleted(CLEAR_FOLDER);
        instance.ensureFolderExists(CLEAR_FOLDER);
        instance.sleep(500);
        instance.deleteResource(CLEAR_FOLDER);
        instance.sleep(1500);
    }

    @AfterAll
    static void cleanUpTestData() {
        TrashTest instance = new TrashTest();
        instance.ensureResourceDeleted(RESTORE_FOLDER);
        instance.ensureResourceDeleted(TEST_DIR);
    }

    @Test
    @Order(10)
    @DisplayName("PUT Восстановление ресурса из Корзины (существующий ресурс) — 201")
    void restoreFromTrash_existingResource_returns201() {
        Response trashContents = givenAuth().get("/v1/disk/trash/resources");
        if (trashContents.statusCode() == 200) {
            var items = trashContents.jsonPath().getList("_embedded.items");
            if (items != null && !items.isEmpty()) {
                String trashPath = trashContents.jsonPath().getString("_embedded.items[0].path");
                String originalPath = trashContents.jsonPath().getString("_embedded.items[0].origin_path");
                if (trashPath != null) {
                    if (originalPath != null) ensureResourceDeleted(originalPath);
                    Response response = givenAuth()
                            .queryParam("path", trashPath)
                            .put("/v1/disk/trash/resources/restore");
                    assertTrue(response.statusCode() == 201 || response.statusCode() == 202);
                    if (originalPath != null) {
                        sleep(1000);
                        ensureResourceDeleted(originalPath);
                    }
                    return;
                }
            }
        }
        System.out.println("Корзина пуста, тест восстановления пропущен");
    }
}