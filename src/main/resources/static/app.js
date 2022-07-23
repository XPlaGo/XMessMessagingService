var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    var socket = new SockJS('localhost:8080/socket');
    stompClient = Stomp.over(socket);
    console.log(stompClient)
    for (var k in socket) console.log(k);
    stompClient.connect({"Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4cGxhZ28iLCJ1c2VySWQiOjEsImF1dGgiOiJST0xFX0NMSUVOVCIsImFjY291bnRFeHBpcmVkIjpmYWxzZSwiYWNjb3VudExvY2tlZCI6ZmFsc2UsImNyZWRlbnRpYWxzRXhwaXJlZCI6ZmFsc2UsImRpc2FibGVkIjpmYWxzZSwiaWF0IjoxNjU3MjczOTUzLCJleHAiOjE2NTcyNzQyNTN9.JnX6tesrBrSJMiX4cMAvHLJQ6BfMC5UOPW8L0XpSJrU"}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/user/topic/chats', function (greeting) {
            showGreeting(JSON.parse(greeting.body).content);
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    stompClient.send("/app/hello", {}, JSON.stringify({'message': $("#name").val()}));
}

function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendName(); });
});