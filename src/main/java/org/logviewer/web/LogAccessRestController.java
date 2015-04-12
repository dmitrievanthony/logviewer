package org.logviewer.web;

import org.logviewer.core.LogDirectory;
import org.logviewer.core.LogFile;
import org.logviewer.core.LogRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LogAccessRestController {

    @Autowired
    private LogDirectory logDirectory;

    @RequestMapping("/record")
    public ResponseEntity<LogRecord> getLogRecord(@RequestParam("fileName") String fileName, @RequestParam("n") long n) {
        LogFile logFile = logDirectory.getLogFile(fileName);
        if (logFile != null) {
            try {
                LogRecord logRecord = logFile.get(n);
                return new ResponseEntity<>(logRecord, HttpStatus.OK);
            }
            catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping("/records")
    public ResponseEntity<List<LogRecord>> getLogRecords(@RequestParam("fileName") String fileName, @RequestParam("first") long first, @RequestParam("count") int count) {
        LogFile logFile = logDirectory.getLogFile(fileName);
        if (logFile != null) {
            try {
                List<LogRecord> records = logFile.get(first, count);
                return new ResponseEntity<>(records, HttpStatus.OK);
            }
            catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
