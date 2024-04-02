const socket = new SockJS('/wauction');
const stompClient = Stomp.over(socket);



class Channel {
    id;
    user;
    exchangeRequestList;

    constructor(channelId, user) {
        this.id = channelId;
        this.user = user
        this.exchangeRequestList = [];
    }

    connect() {
        const header = {
            id: this.id,
            user: this.user
        }

        stompClient.connect(header, frame => {
            this.onMessage();
        });
    }

    leave() {
        if (stompClient !== null) {
            stompClient.disconnect(() => window.location.href = "/");
        }
        console.log("Disconnected");
    }

    exchangeSeat(targetUser) {
        const data = {
            sender: this.user,
            type: "EXCHANGE",
            message: "자리교환 요청",
            targetUsername: targetUser
        }
        stompClient.send(`/wauction/channel/${this.id}/exchangeSeat`, {}, JSON.stringify(data));

    }

    ready() {
        const data = {
            sender: this.user,
            type: "READY",
            message: "준비완료"
        }

        stompClient.send(`/wauction/channel/${this.id}/ready`, {}, JSON.stringify(data));
    }

    onMessage() {
        const topic = {
            public : `/channel/${this.id}`,
            secured : `/channel/${this.id}/${this.user}/secured`
        }

        stompClient.subscribe(topic.public, msg => {
            this.updateUi(JSON.parse(msg.body));
        },{
            id: topic.public
        });

        stompClient.subscribe(topic.secured , msg => {
            this.showPrivateMsg(JSON.parse(msg.body));
        }, {
            id: topic.secured
        });
    }

    updateUi(msg)  {

        switch (msg.messageType) {
            case 'JOIN' :
                console.log("JOIN MESSAGE");

                document.querySelectorAll(".random-color-element").forEach((el,idx) => {

                    if(el.textContent === msg.sender && this.user !== msg.sender) {
                        const target = document.querySelectorAll(".overlay").item(idx);
                        if(!target) console.error("메시지 작성자와 일치하는 참가자를 찾을 수 없습니다.");
                        target.classList.remove("overlay-inactive");
                        target.classList.add("active");
                        target.querySelector(".exchange-display").classList.remove("d-none");
                    }
                })
                this.showPublicMsg(msg);
                break;
            case 'LEAVE' :
                document.querySelectorAll(".random-color-element").forEach((el,idx) => {

                    if(el.textContent === msg.sender && this.user !== msg.sender) {
                        const target = document.querySelectorAll(".overlay").item(idx);
                        if(!target) console.error("메시지 작성자와 일치하는 참가자를 찾을 수 없습니다.");
                        target.classList.add("overlay-inactive");
                        target.classList.remove("active");
                        target.querySelector(".exchange-display").classList.add("d-none");
                    }
                })
                this.showPublicMsg(msg);
                break;

            case 'READY' :
                console.log(msg.writer + "님 준비완료");
                break;
            default:
                console.error("잘못된 메시지 타입입니다.")

        }

    }

    showPublicMsg(msg) {
        const displayElement = document.getElementById("display");
        const newRow = document.createElement("div");
        newRow.className = "message-line";

        const writerElement = document.createElement("div");
        //writerElement.style.color = msg.writer.color;
        writerElement.textContent = msg.writer;
        writerElement.className = "writer"

        const messageElement = document.createElement("div");
        messageElement.textContent = msg.msg;
        messageElement.className = "message";

        newRow.appendChild(writerElement);
        newRow.appendChild(messageElement);


        displayElement.appendChild(newRow);
    }

