
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

    enter() {
        const data = {
            sender: this.user,
            message: "JOIN",
            type: "JOIN",
        }
        this.stompClient.send(`/wauction/channel/${this.id}/enter`, {}, JSON.stringify(data));
    }


    leave() {
        if (this.stompClient !== null) {
            this.stompClient.disconnect();
        }
        console.log("Disconnected");

    }

    ready() {
        const data = {
            sender: this.user,
            message: "READY",
            type: "READY",
        }
        this.stompClient.send(`/wauction/channel/${this.id}/ready`, {}, JSON.stringify(data));
    }

    isReadyAll(message) {

        return message.capacity === message.readyCount;
    }

    bid(messageType) {
        const data = {
            sender: this.user,
            message: document.querySelector("#point").value,
            type: messageType,
        }
        this.stompClient.send(`/wauction/channel/${this.id}/send`, {}, JSON.stringify(data));
    }

    onMessage() {
        const topic = "/channel/" + this.id;

        this.stompClient.subscribe(topic, msg => {
            this.updateUi(JSON.parse(msg.body));
        });
    }

    updateUi(message) {

        if(message.messageType === "PRICE") {
            this.showPrice(message)
        }
        else if(message.messageType === "READY") {
            if(this.isReadyAll(message)) this.enableStartUi(message);
        }
        else if(message.messageType === "JOIN") {
            this.updateParticipants(message);
        }
    }

    showPrice(message) {
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

    enableStartUi(message) {


    }

    updateParticipants(message) {
        const inActiveTeam = this.teams.find(team => team.isActive === false);

        inActiveTeam.isActive = true;

        this.teams = this.teams.map({
            ...,
            inActiveTeam
        })

        this.teams.forEach(team => {
            if(this.user !== team.name && team.isActive === true) {
                // TODO 입장 효과 UI
            }
        })

    }


    setTeamColor() {

        const shuffleColors = shuffleArray(this.colorArray);

        const teamNames = document.querySelectorAll(".random-color-element");
        teamNames.forEach((title,idx) => {
            title.style.color = shuffleColors[idx];

            this.teams.push({
                color: shuffleColors[idx],
                name:title.textContent,
                isActive: false
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

    const channelData = getChannelData(channel.id);

    channel.connect();

    channel.setUser(channelData.data.auctionRuleResponse.roles[0].name);
    channel.enter();

    document.querySelector('form').addEventListener('submit', function (e) {
        e.preventDefault();
    });

    document.querySelector("#leave").addEventListener("click", () => {
        channel.leave();
        window.location.href = "/";
    })

    document.querySelector("#ready").addEventListener("click", () => {
        const messageType = "READY";
        channel.ready(messageType);
    })

    document.querySelector("#bid").addEventListener("click", () => {
        const messageType = "PRICE";
        channel.bid(messageType);
    })

    channel.setTeamColor()


});

const getChannelData = async (channelId) => {
    const apiUrl = `/api/channel/${channelId}`;
    const response = await fetch(apiUrl, {
        method: "GET",
    })
    return response.json();
}

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
