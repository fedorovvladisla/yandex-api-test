package operations;

import base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LastUploadedTest extends BaseTest {

    private static final String TEST_DIR = BASE_PATH + "/last_uploaded_tests";
    private static final String OLD_FILE = TEST_DIR + "/old.txt";
    private static final String MID_FILE = TEST_DIR + "/mid.txt";
    private static final String NEW_FILE = TEST_DIR + "/new.txt";

    @BeforeEach
    void prepareTestData() throws InterruptedException {
        ensureFolderExists(TEST_DIR);
        uploadFile(OLD_FILE, "Old content".getBytes());
        Thread.sleep(2000);
        uploadFile(MID_FILE, "Middle content".getBytes());
        Thread.sleep(2000);
        uploadFile(NEW_FILE, "New content".getBytes());
        Thread.sleep(1000);
    }

    @Test
    @DisplayName("GET /v1/disk/resources/last-uploaded – возвращает файлы от новых к старым")
    void lastUploaded_orderNewestFirst() {
        Response response = givenAuth()
                .get("/v1/disk/resources/last-uploaded");
        assertThat(response.statusCode()).isEqualTo(200);
        List<String> paths = response.jsonPath().getList("items.path");
        int newIndex = paths.indexOf(NEW_FILE);
        int oldIndex = paths.indexOf(OLD_FILE);
        assertThat(newIndex).isLessThan(oldIndex);
    }

    @Test
    @DisplayName("GET /v1/disk/resources/last-uploaded – параметр limit работает")
    void lastUploaded_withLimit() {
        Response response = givenAuth()
                .queryParam("limit", 2)
                .get("/v1/disk/resources/last-uploaded");
        assertThat(response.statusCode()).isEqualTo(200);
        List<?> items = response.jsonPath().getList("items");
        assertThat(items).hasSizeLessThanOrEqualTo(2);
    }

    @Test
    @DisplayName("GET /v1/disk/resources/last-uploaded – фильтрация по media_type")
    void lastUploaded_filterByMediaType() {
        byte[] img = loadTestImage();
        String imgFile = TEST_DIR + "/test_image.png";
        uploadFile(imgFile, img);
        sleep(1000);

        Response response = givenAuth()
                .queryParam("media_type", "image")
                .get("/v1/disk/resources/last-uploaded");
        assertThat(response.statusCode()).isEqualTo(200);
        List<String> mediaTypes = response.jsonPath().getList("items.media_type");
        assertThat(mediaTypes).allMatch(type -> "image".equals(type));
    }

    private byte[] loadTestImage() {
        try (var is = getClass().getResourceAsStream("/files/sample.png")) {
            if (is != null) return is.readAllBytes();
        } catch (Exception e) { }
        return new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                0x08, 0x02, 0x00, 0x00, 0x00, (byte) 0x90, 0x77, 0x53,
                (byte) 0xDE, 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41,
                0x54, 0x08, (byte) 0xD7, 0x63, (byte) 0xF8, (byte) 0xCF, 0x00, 0x00,
                0x03, 0x01, 0x01, 0x00, 0x18, (byte) 0xDD, (byte) 0x8D, (byte) 0xB0,
                0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
        };
    }
}