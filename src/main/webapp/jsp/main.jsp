<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head>
        <script type="application/javascript" src="https://code.jquery.com/jquery-2.1.3.min.js"></script>
        <script type="application/javascript" src="/js/logviewer.js"></script>
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
                <div id="log-row-number"></div>
                <div id="log-row-content"></div>
                <div id="log-scroll">
                    <div id="slider"></div>
                </div>
            </div>
        </div>
    </body>
</html>