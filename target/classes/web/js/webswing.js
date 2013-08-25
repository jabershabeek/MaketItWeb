var clientId = GUID();
var serverpath = location.protocol == 'file:' ? "http://localhost" : location.protocol + "//" + location.host;
var socket = io.connect(serverpath);
var latestMouseMoveEvent;
var latestMouseWheelEvent;

var mouseDown = 0;

function processRequest(data) {
	var key;
	if (data.type == "paint") {
		for (key in data.b64images) {
			if (data.b64images.hasOwnProperty(key)) {
				draw(data.windowInfos[key], data.b64images[key]);
			}
		}
		socket.send("paintAck" + clientId);
		
	}
}

function draw(windowInfo, b64image) {
	var canvas, context, imageObj;
	canvas = document.getElementById(windowInfo.id);
	if (canvas == null) {
		canvas = createCanvasWinodow(windowInfo.id, windowInfo.title, windowInfo.width, windowInfo.height, windowInfo.resizable);
	}
	if(canvas.width!=windowInfo.width || canvas.height!=windowInfo.height){
		canvas.width=windowInfo.width;
		canvas.height=windowInfo.height;
	}
	context = canvas.getContext("2d");
	imageObj = new Image();
	imageObj.onload = function() {
		context.drawImage(imageObj, 0, 0);
		var currentDialog = $("#" + windowInfo.id + "Window");
		if (currentDialog.dialog("isOpen") == false) {
			currentDialog.dialog("open");
		}
		if (windowInfo.hasFocus) {
			currentDialog.dialog("moveToTop");
		}
	};
	imageObj.src = 'data:image/png;base64,' + b64image;
}

function createCanvasWinodow(name, title, width, height,resizable) {
	$(".info").remove();
	$("#root").append('<div id="' + name + 'Window"><canvas id="' + name + '" width="' + width + '" height="' + height + '" tabindex="-1"/></div>');
	$("#" + name + "Window").dialog({
		width : "auto",
		heigth : "auto",
		title : title,
		resizable : resizable,
		beforeClose : sendCloseWindowEvent,
		resizeStop : sendResizeWindowEvent
	});
	var canvas = document.getElementById(name);
	registerEventListeners(canvas);
	return canvas;
}

function sendCloseWindowEvent(event, ui) {
	if (event.hasOwnProperty('originalEvent')) {
		var e = {
			'@class' : 'org.webswing.ignored.model.c2s.JsonEventWindow',
			'type' : 'close',
			'windowId' : this.id.substring(0, this.id.length - 6),
			'clientId' : clientId
		};
		socket.json.send(e);
	}
}

function sendResizeWindowEvent(event, ui) {
	if (event.hasOwnProperty('originalEvent')) {
		var e = {
			'@class' : 'org.webswing.ignored.model.c2s.JsonEventWindow',
			'type' : 'resize',
			'windowId' : this.id.substring(0, this.id.length - 6),
			'clientId' : clientId,
			'newWidth' : ui.size.width,
			'newHeight': ui.size.height
		};
		socket.json.send(e);
	}
}

function mouseMoveEventFilter() {
	if (latestMouseMoveEvent != null) {
		socket.json.send(latestMouseMoveEvent);
		latestMouseMoveEvent = null;
	}
	if (latestMouseWheelEvent != null) {
		socket.json.send(latestMouseWheelEvent);
		latestMouseWheelEvent = null;
	}
}

