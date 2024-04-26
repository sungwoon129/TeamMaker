const socket = new SockJS('/wauction');
const stompClient = Stomp.over(socket);

class BarTimer {
    initialTime;
    timeLeft;
    interval;
    progressBarTextElement = document.getElementById('progress-bar-text');
    progressBarElement = document.getElementById('progress-bar');

    constructor() {
        this.initialTime = 5;
        this.timeLeft = this.initialTime;
    }

    render() {
        let progressPercentage = (this.timeLeft / this.initialTime) * 100;

        this.progressBarElement.style.width = progressPercentage + '%';
        //this.progressBarTextElement.innerHTML = this.timeLeft + 's';
    }

    tick = () => {
        this.timeLeft = this.timeLeft - 0.1;
        if(this.timeLeft <= 0) {
            clearInterval(this.interval);
        }

        this.render();
    }

    startProgressBar() {

        this.progressBarElement.style.transition = 'width 1s linear';

        this.interval = setInterval(this.tick, 100);
        this.render();
    }

    init() {

        clearInterval(this.interval);

        this.timeLeft = this.initialTime;
        this.progressBarElement.style.transition = 'none';
        this.progressBarElement.style.width = '100%';
    }
}

class Channel {
    id;
    role;
    user;
    exchangeRequestList;
    isWaiting;
    timeoutFunc;
    auctionRule;
    #waitingTimeForAfterBid;
    #waitingTimeForNext;
    #currentItem; // TODO : 현재 아이템 경매가와 관련된 기능 구현 - 현재 입찰가보다 낮은 가격 입찰불가
    timer = new BarTimer();

    constructor(channelId, role, uid) {
        this.id = channelId;
        this.user = Object.freeze(uid);
        this.role = role;
        this.exchangeRequestList = [];
        this.isWaiting = false;
        this.auctionRule = null;
    }

