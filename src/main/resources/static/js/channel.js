class Channel {
    socket;
    stompClient;
    id;

    constructor(channelId) {
        this.socket = new SockJS('/wauction');
        this.stompClient = Stomp.over(this.socket);
        this.id = channelId;
    }

    connect() {
        this.stompClient.connect({}, frame => {
            this.onMessage();
        });
    }

    leave() {
        if (this.stompClient !== null) {
            this.stompClient.disconnect();
        }
        console.log("Disconnected");

    }

    sendMessage(data) {
        this.stompClient.send(`/wauction/channel/${this.id}/greeting`, {}, JSON.stringify(data));
    }

    onMessage() {
        const url = "/channel/" + this.id;
        this.stompClient.subscribe(url, msg => {
            this.showMessage(JSON.parse(msg.body));
        });
    }

    showMessage(message) {
        $("#greetings").append("<tr>" +
            "<td>" + message.writer + "</td>" +
            "<td>" + message.msg + "</td>" +
            "</tr>");
    }
}

document.addEventListener("DOMContentLoaded", () => {

    const channel = new Channel(extractChannelIdFromUrl());

    channel.connect();

    $("form").on('submit', function (e) {
        e.preventDefault();
    });

    document.querySelector("#leave").onclick = () => {
        channel.leave();
    }
    document.querySelector("#send").onclick = () => {
        channel.sendMessage(getData());
    }

});

const getData = () => {
    return {
        sender: document.querySelector("#name").value,
        type: "join",
    }
}


const extractChannelIdFromUrl = () => {

    const currentUrl = window.location.href;
    const regex = /channel\/(.*)/;

    const match = currentUrl.match(regex);

    return match ? match[1] : null;
}
