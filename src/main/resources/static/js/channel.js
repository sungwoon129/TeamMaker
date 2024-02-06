

class Channel {
    socket;
    stompClient;
    constructor(channelId) {
        this.socket = new SockJS('/wauction/' + channelId);
        this.stompClient = Stomp.over(socket);
    }

    connect() {
        this.stompClient.connect({}, function (frame) {
            console.log('Connected: ' + frame);
        });
    }

    leave() {
        if (this.stompClient !== null) {
            this.stompClient.disconnect();
        }
        console.log("Disconnected");

        //TODO : GET /list
    }

    sendMessage() {
        const data = {
            channelId: 1,
            senderId: 1,
            type:"price",
            message: "1000"
        }
        this.stompClient.send("/app/hello", {}, JSON.stringify(data));
    }

    onMessage() {
        this.stompClient.subscribe('/topic/greetings',  msg => {
            this.showMessage(JSON.parse(msg.body));
        });
    }

    showMessage(message) {
        console.log(message);

        $("#greetings").append("<tr>" +
        "<td>" + message.writer + "</td>" +
        "<td>" + message.msg + "</td>" +
        "</tr>");
    }
}

$(function () {

    const channelId = "";
    const channel = new Channel(channelId);

    channel.connect();
    channel.onMessage();

    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#leave" ).click(function() { channel.leave(); });
    $( "#send" ).click(function() { channel.sendMessage() });

});