    connect() {
        const header = {
            id: this.id,
            user: this.user,
            role: this.role
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

        if(this.isWaiting === true) return;

        const data = {
            sender: this.role,
            type: "EXCHANGE",
            message: "자리교환 요청",
            targetUsername: targetUser
        }
        stompClient.send(`/wauction/channel/${this.id}/exchangeSeat`, {}, JSON.stringify(data));
        this.isWaiting = true;
        document.querySelectorAll(".exchange-seat").forEach(btn => btn.disabled = true);

    }

    ready() {
        const data = {
            sender: this.role,
            type: "READY",
            message: "준비완료"
        }

        stompClient.send(`/wauction/channel/${this.id}/ready`, {}, JSON.stringify(data));

        document.getElementById("ready").classList.add('d-none');
        document.getElementById("unready").classList.remove('d-none');
    }

    unready() {
        const data = {
            sender: this.role,
            type: "UNREADY",
            message: "준비완료 취소"
        }

        stompClient.send(`/wauction/channel/${this.id}/unready`, {}, JSON.stringify(data));

        document.getElementById("ready").classList.remove('d-none');
        document.getElementById("unready").classList.add('d-none');
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
            const data = JSON.parse(msg.body)
            this.showPrivateMsg(data);
        }, {
            id: topic.secured
        });
    }

    updateUi(msg)  {

        switch (msg.messageType) {
            case 'JOIN' :

                document.querySelectorAll(".random-color-element").forEach((el,idx) => {

                    if(el.textContent === msg.sender && this.role !== msg.sender) {
                        const target = document.querySelectorAll(".overlay").item(idx);
                        if(!target) console.error("메시지 작성자와 일치하는 참가자를 찾을 수 없습니다.");
                        target.classList.remove("overlay-inactive");
                        target.classList.add("active");
                        target.querySelector(".exchange-display").classList.remove("d-none");
                    }

                    if(this.role === msg.manager) {
                        document.getElementById("start").classList.remove("d-none");

                    }
                })
                this.showPublicMsg(msg);
                break;
            case 'LEAVE' :
                document.querySelectorAll(".random-color-element").forEach((el,idx) => {

                    if(el.textContent === msg.sender && this.role !== msg.sender) {
                        const target = document.querySelectorAll(".overlay").item(idx);
                        if(!target) console.error("메시지 작성자와 일치하는 참가자를 찾을 수 없습니다.");
                        target.classList.add("overlay-inactive");
                        target.classList.remove("active");
                        target.querySelector(".exchange-display").classList.add("d-none");
                    }

                    if(this.role === msg.manager) {
                        document.getElementById("start").classList.remove("d-none");

                    }
                })
                this.showPublicMsg(msg);
                break;

            case 'READY' :

                const readyTargetIdx= getParticipantIdx(msg.writer);
                document.querySelectorAll(".participant-info").item(readyTargetIdx).classList.add('ready');
                document.querySelectorAll(".exchange-display").item(readyTargetIdx).classList.add('d-none');

                break;

            case 'UNREADY' :
                const unreadyTargetIdx= getParticipantIdx(msg.writer);
                document.querySelectorAll(".participant-info").item(unreadyTargetIdx).classList.remove('ready');
                document.querySelectorAll(".exchange-display").item(unreadyTargetIdx).classList.remove('d-none');

                break;

            case 'START' :
                document.querySelector('.ready-btn-box').classList.add('d-none');
                this.auctionRule = msg.data;
                let count = 5;
                const countDown = () => {

                    this.showPublicMsg({writer: "SYSTEM", msg: `시작 ${count}초 전...`})

                    count --;

                    if(count > 0) {
                        setTimeout(countDown, 1000)
                    }

                    if(count === 0) {
                        this.showPublicMsg(msg);
                        document.querySelectorAll(".participant-info").forEach(el => el.classList.remove('ready'));


                        setTimeout(() => {
                            shuffle( msg.data.items, document.querySelectorAll('.item-shuffle-box'));
                        },1000);


                        document.getElementById("shuffle-wrapper").classList.remove('d-none');

                        setTimeout(() => {
                            document.getElementById("shuffle-wrapper").classList.add('d-none');
                            relocationItem(msg.data.items);
                            this.#waitingTimeForAfterBid = msg.data.waitingTimeForAfterBid;
                            this.#waitingTimeForNext = msg.data.waitingTimeForNext;
                            this.#currentItem = msg.data.auctionPlayItem;
                            this.toStageNextItem(msg.data.order)
                        }, 5000)


                    }
                }

                countDown();
                break;

            case 'BID' :
                // TODO : 대기시간 초기화, 대기시간 모두 소모시까지 입찰 없을 시 유찰처리, 입찰정보에 따른 ui 업데이트
                this.showPublicMsg(msg)
                break;
            case 'NEXT' :
                // TODO : 경매 대상정보 메시지 출력, 다음 경매대상 경매 시작까지 대기시간 처리
                this.#currentItem = msg.data;
                this.toStageNextItem(msg.data.order);
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
        displayElement.scrollTop = displayElement.scrollHeight;
    }

    showPrivateMsg(msg, isRecursive)  {

        switch (msg.messageType) {
            case "EXCHANGE" :

                if(isRecursive === undefined || !isRecursive) this.exchangeRequestList.push(msg);

                if(document.getElementById('exchange-modal').classList.contains('show')) {
                    console.log("exchange modal is shown");
                    break;
                }

                document.getElementById("exchange-modal-message").textContent = msg.msg;


                this.timer.init();
                this.timer.startProgressBar();

                openModal();

                this.timeoutFunc = setTimeout(() => {
                    if(this.exchangeRequestList.filter(req => req.writer === msg.writer).length > 0) {
                        console.log("응답안했음. 시간초과");
                        console.table(this.exchangeRequestList);
                        closeModal();
                        this.declineExchange(this.exchangeRequestList.shift());
                    }
                }, 5000);
                break;


            case "EXCHANGE_RES" :
                if(msg.resultYne === "Y") {
                    this.role = msg.writer;
                    const idx = getParticipantIdx(msg.writer);
                    const origin = document.querySelector(".emphasis-user");
                    const target = document.querySelectorAll(".participant-info").item(idx);

                    swapRole(origin, target);
                } else if(msg.resultYne === "N") {
                    alert(msg.msg);
                }

                this.isWaiting = false;
                document.querySelectorAll(".exchange-seat").forEach(btn => btn.disabled = false);

                break;
        }

    }

    acceptExchange(item) {

        if(this.timeoutFunc) clearTimeout(this.timeoutFunc);

        const data = {
            sender: this.role,
            type: "EXCHANGE_RES",
            message: "Y",
            targetUsername: item.writer
        }

        this.role = data.targetUsername;

        stompClient.send(`/wauction/channel/${this.id}/role-exchange/response`, {}, JSON.stringify(data));

        if(this.hasNextExchangeRequest()) {
            const copy = [...this.exchangeRequestList];
            this.exchangeRequestList = [];
            for(const req of copy) {
                this.declineExchange(req);
            }
        }
    }

    declineExchange(item) {

        if(this.timeoutFunc) clearTimeout(this.timeoutFunc);

        const data = {
            sender: this.role,
            type: "EXCHANGE_RES",
            message: "N",
            targetUsername: item.writer
        }

        stompClient.send(`/wauction/channel/${this.id}/role-exchange/response`, {}, JSON.stringify(data));

        if(this.hasNextExchangeRequest()) {
            this.timer.init();
            setTimeout(() => this.showPrivateMsg(this.exchangeRequestList[0], true), 500);
        }
    }

    start() {

        stompClient.send(`/wauction/channel/${this.id}/start`);
    }

    hasNextExchangeRequest() {
        return this.exchangeRequestList.length > 0;
    }

    toStageNextItem(order) {


        if(Array.isArray(this.auctionRule.items)) {

            const item = this.auctionRule.items[parseInt(order)];
            document.getElementById("profile-img").src=`${item.img}`;

            const tag = document.createElement('script');

            tag.src = `https://youtu.be/t1_FVzv4l-4?si=XsPcnLvDt34yg-KK`;
            const firstScriptTag = document.getElementsByTagName('script')[0];
            firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);



            // 3. This function creates an <iframe> (and YouTube player)
            //    after the API code downloads.
            let player;
            function onYouTubeIframeAPIReady() {
                player = new YT.Player('player', {
                    rel: '0',
                    autoplay: '1',
                    height: '360',
                    width: '640',
                    videoId: 'M7lc1UVf-VE',
                    events: {
                        'onReady': onPlayerReady,
                        'onStateChange': onPlayerStateChange
                    }
                });
            }
            //document.getElementById("highlight").insertAdjacentHTML('beforeend', item.highlights[0].url);

        }


        // 4. The API will call this function when the video player is ready.
        function onPlayerReady(event) {
            event.target.playVideo();
        }

        // 5. The API calls this function when the player's state changes.
        //    The function indicates that when playing a video (state=1),
        //    the player should play for six seconds and then stop.
        function onPlayerStateChange(event) {
            if (event.data == YT.PlayerState.ENDED || event.data == YT.PlayerState.PAUSED) {
                setTimeout(stopVideo, 1000);
            }
        }
        function stopVideo() {
            player.stopVideo();
            createProgressbar("auction-timer",this.#waitingTimeForNext * 1000, this.auctionTimerEnd);
            this.showPublicMsg({writer: this.role, msg: `${this.#currentItem.name} 경매시작 대기중`});
        }

    }

    bid(value) {

        const order = 0;

        const data = {
            sender: this.role,
            type: "BID",
            message: value,
            itemId: this.auctionRule.items[order].id
        }

        stompClient.send(`/wauction/channel/${this.id}/start`, data);
        // TODO : createProgressbar 애니메이션 초기화
    }

    auctionTimerEnd() {

        // TODO : 채널 방장만 서버에 1회 전송

        const data = {
            itemId: this.#currentItem.id
        }

        stompClient.send(`/wauction/channel/${this.id}/item/determine-destination`, data);

    }
}


