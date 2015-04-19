$(function() {

    // Observe keyboard keys events
    $(document).keydown(function(event) {
        if (logviewer.activeFile == undefined) return;
        if (event.keyCode == 38) logviewer.activeFile.up();
        else if (event.keyCode == 40) logviewer.activeFile.down();
        else if (event.keyCode == 33) logviewer.activeFile.pageUp();
        else if (event.keyCode == 34) logviewer.activeFile.pageDown();
    });

    // Process URL, if URL contains correct hash path specified file will be opened
    var pointer = logviewer.parseHashUrl();
    if (pointer != undefined) {
        logviewer.openLogFile(pointer.fileName, pointer.length, undefined, pointer.position);
        var files = $("#files div");
        for (var i = 0; i < files.length; i++) {
            if (files[i].innerHTML == pointer.fileName) {
                $(files[i]).addClass("active");
                break;
            }
        }
    }
    var oldHashUrl;
    window.setInterval(function() {
        if (logviewer.hashUrl != oldHashUrl) parent.location.hash = oldHashUrl = logviewer.hashUrl;
    }, 1000);

    // Slider processing
    var sliderScrollFlag = false;
    $(document).mouseup(function() {
        if (sliderScrollFlag) {
            $(document).unbind("mousemove");
            sliderScrollFlag = false;
            logviewer.setHashUrl(logviewer.activeFile.name, logviewer.activeFile.position, logviewer.activeFile.length);
        }
    });
    $("#slider").mousedown(function() {
        sliderScrollFlag = true;
        $(document).mousemove(function(event) {
            var scroll = $("#log-scroll");
            var y = event.pageY - scroll.position().top - 10;
            var h = scroll.height();
            if (y < 0) y = 0;
            else if (y > h) y = h;
            logviewer.activeFile.randomAccess(y / h);
        });
    });
    $("#log-scroll").mousedown(function(event) {
        var scroll = $("#log-scroll");
        var y = event.pageY - scroll.position().top - 10;
        var h = scroll.height();
        if (y < 0) y = 0;
        if (y > h) y = h;
        logviewer.activeFile.randomAccess(y / h);
    });

    // Search input processing
    $("#log-search-input").keydown(function(event) {
        if (event.keyCode == 13) { // enter
            var regex = $("#log-search-input").val();
            if (logviewer.activeFile != undefined) {
                if (logviewer.activeFile.searchInstance != undefined && logviewer.activeFile.searchInstance.regex == regex) {
                    logviewer.activeFile.search(); // continue search
                }
                else logviewer.activeFile.search(regex); // new search
            }
        }
    });
    $("#log-search-next").click(function(event) {
        if (logviewer.activeFile != undefined && logviewer.activeFile.searchInstance != undefined) {
            logviewer.activeFile.search();
        }
    });
});

