package org.logviewer.web.util;

import org.springframework.util.SimpleIdGenerator;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpSessionInitializer implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.getSession();
        session.setAttribute(WebKeys.CONVERSATION_ID_GENERATOR_ATTRIBUTE_NAME, new SimpleIdGenerator());
        session.setAttribute(WebKeys.CONVERSATIONS_ATTRIBUTE_NAME, new ConcurrentHashMap<String, Map<String, Object>>());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) { /* nothing */ }
}