document.addEventListener("DOMContentLoaded",  () => {
    const username = getCookie("rname");
    const uid = getCookie("uid");
    const channel = new Channel(extractChannelIdFromUrl(),username,uid);

    channel.connect();

    document.querySelector('form').addEventListener('submit', function (e) {
        e.preventDefault();
    });

    document.querySelector("#leave").addEventListener("click", () => {
        channel.leave();
    })

    document.querySelector("#ready").addEventListener("click", () => {
        const messageType = "READY";
        channel.ready(messageType);
    });

    document.querySelector("#unready").addEventListener("click", () => {
        const messageType = "UNREADY";
        channel.unready(messageType);
    });

    document.querySelector("#start").addEventListener("click", () => {
        channel.start();
    });

    document.querySelector("#bid").addEventListener("click", () => {
        const messageType = "PRICE";
        //channel.bid(messageType);
    })

    document.querySelectorAll(".exchange-seat").forEach((btn, idx) => {
        btn.addEventListener("click", (event) => {
            channel.exchangeSeat(event.target.closest(".participant-info").querySelector(".role-name").textContent);
        })
    });

    document.querySelector("#accept-exchange").addEventListener("click", () => {

        closeModal();

        const data = channel.exchangeRequestList.shift();

        channel.acceptExchange(data);

        const idx = getParticipantIdx(data.writer);
        const origin = document.querySelector(".emphasis-user");
        const target = document.querySelectorAll(".participant-info").item(idx);
        swapRole(origin, target);
    });

    document.querySelector("#decline-exchange").addEventListener("click", () => {

        closeModal();

        const data = channel.exchangeRequestList.shift();
        channel.declineExchange(data);

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

const closeModal = () => {


    document.getElementById('exchange-modal').style.display = 'none';
    document.getElementById('exchange-modal').classList.remove('show');

}

const openModal = () => {

    document.getElementById('exchange-modal').style.display = 'block';
    document.getElementById('exchange-modal').classList.add('show');


}

const shuffle = (shuffled, boxes) => {
        boxes.forEach(box => {
            const preOrder = box.dataset.id;
            const newOrder = shuffled.findIndex(item => item.id === parseInt(preOrder));
            const oriPosition = getElementPosition(box);

            const targetPosition = getElementPosition(boxes[newOrder]);

            const newX = targetPosition.x - oriPosition.x;
            const newY = targetPosition.y - oriPosition.y;
            box.style.transform = `translate(${newX}px, ${newY}px)`;

        });
}

const relocationItem = (shuffled) => {

    const boxes = document.querySelectorAll(".item-box");

    boxes.forEach(box => {

        const preOrder = box.dataset.id;
        const target = shuffled[preOrder - 1];

        box.dataset.id = target.id;
        box.querySelector(".item-img img").src = target.img;
        box.querySelector(".item-box-name").textContent = target.name;
    })
}

const getElementPosition = (element) => {
    const rect = element.getBoundingClientRect();
    const scrollLeft =  document.documentElement.scrollLeft;
    const scrollTop = document.documentElement.scrollTop;
    return { x: rect.left + scrollLeft, y: rect.top + scrollTop };
}

const createProgressbar = (id, duration, callback) => {
    // We select the div that we want to turn into a progressbar
    const progressbar = document.getElementById(id);
    progressbar.className = 'progressbar';

    // We create the div that changes width to show progress
    const progressbarinner = document.createElement('div');
    progressbarinner.className = 'inner';

    // Now we set the animation parameters
    progressbarinner.style.animationDuration = duration;

    // Eventually couple a callback
    if (typeof(callback) === 'function') {
        progressbarinner.addEventListener('animationend', callback);
    }

    // Append the progressbar to the main progressbardiv
    progressbar.appendChild(progressbarinner);

    // When everything is set up we start the animation
    progressbarinner.style.animationPlayState = 'running';
}







