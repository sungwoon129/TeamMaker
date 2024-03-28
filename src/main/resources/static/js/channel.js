const socket = new SockJS('/wauction');
const stompClient = Stomp.over(socket);

class Channel {
    id;
    user;

    constructor(channelId, user) {
        this.id = channelId;
        this.user = user
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
        if(msg.messageType === "EXCHANGE") {
            const modal = new bootstrap.Modal(document.getElementById('exchange-modal'), {
                keyboard: false
            })
            document.getElementById("exchange-modal-message").textContent = msg.msg;
            modal.show();
        }

        console.log(msg);
    }

    acceptExchange() {

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