function registerEventListeners(canvas) {
	bindEvent(canvas, 'mousedown', function(evt) {
		var mousePos = getMousePos(canvas, evt, 'mousedown');
		latestMouseMoveEvent = null;
		socket.json.send(mousePos);
		canvas.focus();
		return false;
	}, false);
	bindEvent(canvas, 'dblclick', function(evt) {
		var mousePos = getMousePos(canvas, evt, 'dblclick');
		latestMouseMoveEvent = null;
		socket.json.send(mousePos);
		canvas.focus();
		return false;
	}, false);
	bindEvent(canvas, 'mousemove', function(evt) {
		var mousePos = getMousePos(canvas, evt, 'mousemove');
		mousePos.button = mouseDown;
		latestMouseMoveEvent = mousePos;
		return false;
	}, false);
	bindEvent(canvas, 'mouseup', function(evt) {
		var mousePos = getMousePos(canvas, evt, 'mouseup');
		latestMouseMoveEvent = null;
		socket.json.send(mousePos);
		return false;
	}, false);
	// IE9, Chrome, Safari, Opera
	bindEvent(canvas, "mousewheel", function(evt) {
		var mousePos = getMousePos(canvas, evt, 'mousewheel');
		latestMouseMoveEvent = null;
		if (latestMouseWheelEvent != null) {
			mousePos.wheelDelta += latestMouseWheelEvent.wheelDelta;
		}
		latestMouseWheelEvent = mousePos;
		return false;
	}, false);
	// firefox
	bindEvent(canvas, "DOMMouseScroll", function(evt) {
		var mousePos = getMousePos(canvas, evt, 'mousewheel');
		latestMouseMoveEvent = null;
		if (latestMouseWheelEvent != null) {
			mousePos.wheelDelta += latestMouseWheelEvent.wheelDelta;
		}
		latestMouseWheelEvent = mousePos;
		return false;
	}, false);
	bindEvent(canvas, 'contextmenu', function(event) {
		event.preventDefault();
		event.stopPropagation();
		return false;
	});

	bindEvent(canvas, 'keydown', function(event) {
		// 48-57
		// 65-90
		// 186-192
		// 219-222
		// 226
		var kc = event.keyCode;
		if (!((kc >= 48 && kc <= 57) || (kc >= 65 && kc <= 90) || (kc >= 186 && kc <= 192) || (kc >= 219 && kc <= 222) || (kc == 226))) {
			event.preventDefault();
			event.stopPropagation();
		}
		var keyevt = getKBKey('keydown', canvas, event);
		socket.json.send(keyevt);
		return false;
	}, false);
	bindEvent(canvas, 'keypress', function(event) {
		event.preventDefault();
		event.stopPropagation();
		var keyevt = getKBKey('keypress', canvas, event);
		socket.json.send(keyevt);
		return false;
	}, false);
	bindEvent(canvas, 'keyup', function(event) {
		event.preventDefault();
		event.stopPropagation();
		var keyevt = getKBKey('keyup', canvas, event);
		socket.json.send(keyevt);
		return false;
	}, false);

}

function getMousePos(canvas, evt, type) {
	var rect = canvas.getBoundingClientRect(), root = document.documentElement;
	// return relative mouse position
	var mouseX = evt.clientX - rect.left - root.scrollTop;
	var mouseY = evt.clientY - rect.top - root.scrollLeft;
	var delta = 0;
	if (type == 'mousewheel') {
		delta = -Math.max(-1, Math.min(1, (evt.wheelDelta || -evt.detail)));
	}
	return {
		'@class' : 'org.webswing.ignored.model.c2s.JsonEventMouse',
		'windowId' : canvas.id,
		'clientId' : clientId,
		'x' : mouseX,
		'y' : mouseY,
		'type' : type,
		'wheelDelta' : delta,
		'button' : evt.which,
		'ctrl' : evt.ctrlKey,
		'alt' : evt.altKey,
		'shift' : evt.shiftKey,
		'meta' : evt.metaKey
	};
}

function getKBKey(type, canvas, evt) {
	return {
		'@class' : 'org.webswing.ignored.model.c2s.JsonEventKeyboard',
		'windowId' : canvas.id,
		'clientId' : clientId,
		'type' : type,
		'character' : evt.which,
		'keycode' : evt.keyCode,
		'alt' : evt.altKey,
		'ctrl' : evt.ctrlKey,
		'shift' : evt.shiftKey,
		'meta' : evt.metaKey,
		'altgr' : evt.altGraphKey
	}
}

