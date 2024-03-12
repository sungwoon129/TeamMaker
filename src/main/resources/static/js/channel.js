class Channel {

    stompClient;
    id;
    teams;
    colorArray = ["#ffd700", "#ffa500", "#40e0d0", "#ff7373", "#00ff7f", "#794044", "#ff80ed", "#c39797", "#808080", "#daa520"];
    user;
    channelData;
    isCompleteSubscription;

    constructor(channelId) {
        const socket = new SockJS('/wauction');
        this.stompClient = Stomp.over(socket);
        this.id = channelId;
        this.teams = [];
        this.isCompleteSubscription = false;
    }

    async init() {
        await this.getChannelDataFromServer();
        await this.connect();
    }

    async getChannelDataFromServer() {
        const apiUrl = `/api/channel/${this.id}`;
        const response = await fetch(apiUrl, {
            method: "GET",
        })
        this.channelData = await response.json();

    }


    connect() {
        if(!!this.stompClient) {

            const headers = {
                id: this.id,
            }

            this.stompClient.heartbeat.outgoing = 20000;
            this.stompClient.heartbeat.incoming = 0;
            this.stompClient.reconnect_delay = 3000;
            this.stompClient.connect(headers, frame => {
                this.onMessage();
            });
        }
    }

    leave() {
        const headers = {
            id: this.id,
        }

        if (this.stompClient !== null) {
            this.stompClient.disconnect(() => window.location.href = "/", headers);
        }
        console.log("Disconnected");

    }

    exchangeSeat(idx) {
        const data = {
            sender: this.user,
            type: "EXCHANGE",
            message: "자리교환 요청",
            targetUsername: this.teams[idx].name
        }
        this.stompClient.send(`/wauction/channel/${this.id}/exchangeSeat`, {}, JSON.stringify(data));

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
        const header = {
            id: this.id
        }

        this.stompClient.subscribe(topic, msg => {
            this.updateUi(JSON.parse(msg.body));
            if(!this.isCompleteSubscription) {
                const securedTopic = `/user/private`
                this.stompClient.subscribe(securedTopic , msg => {
                    this.updateUi(JSON.parse(msg.body));
                    this.isCompleteSubscription = true;
                },header);
            }
        },header);

    }



    updateUi(message) {

        if(!!message.targetUsername && (message.targetUsername === this.user || message.sender === this.user )) {
            if(message.messageType === "EXCHANGE") this.showExchangeModal(message);
            else if(message.messageType === "EXCHANGE_RES") this.showExchangeResult(message);
            return;
        }

        if (message.messageType === "PRICE") {
            this.showMessage(message)
        } else if (message.messageType === "READY") {
            if (this.isReadyAll(message)) this.enableStartUi(message);
        } else if (message.messageType === "JOIN") {
            if(this.getUser() === null || this.getUser() === undefined) this.setUser(message.sender);
            this.updateParticipants(message);
            this.showMessage(message)
        } else if (message.messageType === "LEAVE") {
            this.updateParticipants(message);
            this.showMessage(message)
        }
    }

    showMessage(message) {
        const writer = this.teams.find(team => team.name === message.writer) !== undefined ? this.teams.find(team => team.name === message.writer) : {
            id: 0,
            name: message.writer,
            color: "#FEFEFE"
        };

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
        // TODO : 채널의 모든 구성원이 '준비완료'상태가 되면 시작 버튼 활성화
    }

    updateParticipants(message) {
        const overlays = document.querySelectorAll(".overlay");
        const participantBox = document.querySelectorAll(".participant-info");

        Array.isArray(message.activeRoles) && message.activeRoles.forEach(activeRole => {
            const targetTeamIdx = this.teams.findIndex(team => team.name === activeRole);

            if(targetTeamIdx === -1 ) throw Error("활성화된 팀 정보를 팀 목록에서 찾을 수 없습니다.");

            if (message.messageType === "JOIN" ) {
                this.teams[targetTeamIdx].isActive = true;
                overlays[targetTeamIdx].classList.remove("overlay-inactive");

                if(this.teams[targetTeamIdx].name === this.user && message.sender === this.user) {
                    overlays[targetTeamIdx].querySelector(".ready-alarm").classList.remove("d-none")
                    if(!participantBox[targetTeamIdx].classList.contains("emphasis-user")) participantBox[targetTeamIdx].classList.add("emphasis-user");

                } else if(this.teams[targetTeamIdx].name !== this.user) {
                    overlays[targetTeamIdx].querySelector(".exchange-display").classList.remove("d-none")
                }

            } else if (message.messageType === "LEAVE") {

                this.teams[targetTeamIdx].isActive = false;
                overlays[targetTeamIdx].classList.add("overlay-inactive");
                overlays[targetTeamIdx].querySelector(".exchange-display").classList.add("d-none")
                overlays[targetTeamIdx].querySelector(".ready-alarm").classList.remove("d-none")
            }
        })
    }

    showExchangeModal(msg) {

        const res = confirm(msg.msg);

        const data = {
            sender : this.user,
            type : "exchangeRes",
            message : res,
            targetUsername : msg.sender
        }

        this.stompClient.send(`/wauction/channel/${this.id}/acceptChange`, {}, JSON.stringify(data));

    }

    showExchangeResult(msg) {
        if(!!msg.resultYne && msg.resultYne === "Y") {
            this.swapUser(msg)
        } else {
            alert(msg.msg);
        }
    }

    swapUser(msg) {
        if(msg.sender === this. user) {
            this.user = msg.targetUsername;

        } else if(msg.targetUsername === this.user) {
            this.user = msg.sender;
        }

        // TODO : 자기자신 표시하는 테두리 변경

    }


    setTeam() {

        // TODO : 모든 클라이언트에서 동일한 색상이여야 하므로 서버에서 생성후 반환하도록 변경
        const shuffleColors = shuffleArray(this.colorArray);

        const teamNames = document.querySelectorAll(".random-color-element");
        const teams = this.channelData.data.auctionRuleResponse.roles;

        this.teams = teams.map((team, idx) => ({
            ...team,
            color: shuffleColors[idx],
            isActive: false
        }))

        teamNames.forEach((title, idx) => {
            title.style.color = shuffleColors[idx];
        })
    }

    getUser() {
        return this.user;
    }

    setUser(username) {
        this.setTeam();
        this.user = username;
    }



}

document.addEventListener("DOMContentLoaded", async () => {

    const channel = new Channel(extractChannelIdFromUrl());
    await channel.init();


    document.querySelector('form').addEventListener('submit', function (e) {
        e.preventDefault();
    });

    document.querySelector("#leave").addEventListener("click", () => {
        channel.leave();
    })

    document.querySelector("#ready").addEventListener("click", () => {
        const messageType = "READY";
        channel.ready(messageType);
    })

    document.querySelector("#bid").addEventListener("click", () => {
        const messageType = "PRICE";
        channel.bid(messageType);
    })

    document.querySelectorAll(".exchange-seat").forEach((btn, idx) => {
        btn.addEventListener("click", (event) => {

            channel.exchangeSeat(idx);
        })
    })


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
    if (ampersandPosition !== -1) {
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

