import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.logviewer.core.MemoryMappedLogFile;
import org.logviewer.core.cache.NoCacheStrategy;
import org.logviewer.core.filter.FilterableLogFileWrapper;
import org.logviewer.core.index.InMemoryIndexStrategy;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static junit.framework.Assert.assertEquals;

public class FilterableLogFileWrapperTest {

    private static final int DATA_LENGTH = 128;

    private static String[] data;

    private static File file;

    @BeforeClass
    public static void init() throws IOException {
        data = new String[DATA_LENGTH];
        for (int i = 0; i < DATA_LENGTH; i++) {
            data[i] = "test" + i;
        }
        file = new File("./.data.test");
        if (file.exists()) file.delete();
        file.createNewFile();
        PrintWriter printWriter = new PrintWriter(file);
        for (String row : data) {
            printWriter.println(row);
        }
        printWriter.flush();
        printWriter.close();
    }

    @AfterClass
    public static void destroy() {
        if (file.exists()) file.delete();
    }

    @Test
    public void filterLengthTest() throws IOException {
        try (MemoryMappedLogFile memoryMappedLogFile = new MemoryMappedLogFile(file,
                new NoCacheStrategy<>(),
                new InMemoryIndexStrategy<>(DATA_LENGTH))) {
            FilterableLogFileWrapper wrapper = new FilterableLogFileWrapper(memoryMappedLogFile, "test[0-9]");
            assertEquals(10, wrapper.length());
        }
    }

    @Test
    public void filterContentTest() throws IOException {
        try (MemoryMappedLogFile memoryMappedLogFile = new MemoryMappedLogFile(file,
                new NoCacheStrategy<>(),
                new InMemoryIndexStrategy<>(DATA_LENGTH))) {
            FilterableLogFileWrapper wrapper = new FilterableLogFileWrapper(memoryMappedLogFile, "test[0-9]");
            for (int i = 0; i < wrapper.length(); i++) {
                assertEquals(data[i], wrapper.get(i).getMessage());
            }
        }
    }
}