// Main logviewer client-side functionality
var logviewer = {};
logviewer.LOG_MESSAGE_HEIGHT = 20;
logviewer.File = function(name, length, position, windowSize) {
    this.name = name;
    this.length = length;
    this.position = position;
    this.windowSize = windowSize;
};
logviewer.File.prototype.reload = function(callback) {
    logviewer.setHashUrl(this.name, this.position, this.length);
    $.get("/records", {
        fileName: this.name,
        first: this.position,
        count: this.windowSize
    }).done(function (response) {
        $("#log-row-number, #log-row-content").empty();
        response.forEach(function (logRecord) {
            var html = logviewer._logRecordToHtml(logRecord);
            $("#log-row-number").append(html[0]);
            $("#log-row-content").append(html[1]);
        });
        logviewer._refreshSlider();
        if (typeof callback == "function") callback();
    }).fail(logviewer._onFailLoad);
};
logviewer.File.prototype.up = function() {
    if (this.position > 0) {
        this.position--;
        logviewer.setHashUrl(this.name, this.position, this.length);
        $.get("/record", {
            fileName: this.name,
            n: this.position
        }).done(function(response) {
            $("#log-row-number pre:last-child, #log-row-content pre:last-child").remove();
            var html = logviewer._logRecordToHtml(response);
            $("#log-row-number").prepend(html[0]);
            $("#log-row-content").prepend(html[1]);
            $("#slider").css("top", sliderPosition);
            logviewer._refreshSlider();
        }).fail(logviewer._onFailLoad);
    }
};
logviewer.File.prototype.down = function() {
    if (this.position + this.windowSize < this.length) {
        this.position++;
        logviewer.setHashUrl(this.name, this.position, this.length);
        $.get("/record", {
            fileName: this.name,
            n: this.position + this.windowSize - 1
        }).done(function(response) {
            $("#log-row-number pre:first-child, #log-row-content pre:first-child").remove();
            var html = logviewer._logRecordToHtml(response);
            $("#log-row-number").append(html[0]);
            $("#log-row-content").append(html[1]);
            logviewer._refreshSlider();
        }).fail(logviewer._onFailLoad);
    }
};
logviewer.File.prototype.pageUp = function() {
    if (this.position == 0) return;
    if (this.position - this.windowSize < 0) this.position = 0
    else this.position -= this.windowSize;
    this.reload();
};
logviewer.File.prototype.pageDown = function() {
    if (this.position + this.windowSize == this.length) return;
    if (this.position + this.windowSize > this.length) this.position = this.length - this.position;
    else this.position += this.windowSize;
    this.reload();
};
logviewer.File.prototype.randomAccess = function(n) {
    this.position = Math.floor(n * this.length);
    this.reload();
};
logviewer.File.prototype.search = function(regex) {
    var file = this;
    if (file.searchInstance == undefined && regex == undefined)
        throw 'Regex should not be null for search initialization';
    var _search = function(regex) {
        var data = { fileName: file.name, conversationId: file.searchInstance.conversationId };
        if (regex != undefined) data.regex = '.*' + regex + '.*';
        var finished = $("#finished");
        $.get('/search', data).done(function(response, statusText) {
            if (statusText != 'nocontent') {
                file.position = response;
                file.reload(function() {
                    console.log("reload!");
                    var row = $('#log-row-content').find('.log-message:first-child');
                    var html = row.html().replace(new RegExp(file.searchInstance.regex, 'g'), '<span class="match">$&</span>');
                    row.html(html);
                    finished.hide();
                });
            }
            else {
                file.searchInstance = undefined;
                finished.show();
                console.log('nocontent');
            }
        }).fail(function() {
            file.searchInstance = undefined;
            logviewer.closeConversation(file.searchInstance.conversationId);
        });
    };
    if (regex != undefined) {
        logviewer.openConversation(function(conversationId) {
            file.searchInstance = {
                conversationId: conversationId,
                regex: regex
            };
            _search(regex);
        });
    }
    else _search();
};
logviewer.setHashUrl = function(name, position, length) {
    logviewer.hashUrl = "#" + name + ":" + position + "/" + length;
};
logviewer.parseHashUrl = function() {
    var arr = parent.location.hash.substr(1).split(":");
    if (arr.length != 2 || arr[1].split("/").length != 2) return undefined;
    return {
        fileName : arr[0],
        position: arr[1].split("/")[0],
        length: arr[1].split("/")[1]
    }
};
logviewer.openLogFile = function(name, length, button, position) {
    if (this._openFiles[name] == undefined) {
        this._openFiles[name] = new logviewer.File(name,
            length,
            position == undefined ? 0 : position,
            Math.floor($("#log-row-content").height() / logviewer.LOG_MESSAGE_HEIGHT));
        if (button != undefined) $(button).addClass("open");
    }
    this.activeFile = this._openFiles[name];
    this.activeFile.reload();
    if (button != undefined) {
        var buttons = $("#files div");
        for (var i = 0; i < buttons.length; i++) $(buttons[i]).removeClass("active");
        $(button).addClass("active");
    }
    return this._openFiles[name];
};
logviewer._openFiles = {};
logviewer._logRecordToHtml = function(logRecord) {
    var rowNumberDiv = $('<pre class="log-number">' + logRecord.id + '</pre>');
    var rowContentDiv = $('<pre class="log-message">' + logRecord.message + '</pre>');
    return [rowNumberDiv, rowContentDiv];
};
logviewer._refreshSlider = function() {
    var part = logviewer.activeFile.position / logviewer.activeFile.length;
    $("#slider").css("top", "calc(" + (100 * part) + "% + " + (10 - 20 * part) + "px)")
};
logviewer._onFailLoad = function() {
    $("#log-row-number, #log-row-content").empty();
    $("#log-row-content").html("Loading error!");
};
logviewer.openConversation = function(callback) {
    $.get('/api/util/conversation/open').done(function(response) {
        console.log("conversationId = " + response);
        if (typeof callback == 'function') callback(response);
    }).fail(function() {
        throw 'Conversation can not be opened';
    });
};
logviewer.closeConversation = function(callback) {
    $.get('/api/util/conversation/close', {
        conversationId: logviewer.conversationId
    }).done(function(response) {
        if (typeof callback == 'function') {
            callback(response);
        }
    }).fail(function() {
        throw 'Conversation can not be closed';
    });
};
logviewer.getAllConversations = function(callback) {
    $.get('/api/util/conversation/list').done(function(response) {
        if (typeof callback == 'function') {
            callback(response);
        }
    }).fail(function() {
        throw 'List of conversations can not be gotten';
    });
};