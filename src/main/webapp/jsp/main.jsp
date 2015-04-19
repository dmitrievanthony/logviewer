<%@ page import="org.logviewer.web.util.WebKeys" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head>
        <script type="application/javascript" src="https://code.jquery.com/jquery-2.1.3.min.js"></script>
        <script type="application/javascript" src="/js/logviewer.js"></script>
        <link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css" />
        <link rel="stylesheet" type="test/css" href="/css/logviewer.css" />
    </head>
    <body>
        <div class="wrapper">
            <div id="files">
                <c:forEach items="${files}" var="file">
                    <c:choose>
                        <c:when test="${file.isAvailable()}">
                            <div class="file available" onclick="logviewer.openLogFile('${file.name}', ${file.length()}, this)">${file.name}</div>
                        </c:when>
                        <c:otherwise>
                            <div class="file not-available">${file.name}</div>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </div>
            <div id="log">
                <div id="log-content">
                    <div id="log-row-number"></div>
                    <div id="log-row-content"></div>
                    <div id="log-scroll">
                        <div id="slider"></div>
                    </div>
                </div>
                <div id="log-controls">
                    <div id="log-search">
                        <label for="log-search-input"><i class="fa fa-search"></i></label>
                        <input id="log-search-input" type="text" />
                        <i id="log-search-next" class="fa fa-caret-square-o-down"></i>
                        <span id="finished" style="display: none;">[no more results]</span>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>