package operations;

import base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FlatFilesListTest extends BaseTest {

    private static final String TEST_DIR = BASE_PATH + "/flat_tests";
    private static final String FILE1 = TEST_DIR + "/a_doc.txt";
    private static final String FILE2 = TEST_DIR + "/b_img.png";
    private static final String FILE3 = TEST_DIR + "/c_pdf.pdf";
    private static final String FILE4 = TEST_DIR + "/d_doc2.txt";

    @BeforeEach
    void prepareTestData() {
        ensureFolderExists(TEST_DIR);
        uploadFile(FILE1, "Content of first text file".getBytes());
        uploadFile(FILE2, loadTestImage());
        uploadFile(FILE3, "Dummy PDF content".getBytes());
        uploadFile(FILE4, "Another text file".getBytes());
        sleep(1000);
    }

    private byte[] loadTestImage() {
        try (var is = getClass().getResourceAsStream("/files/sample.png")) {
            if (is != null) return is.readAllBytes();
        } catch (Exception e) {
        }
        return new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                0x08, 0x02, 0x00, 0x00, 0x00, (byte) 0x90, (byte) 0x77, 0x53,
                (byte) 0xDE, 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41,
                0x54, 0x08, (byte) 0xD7, 0x63, (byte) 0xF8, (byte) 0xCF, 0x00, 0x00,
                0x03, 0x01, 0x01, 0x00, 0x18, (byte) 0xDD, (byte) 0x8D, (byte) 0xB0,
                0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
        };
    }

    @Test
    @DisplayName("GET /v1/disk/resources/files – базовый запрос возвращает 200 и список")
    void getFlatFilesList_basic() {
        Response response = givenAuth()
                .get("/v1/disk/resources/files");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("items")).isNotNull();
    }

    @Test
    @DisplayName("GET /v1/disk/resources/files – пагинация limit и offset")
    void getFlatFilesList_withLimitAndOffset() {
        Response limit2 = givenAuth()
                .queryParam("limit", 2)
                .get("/v1/disk/resources/files");
        assertThat(limit2.statusCode()).isEqualTo(200);
        List<?> itemsLimit2 = limit2.jsonPath().getList("items");
        assertThat(itemsLimit2).hasSizeLessThanOrEqualTo(2);

        Response offset1 = givenAuth()
                .queryParam("limit", 1)
                .queryParam("offset", 1)
                .get("/v1/disk/resources/files");
        assertThat(offset1.statusCode()).isEqualTo(200);
        List<?> itemsOffset1 = offset1.jsonPath().getList("items");
        assertThat(itemsOffset1).hasSize(1);
    }

    @Test
    @DisplayName("GET /v1/disk/resources/files – сортировка по имени (sort=name)")
    void getFlatFilesList_sortedByName() {
        Response response = givenAuth()
                .queryParam("sort", "name")
                .get("/v1/disk/resources/files");
        assertThat(response.statusCode()).isEqualTo(200);
        List<String> names = response.jsonPath().getList("items.name");
        assertThat(names).isSorted();
    }

    @Test
    @DisplayName("GET /v1/disk/resources/files – сортировка по дате изменения (sort=modified)")
    void getFlatFilesList_sortedByModified() {
        Response response = givenAuth()
                .queryParam("sort", "modified")
                .get("/v1/disk/resources/files");
        assertThat(response.statusCode()).isEqualTo(200);
        List<String> modifiedDates = response.jsonPath().getList("items.modified");

        for (int i = 0; i < modifiedDates.size() - 1; i++) {
            assertThat(modifiedDates.get(i)).isLessThanOrEqualTo(modifiedDates.get(i+1));
        }
    }

    @Test
    @DisplayName("GET /v1/disk/resources/files – фильтрация по media_type=image")
    void getFlatFilesList_filterByImage() {
        Response response = givenAuth()
                .queryParam("media_type", "image")
                .get("/v1/disk/resources/files");
        assertThat(response.statusCode()).isEqualTo(200);
        List<String> mediaTypes = response.jsonPath().getList("items.media_type");
        assertThat(mediaTypes).allMatch(type -> "image".equals(type));
    }

    @Test
    @DisplayName("GET /v1/disk/resources/files – фильтрация по media_type=document")
    void getFlatFilesList_filterByDocument() {
        Response response = givenAuth()
                .queryParam("media_type", "document")
                .get("/v1/disk/resources/files");
        assertThat(response.statusCode()).isEqualTo(200);
        List<String> mediaTypes = response.jsonPath().getList("items.media_type");
        assertThat(mediaTypes).allMatch(type -> "document".equals(type));
    }


    @Test
    @DisplayName("GET /v1/disk/resources/files – получить плоский список файлов")
    void getFlatFilesList() {
        String folder = BASE_PATH + "/flat_test_" + System.currentTimeMillis();
        createFolder(folder);
        uploadFile(folder + "/file1.txt", "content1".getBytes());
        uploadFile(folder + "/file2.txt", "content2".getBytes());

        Response response = givenAuth()
                .queryParam("limit", 10)
                .get("/v1/disk/resources/files");
        assertThat(response.statusCode()).isEqualTo(200);
        var items = response.jsonPath().getList("items");
        assertThat(items).isNotEmpty();
    }
}
