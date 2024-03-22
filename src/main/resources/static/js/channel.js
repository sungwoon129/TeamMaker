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

    onMessage() {
        const topic = "/channel/" + this.id;
        const securedTopic = `/user/private`

        const header = {
            id: this.id
        }


        stompClient.subscribe(topic, msg => {
            this.showPublicMsg(JSON.parse(msg.body));
        },header);

        stompClient.subscribe(securedTopic , msg => {
            this.showPrivateMsg(JSON.parse(msg.body));
        },header);
    }

    showPublicMsg(msg)  {
        console.log(msg);
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
        //channel.leave();
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