    showPrivateMsg(msg)  {

        switch (msg.messageType) {
            // TODO : 하나의 교환 요청에 응답하기 전 새로운 요청이 들어온 경우 고려 필요 => 모달창이 여러개 생성되어야 함. 지금은 하나의 창을 재활용하고 있음.
            case "EXCHANGE" :
                this.exchangeRequestList.push({proposer : msg.writer})
 /*               const modal = new bootstrap.Modal(document.getElementById('exchange-modal'), {
                    keyboard: false
                });*/

                const modal = Modal.getOrCreateInstance(document.getElementById('exchange-modal'));

                if(modal.show()) {

                } else {
                    document.getElementById("exchange-modal-message").textContent = msg.msg;


                    modal.show();

                    // 타이머 시작(5초)
                    document.querySelector(".timer").classList.add("round-time-bar");

                    // 타이머 종료
                    setTimeout(() => {
                        modal.hide();
                        if(this.exchangeRequestList.filter(req => req.proposer === msg.writer).length > 0) this.declineExchange()
                    }, 5000);
                    break;
                }

            case "EXCHANGE_RES" :
                if(msg.resultYne === "Y") {
                    this.user = msg.writer;
                    const idx = getParticipantIdx(msg.writer);
                    const origin = document.querySelector(".emphasis-user");
                    const target = document.querySelectorAll(".participant-info").item(idx);

                    swapRole(origin, target);
                } else if(msg.resultYne === "N") {
                    alert(msg.msg);
                }
                break;
        }

    }

    acceptExchange() {
        const data = {
            sender: this.user,
            type: "EXCHANGE_RES",
            message: "Y",
            targetUsername: this.exchangeRequestList[0].proposer
        }

        this.user = data.targetUsername;
        this.exchangeRequestList = this.exchangeRequestList.filter(((req,idx) => idx !== 0));

        stompClient.send(`/wauction/channel/${this.id}/role-exchange/response`, {}, JSON.stringify(data));

        if(this.hasNextExchangeRequest()) {

        }
    }

    declineExchange() {
        const data = {
            sender: this.user,
            type: "EXCHANGE_RES",
            message: "N",
            targetUsername: this.exchangeRequestList[0].proposer
        }

        this.exchangeRequestList = this.exchangeRequestList.filter(((req,idx) => idx !== 0));

        stompClient.send(`/wauction/channel/${this.id}/role-exchange/response`, {}, JSON.stringify(data));

        if(this.hasNextExchangeRequest()) {

        }
    }

    hasNextExchangeRequest() {
        return this.exchangeRequestList.length > 0;
    }
}


document.addEventListener("DOMContentLoaded",  () => {
    const username = getCookie("rname");
    const channel = new Channel(extractChannelIdFromUrl(),username);

    channel.connect();

    document.querySelector('form').addEventListener('submit', function (e) {
        e.preventDefault();
    });

    document.querySelector("#leave").addEventListener("click", () => {
        channel.leave();
    })

    document.querySelector("#ready").addEventListener("click", () => {
        const messageType = "READY";
        //channel.ready(messageType);
    })

    document.querySelector("#bid").addEventListener("click", () => {
        const messageType = "PRICE";
        //channel.bid(messageType);
    })

    document.querySelectorAll(".exchange-seat").forEach((btn, idx) => {
        btn.addEventListener("click", (event) => {
            channel.exchangeSeat(btn.closest(".participant-info").querySelector(".role-name").textContent);
        })
    });

    document.querySelector("#accept-exchange").addEventListener("click", () => {

        const idx = getParticipantIdx(channel.exchangeRequestList[0].proposer);
        const origin = document.querySelector(".emphasis-user");
        const target = document.querySelectorAll(".participant-info").item(idx);
        swapRole(origin, target);
        channel.acceptExchange();
    })

});

const extractChannelIdFromUrl = () => {

    const currentUrl = window.location.href;
    const regex = /\/channel\/(\d+)/;

    const match = currentUrl.match(regex);

    return match ? match[1] : null;
}

const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    return parts.length === 2 ? parts.pop().split(';').shift() : null;
};

const getParticipantIdx = (roleName) => {
    let resultIdx = -1;
    document.querySelectorAll(".role-name").forEach((el,idx) => {
        if(el.textContent === roleName) resultIdx = idx;
    })

    return resultIdx;
}

const swapRole = (origin, target) => {
    origin.classList.remove("emphasis-user");
    target.classList.add("emphasis-user");

    target.querySelector(".exchange-display").classList.add("d-none");
    origin.querySelector(".exchange-display").classList.remove("d-none");
}