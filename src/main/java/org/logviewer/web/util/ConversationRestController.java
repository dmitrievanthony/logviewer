package org.logviewer.web.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.IdGenerator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
public class ConversationRestController {

    @RequestMapping("/api/util/conversation/close")
    public void closeConversation(@RequestParam("conversationId") UUID conversationId, HttpSession session) {
        Map<String, Map<String, Object>> conversations = (Map<String, Map<String, Object>>)
                session.getAttribute(WebKeys.CONVERSATIONS_ATTRIBUTE_NAME);
        conversations.remove(conversationId.toString());
    }

    @RequestMapping("/api/util/conversation/open")
    public ResponseEntity<UUID> openConversation(HttpSession session) {
        IdGenerator idGenerator = (IdGenerator) session.getAttribute(WebKeys.CONVERSATION_ID_GENERATOR_ATTRIBUTE_NAME);
        UUID newConversationId = idGenerator.generateId();
        Map<String, Map<String, Object>> conversations = (Map<String, Map<String, Object>>)
                session.getAttribute(WebKeys.CONVERSATIONS_ATTRIBUTE_NAME);
        conversations.put(newConversationId.toString(), new ConcurrentHashMap<>());
        return new ResponseEntity<>(newConversationId, HttpStatus.OK);
    }

    @RequestMapping("/api/util/conversation/list")
    public List<UUID> getConversations(HttpSession session) {
        Map<String, Map<String, Object>> conversations = (Map<String, Map<String, Object>>)
                session.getAttribute(WebKeys.CONVERSATIONS_ATTRIBUTE_NAME);
        return conversations.keySet().stream().map(UUID::fromString).collect(Collectors.toList());
    }
}
