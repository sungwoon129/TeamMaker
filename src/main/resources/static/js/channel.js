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
            document.querySelector(".emphasis-user").querySelector(".random-color-element").classList.add("active-user")
        });
    }

    leave() {
        if (stompClient !== null) {
            stompClient.disconnect(() => window.location.href = "/");
        }
        console.log("Disconnected");
    }

    exchangeSeat(idx) {
        const data = {
            sender: this.user,
            type: "EXCHANGE",
            message: "자리교환 요청",
            targetUsername: document.querySelectorAll(".active-user").item(idx).textContent
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
                    // TODO: 인덱스와 일치하는 overlay 클래스를 가진 엘리먼트에서 overlay-inactive 제거, 그 자식 요소중 .exchange-display 찾아서 d-none 클래스 제거
                    if(el.textContent === msg.sender && this.user !== msg.sender) {
                        const target = document.querySelectorAll(".overlay").item(idx);
                        if(!target) console.error("메시지 작성자와 일치하는 참가자를 찾을 수 없습니다.");
                        target.classList.remove("overlay-inactive");
                        target.querySelector(".exchange-display").classList.remove("d-none");
                    }
                })
                this.showPublicMsg(msg);
                break;
            case 'LEAVE' :
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
        console.log(msg);
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

            channel.exchangeSeat(idx);
        })
    });

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