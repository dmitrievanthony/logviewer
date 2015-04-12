import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.logviewer.core.LogRecord;
import org.logviewer.core.MemoryMappedLogFile;
import org.logviewer.core.cache.NoCacheStrategy;
import org.logviewer.core.index.InMemoryIndexStrategy;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class MemoryMappedLogFileTest {

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
    public void testGetByNumberWithBigIndexArraySize() throws Exception {
        try (MemoryMappedLogFile memoryMappedLogFile = new MemoryMappedLogFile(file,
                new NoCacheStrategy<>(),
                new InMemoryIndexStrategy<>(DATA_LENGTH * 4))) {
            int index = DATA_LENGTH / 3;
            LogRecord logRecord = memoryMappedLogFile.get(index);
            assertEquals(data[index], logRecord.getMessage());
            assertEquals(index, logRecord.getId());
        }
    }

    @Test
    public void testGetByNumberWithDataLengthIndexArraySize() throws Exception {
        try (MemoryMappedLogFile memoryMappedLogFile = new MemoryMappedLogFile(file,
                new NoCacheStrategy<>(),
                new InMemoryIndexStrategy<>(DATA_LENGTH))) {
            int index = DATA_LENGTH / 3;
            LogRecord logRecord = memoryMappedLogFile.get(index);
            assertEquals(data[index], logRecord.getMessage());
            assertEquals(index, logRecord.getId());
        }
    }

    @Test
    public void testGetByNumberWithVerySmallIndexArraySize() throws Exception {
        try (MemoryMappedLogFile memoryMappedLogFile = new MemoryMappedLogFile(file,
                new NoCacheStrategy<>(),
                new InMemoryIndexStrategy<>(DATA_LENGTH / 4))) {
            int index = DATA_LENGTH / 3;
            LogRecord logRecord = memoryMappedLogFile.get(index);
            assertEquals(data[index], logRecord.getMessage());
            assertEquals(index, logRecord.getId());
        }
    }

    @Test
    public void testGetByRangeWithBigIndexArraySize() throws Exception {
        try (MemoryMappedLogFile memoryMappedLogFile = new MemoryMappedLogFile(file,
                new NoCacheStrategy<>(),
                new InMemoryIndexStrategy<>(DATA_LENGTH * 4))) {
            List<LogRecord> records = memoryMappedLogFile.get(DATA_LENGTH / 3, DATA_LENGTH / 3);
            assertEquals(DATA_LENGTH / 3, records.size());
            for (int i = 0; i < records.size(); i++) {
                assertEquals(data[DATA_LENGTH / 3 + i], records.get(i).getMessage());
                assertEquals(DATA_LENGTH / 3 + i, records.get(i).getId());
            }
        }
    }

    @Test
    public void testGetByRangeWithDataLengthIndexArraySize() throws Exception {
        try (MemoryMappedLogFile memoryMappedLogFile = new MemoryMappedLogFile(file,
                new NoCacheStrategy<>(),
                new InMemoryIndexStrategy<>(DATA_LENGTH))) {
            List<LogRecord> records = memoryMappedLogFile.get(DATA_LENGTH / 3, DATA_LENGTH / 3);
            assertEquals(DATA_LENGTH / 3, records.size());
            for (int i = 0; i < records.size(); i++) {
                assertEquals(data[DATA_LENGTH / 3 + i], records.get(i).getMessage());
                assertEquals(DATA_LENGTH / 3 + i, records.get(i).getId());
            }
        }
    }

    @Test
    public void testGetByRangeWithVerySmallIndexArraySize() throws Exception {
        try (MemoryMappedLogFile memoryMappedLogFile = new MemoryMappedLogFile(file,
                new NoCacheStrategy<>(),
                new InMemoryIndexStrategy<>(DATA_LENGTH / 4))) {
            List<LogRecord> records = memoryMappedLogFile.get(DATA_LENGTH / 3, DATA_LENGTH / 3);
            assertEquals(DATA_LENGTH / 3, records.size());
            for (int i = 0; i < records.size(); i++) {
                assertEquals(data[DATA_LENGTH / 3 + i], records.get(i).getMessage());
                assertEquals(DATA_LENGTH / 3 + i, records.get(i).getId());
            }
        }
    }

    @Test
    public void testIterateWithBigIndexArraySize() throws Exception {
        try (MemoryMappedLogFile memoryMappedLogFile = new MemoryMappedLogFile(file,
                new NoCacheStrategy<>(),
                new InMemoryIndexStrategy<>(DATA_LENGTH * 4))) {
            int index = 0;
            for (LogRecord logRecord : memoryMappedLogFile) {
                assertEquals(data[index], logRecord.getMessage());
                assertEquals(index++, logRecord.getId());
            }
        }
    }

    @Test
    public void testIterateWithDataLengthIndexArraySize() throws Exception {
        try (MemoryMappedLogFile memoryMappedLogFile = new MemoryMappedLogFile(file,
                new NoCacheStrategy<>(),
                new InMemoryIndexStrategy<>(DATA_LENGTH))) {
            int index = 0;
            for (LogRecord logRecord : memoryMappedLogFile) {
                assertEquals(data[index], logRecord.getMessage());
                assertEquals(index++, logRecord.getId());
            }
        }
    }

    @Test
    public void testIterateWithVerySmallIndexArraySize() throws Exception {
        try (MemoryMappedLogFile memoryMappedLogFile = new MemoryMappedLogFile(file,
                new NoCacheStrategy<>(),
                new InMemoryIndexStrategy<>(DATA_LENGTH / 4))) {
            int index = 0;
            for (LogRecord logRecord : memoryMappedLogFile) {
                assertEquals(data[index], logRecord.getMessage());
                assertEquals(index++, logRecord.getId());
            }
        }
    }
}
