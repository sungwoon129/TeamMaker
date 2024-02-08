class Channel {

    stompClient;
    id;

    constructor(channelId) {
        const socket = new SockJS('/wauction');
        this.stompClient = Stomp.over(socket);
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
        const topic = "/channel/" + this.id;

        this.stompClient.subscribe(topic, msg => {

            this.showMessage(JSON.parse(msg.body));
        });
    }

    showMessage(message) {

        const greetingsElement = document.getElementById("greetings");
        const newRow = document.createElement("tr");
        newRow.innerHTML = "<td>" + message.writer + "</td>" +
            "<td>" + message.msg + "</td>";
        greetingsElement.appendChild(newRow);
    }
}

document.addEventListener("DOMContentLoaded", () => {

    const channel = new Channel(extractChannelIdFromUrl());

    channel.connect();

    document.querySelector('form').addEventListener('submit', function (e) {
        e.preventDefault();
    });

    document.querySelector("#leave").addEventListener("click", () => {
        channel.leave();
        window.location.href = "/";
    })
    document.querySelector("#send").addEventListener("click", () => {
        channel.sendMessage(getData());
    })

});

const getData = () => {
    return {
        sender: document.querySelector("#name").value,
        type: "join",
    }
}


const extractChannelIdFromUrl = () => {

    const currentUrl = window.location.href;
    const regex = /\/channel\/(\d+)/;

    const match = currentUrl.match(regex);

    return match ? match[1] : null;
}
