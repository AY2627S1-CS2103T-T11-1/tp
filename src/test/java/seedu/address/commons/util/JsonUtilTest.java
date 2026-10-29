package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;

import seedu.address.testutil.SerializableTestClass;
import seedu.address.testutil.TestUtil;

/**
 * Tests JSON Read and Write
 */
public class JsonUtilTest {

    private static final Path SERIALIZATION_FILE = TestUtil.getFilePathInSandboxFolder("serialize.json");

    @Test
    public void serializeObjectToJsonFile_noExceptionThrown() throws IOException {
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.serializeObjectToJsonFile(SERIALIZATION_FILE, serializableTestClass);

        assertEquals(FileUtil.readFromFile(SERIALIZATION_FILE), SerializableTestClass.JSON_STRING_REPRESENTATION);
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = JsonUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertEquals(serializableTestClass.getName(), SerializableTestClass.getNameTestValue());
        assertEquals(serializableTestClass.getListOfLocalDateTimes(), SerializableTestClass.getListTestValues());
        assertEquals(serializableTestClass.getMapOfIntegerToString(), SerializableTestClass.getHashMapTestValues());
    }

    @TestFactory
    public Stream<DynamicTest> fromJsonString_trailingContent_throwsJsonParseException() {
        return Stream.of("{}", "[]", "\"extra\"", "42", "true", "null", "garbage", ",", "]", "\u00a0")
                .map(suffix -> DynamicTest.dynamicTest("Trailing content: " + suffix, () -> {
                    String json = SerializableTestClass.JSON_STRING_REPRESENTATION + "\n" + suffix;
                    assertThrows(JsonParseException.class, () ->
                            JsonUtil.fromJsonString(json, SerializableTestClass.class));
                }));
    }

    @TestFactory
    public Stream<DynamicTest> fromJsonString_nonObjectRootWithTrailingContent_throwsJsonParseException() {
        return Stream.of("[1,2]", "\"first\"", "1", "true", "null")
                .map(root -> DynamicTest.dynamicTest("Root: " + root, () ->
                        assertThrows(JsonParseException.class, () ->
                                JsonUtil.fromJsonString(root + "\n{}", Object.class))));
    }

    @Test
    public void fromJsonString_trailingWhitespace_preservesAllFields() throws IOException {
        SerializableTestClass result = JsonUtil.fromJsonString(
                SerializableTestClass.JSON_STRING_REPRESENTATION + " \t\r\n", SerializableTestClass.class);

        assertEquals(SerializableTestClass.getNameTestValue(), result.getName());
        assertEquals(SerializableTestClass.getListTestValues(), result.getListOfLocalDateTimes());
        assertEquals(SerializableTestClass.getHashMapTestValues(), result.getMapOfIntegerToString());
    }

    @Test
    public void saveJsonFile_serializationFailure_preservesDestination(@TempDir Path directory) throws IOException {
        Path existing = Files.writeString(directory.resolve("existing.json"), "{\"persons\":[]}");
        Path missing = directory.resolve("missing.json");
        Object invalid = new Object() {
            @JsonProperty
            public String getValue() {
                throw new IllegalStateException("Cannot serialize");
            }
        };
        assertThrows(IOException.class, () -> JsonUtil.saveJsonFile(invalid, existing));
        assertEquals("{\"persons\":[]}", Files.readString(existing));
        assertThrows(IOException.class, () -> JsonUtil.saveJsonFile(invalid, missing));
        assertFalse(Files.exists(missing));
        try (var files = Files.list(directory)) {
            assertEquals(1, files.count());
        }
    }

    //TODO: @Test jsonUtil_readJsonStringToObjectInstance_correctObject()

    //TODO: @Test jsonUtil_writeThenReadObjectToJson_correctObject()
}
