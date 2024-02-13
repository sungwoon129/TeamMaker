

class Channel {

    stompClient;
    id;
    teams;
    colorArray = ["#ffd700","#ffa500","#40e0d0","#ff7373","#00ff7f","#794044","#ff80ed","#c39797","#808080","#daa520"];
    user;

    constructor(channelId) {
        const socket = new SockJS('/wauction');
        this.stompClient = Stomp.over(socket);
        this.id = channelId;
        this.teams = [];
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

    sendMessage() {
        const data = {
            sender: this.user,
            message: document.querySelector("#point").value,
            type: "price",
        }
        this.stompClient.send(`/wauction/channel/${this.id}/bid`, {}, JSON.stringify(data));
    }

    onMessage() {
        const topic = "/channel/" + this.id;

        this.stompClient.subscribe(topic, msg => {

            this.showMessage(JSON.parse(msg.body));
        });
    }

    showMessage(message) {

        const writer = this.teams.find(team => team.name === message.writer);

        const displayElement = document.getElementById("display");
        const newRow = document.createElement("div");
        newRow.className = "message-line";

        const writerElement = document.createElement("div");
        writerElement.style.color = writer.color;
        writerElement.textContent = message.writer;
        writerElement.className = "writer"

        const messageElement = document.createElement("div");
        messageElement.textContent = message.msg;
        messageElement.className = "message";

        newRow.appendChild(writerElement);
        newRow.appendChild(messageElement)
        
        displayElement.appendChild(newRow);
    }

    setTeamColor() {

        const shuffleColors = shuffleArray(this.colorArray);

        const teamNames = document.querySelectorAll(".random-color-element");
        teamNames.forEach((title,idx) => {
            title.style.color = shuffleColors[idx];

            this.teams.push({
                color: shuffleColors[idx],
                name:title.textContent
            });
        })
    }

    getUser() {
        return this.user;
    }

    setUser(user) {
        this.user = user;
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
    document.querySelector("#bid").addEventListener("click", () => {
        channel.sendMessage();
    })

    channel.setTeamColor()
    channel.setUser(channel.teams[2].name)

});

const extractChannelIdFromUrl = () => {

    const currentUrl = window.location.href;
    const regex = /\/channel\/(\d+)/;

    const match = currentUrl.match(regex);

    return match ? match[1] : null;
}

const getYoutubeEmbedLink = (url) => {
    var videoId = url.split('v=')[1];
    var ampersandPosition = videoId.indexOf('&');
    if(ampersandPosition !== -1) {
        videoId = videoId.substring(0, ampersandPosition);
    }
    return 'https://www.youtube.com/embed/' + videoId;
}


const shuffleArray = (array) => {
    for (let i = array.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1)); // 0에서 i 사이의 임의의 인덱스 선택
        [array[i], array[j]] = [array[j], array[i]]; // 두 요소의 위치를 바꿔줌
    }
    return array;
};

const getColorByWriter = (writer) => {
    return undefined;
}