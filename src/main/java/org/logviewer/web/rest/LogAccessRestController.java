package org.logviewer.web.rest;

import org.logviewer.core.LogDirectory;
import org.logviewer.core.LogFile;
import org.logviewer.core.LogRecord;
import org.logviewer.core.search.SearchableLogFileWrapper;
import org.logviewer.web.util.WebKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @RequestMapping("/search")
    public ResponseEntity<Long> search(@RequestParam("fileName") String fileName,
                                                  @RequestParam("conversationId") UUID conversationId,
                                                  @RequestParam(value = "regex", required = false) String regex,
                                                  HttpSession session) {
        LogFile logFile = logDirectory.getLogFile(fileName);
        Map<String, Map<String, Object>> conversations = (Map<String, Map<String, Object>>)
                session.getAttribute(WebKeys.CONVERSATIONS_ATTRIBUTE_NAME);
        Map<String, Object> conversation = conversations.get(conversationId.toString());
        if (conversation == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if (logFile == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Iterator<LogRecord> search;
        if (regex != null) {
            search = new SearchableLogFileWrapper(logFile, regex).iterator();
            conversation.put("search", search);
        }
        else search = (Iterator<LogRecord>) conversation.get("search");
        if (search.hasNext()) {
            return new ResponseEntity<>(search.next().getId(), HttpStatus.OK);
        }
        else return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