function GUID() {
	var S4 = function() {
		return Math.floor(Math.random() * 0x10000).toString(16);
	};
	return (S4() + S4() + "-" + S4() + "-" + S4() + "-" + S4() + "-" + S4() + S4() + S4());
}

function bindEvent(el, eventName, eventHandler) {
	if (el.addEventListener) {
		el.addEventListener(eventName, eventHandler, false);
	} else if (el.attachEvent) {
		el.attachEvent('on' + eventName, eventHandler);
	}
}

function start() {
	$('#status').html('initializing');
	$('#status').removeClass().addClass('ui-initializing ui-widget-header ui-corner-all');
	$(".ui-dialog").remove();
	$("#root").append('<div class="info" id="init" title="Initializing"><p>Initializing connection to server..<image src="/css/smoothness/images/loading32.gif"/></p></div>');
	$("#init").dialog({
		modal : true,
		height : 100,
		width : 400,
		draggable : false,
		resizable : false,
	});

	setInterval(mouseMoveEventFilter, 100);
	socket
			.on(
					'message',
					function(data) {
						if (typeof data == "string") {
							if (data == "shutDownNotification") {
								$(".ui-dialog").remove();
								$("#root").append('<div class="info" id="shutDownNotification" title="Application closed"><p><span class="ui-icon ui-icon-info" style="float: left; margin: 0 7px 20px 0;"></span>Application is closed. Would you like to restart the application?</p></div>');
								$("#shutDownNotification").dialog({
									modal : true,
									height : 200,
									width : 450,
									draggable : false,
									resizable : false,
									buttons : {
										"Restart" : function() {
											location.reload();
										}
									}
								});
							} else if (data == "tooManyClientsNotification") {
								$(".ui-dialog").remove();
								$("#root")
										.append(
												'<div class="info" id="tooManyClientsNotification" title="Server busy"><p><span class="ui-icon ui-icon-info" style="float: left; margin: 0 7px 20px 0;"></span>Application can not be started, because there is too many clients using it at the moment. Please try again later.</p></div>');
								$("#tooManyClientsNotification").dialog({
									modal : true,
									height : 200,
									width : 650,
									draggable : false,
									resizable : false,
									buttons : {
										"Try again..." : function() {
											location.reload();
										}
									}
								});
							}
						}
						if (data.clazz == 'org.webswing.ignored.model.s2c.JsonWindowRequest') {
							$("#" + data.windowId + "Window").dialog("close");
							return;
						}
						if (data.clazz == 'org.webswing.ignored.model.s2c.JsonDesktopRequest') {
							window.open(data.url, '_blank');
						}

						processRequest(data);
					});
	socket.on('connect', function() {
		if ($('#root').text() != "online") {
			socket.json.send({
				'@class' : 'org.webswing.ignored.model.c2s.JsonConnectionHandshake',
				'clientId' : clientId
			});
			$('#status').html('online');
			$('#status').removeClass().addClass('ui-online ui-widget-header ui-corner-all');

			$(".ui-dialog").remove();
			$("#root").append('<div class="info" id="loader" title="Application starting..."><p>Application starting. please wait..<image src="/css/smoothness/images/loading32.gif"/></p></div>');
			$("#loader").dialog({
				modal : true,
				height : 100,
				width : 400,
				draggable : false,
				resizable : false,
			});
		}
	});

	socket.on('disconnect', function() {
		$('#status').html('offline');
		$('#status').removeClass().addClass('ui-offline ui-widget-header ui-corner-all');
	});

	$(window).bind("beforeunload", function() {
		socket.send('unload' + clientId);
	});
	bindEvent(document, 'mousedown', function(evt) {
		if (evt.which == 1) {
			mouseDown = 1;
		}
	});
	bindEvent(document, 'mouseout', function(evt) {
		mouseDown = 0;
	});
	bindEvent(document, 'mouseup', function(evt) {
		if (evt.which == 1) {
			mouseDown = 0;
		}
	});
}