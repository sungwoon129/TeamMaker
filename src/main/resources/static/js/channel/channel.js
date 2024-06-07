const socket = new SockJS('/wauction');
const stompClient = Stomp.over(socket);

// 자리교환 타이머
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
    #currentItem;
    timer = new BarTimer();
    player;

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

        // 공용 채널 구독
        stompClient.subscribe(topic.public, msg => {
            this.updateUi(JSON.parse(msg.body));
        },{
            id: topic.public
        });

        // 개인 채널 구독
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
                        /*target.querySelector(".exchange-display").classList.remove("d-none");*/
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

            case 'EXCHANGE_RES' :

                if(this.role === msg.originRole) {
                    this.isWaiting = false;
                    document.querySelectorAll(".exchange-seat").forEach(btn => btn.disabled = false);
                }

                if(msg.resultYne === "Y") {
                    this.role = msg.destinationRole;
                    const originIdx = getParticipantIdx(msg.originRole);
                    const targetIdx = getParticipantIdx(msg.destinationRole);
                    const origin = document.querySelectorAll(".participant-info").item(originIdx);
                    const target = document.querySelectorAll(".participant-info").item(targetIdx);

                    swapRole(origin, target);

                } else if(msg.resultYne === "N") {
                    alert(msg.msg);
                }
                break;

            case 'READY' :

                const readyTargetIdx= getParticipantIdx(msg.writer);
                document.querySelectorAll(".participant-info").item(readyTargetIdx).classList.add('ready');

                if(this.role !== msg.writer) document.querySelectorAll(".exchange-display").item(readyTargetIdx).classList.add('d-none');

                break;

            case 'UNREADY' :
                const unreadyTargetIdx= getParticipantIdx(msg.writer);
                document.querySelectorAll(".participant-info").item(unreadyTargetIdx).classList.remove('ready');
                if(this.role !== msg.writer) document.querySelectorAll(".exchange-display").item(unreadyTargetIdx).classList.remove('d-none');

                break;

            case 'START' :
                document.querySelector('.ready-btn-box').classList.add('d-none');
                this.auctionRule = msg.data;
                let count = 5;
                const countDown = () => {

                    this.showPublicMsg({writer: "SYSTEM", msg: `시작 ${count}초 전...`})



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
                            this.#currentItem.isCompleteHighlightPlay = false;
                            this.toStageNextItem(msg.data.order)
                        }, 5000)


                    }

                    count --;
                }

                countDown();
                break;

            case 'BID' :
                this.showPublicMsg(msg)
                this.updateBidInfo(msg);
                resetTimer("inner1");
                break;
            case 'NEXT' :
                this.#currentItem = msg.data;
                this.#currentItem.isCompleteHighlightPlay = false;
                this.toStageNextItem(msg.data.order);
                break;
            case 'COMPLETE_HIGHLIGHT_PLAY' :
                this.initWaitingTimer();
                break;
            case 'COMPLETE_COUNT' :
                if(msg.data === "COMPLETE_BEFORE_BID") {
                    this.showPublicMsg(msg.msg)
                    this.startBid()

                } else if(msg.data === "END_BID_TIMER") {

                }

                break;
            case 'SOLD' :
                this.showPublicMsg(msg)
                this.assignSoldItem(msg);
                break;
            case 'FAIL_IN_BID' :
                this.showPublicMsg(msg)
                this.addToPutOffList(msg);
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
                if(msg.resultYne === "N") {
                    alert(msg.msg);
                }
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
            document.querySelector(".item-contents-box .item-name").textContent = item.name;
            document.querySelector(".item-info-box .item-info-1").textContent = item.position.name;
            document.querySelector(".item-info-box .item-info-2").textContent = "";
            this.updateBidInfo({writer:"", data:{price:0}});

            const waitingListEl = document.querySelector(".items-container").querySelectorAll("img");

            for(let i= 0 ;  i < waitingListEl.length ; i++) {
                waitingListEl.item(i).classList.add("darker");
            }

            waitingListEl.item(parseInt(order)).classList.remove("darker");

            if(!!this.player) {
                this.player.loadVideoById(item.highlights[0].url);
            } else {
                this.player = new YT.Player('player', {
                    rel: '0',
                    autoplay: '1',
                    height: '360',
                    width: '640',
                    videoId: `${item.highlights[0].url}`,
                    playerVars: {
                        autoplay: 1,
                        controls: 1,
                        showinfo: 0,
                        autohide: 1
                    },
                    events: {
                        'onReady': this.onPlayerReady,
                        'onStateChange': this.onPlayerStateChange
                    }
                });
            }

        }

    }

    bid(value) {

        const data = {
            sender: this.role,
            type: "BID",
            message: parseInt(this.#currentItem.price) + parseInt(value),
            itemId: this.#currentItem.itemId
        }

        stompClient.send(`/wauction/channel/${this.id}/bid`, {}, JSON.stringify(data));
    }

    // 입찰 전 대기시간 타이머 종료
    beforeBiddingTimerEnd = (event)  => {

        const data = {
            type: "COMPLETE_BEFORE_BID"
        };

        stompClient.send(`/wauction/channel/${this.id}/item/timer-end`,{}, JSON.stringify(data));
    }

    onPlayerReady(event) {
        event.target.playVideo();
    }


    // 하이라이트 영상 일시정지 or 재생완료시 서버에 완료 메시지 전송
    onPlayerStateChange = (event) => {
        if ((event.data === YT.PlayerState.ENDED || event.data === YT.PlayerState.PAUSED) && this.#currentItem.isCompleteHighlightPlay !== true) {
            stompClient.send(`/wauction/channel/${this.id}/item/complete-highlight-play`);
            this.#currentItem.isCompleteHighlightPlay = true;
        }
    }


    // 참가자들의 입찰 전 경매대상의 정보를 확인하면서 준비하는 시간 타이머 동작
    initWaitingTimer() {
        this.player.stopVideo();

        const timerId = "auction-timer";

        if(existingTimer()) {
            resetTimer(timerId)
        } else {
            createTimer(timerId,this.#waitingTimeForNext, this.beforeBiddingTimerEnd, "입찰시작 까지");
        }


        this.showPublicMsg({writer: "SYSTEM", msg: `${this.#currentItem.name} 경매시작 대기중...`});
    }

    // 입찰 시작.
    startBid() {
        const bidStatusBtn = document.getElementById("bid-status");
        bidStatusBtn.class =  "badge bg-success";
        bidStatusBtn.textContent = "입찰중";
        const timerId = "auction-timer";

        this.showPublicMsg({writer: "SYSTEM", msg: `${this.#currentItem.name} 경매시작`});
        if(existingTimer()) {
            resetTimer(timerId)
        } else {
            createTimer(timerId,this.#waitingTimeForAfterBid, this.endBid, "입찰종료 까지");
        }

        document.querySelectorAll(".price-control-panel button").forEach(btn => btn.disabled = false);
    }

    // 입찰 종료. 입찰 타이머가 끝나면 서버에 '타이머 완료' 메시지 전송
    endBid = (event) => {


        this.showPublicMsg({writer: "SYSTEM", msg: `${this.#currentItem.name} 경매 종료`});

        const bidStatusBtn = document.getElementById("bid-status");
        bidStatusBtn.class = "bg-danger";

        bidStatusBtn.textContent = "입찰 종료";

        document.querySelectorAll(".price-control-panel button").forEach(btn => btn.disabled = true);

        const data = {
            type: "END_BID_TIMER"
        };

        stompClient.send(`/wauction/channel/${this.id}/item/timer-end`,{}, JSON.stringify(data));
    }

    // 채널의 참가자가 입찰을 한 경우, 입찰정보 업데이트
    updateBidInfo(msg) {
        document.querySelector(".bidder-text").textContent = msg.writer;
        document.querySelector(".bid-price").textContent = `${msg.data.price} 포인트`;

    }
    // 낙찰이 된 경매대상을 낙찰자의 낙찰목록에 시각적으로 추가
    assignSoldItem(msg) {
        const item = this.auctionRule.items.find(item => item.id === msg.data.itemId);
        const winningBidder = msg.data.winningBidder;
        const idx = getParticipantIdx(winningBidder);
        const targetParticipant = document.querySelectorAll(".participant-info").item(idx);



        targetParticipant.querySelectorAll(".successful-bid").forEach(el => {
            const positionNameEl = el.querySelector(".name");
            if(positionNameEl.textContent === item.position.name) {
                el.querySelector("img").src = item.img;
                positionNameEl.textContent = item.name;
            }
        });
        const origin = targetParticipant.querySelector(".remaining-point").textContent;
        targetParticipant.querySelector(".remaining-point").textContent = parseInt(origin) - parseInt(msg.data.price);

    }
    // 유찰된 경매대상을 유찰목록에 시각적으로 추가
    addToPutOffList(msg) {
        const target = this.auctionRule.items.find(item => item.id === msg.data.itemId);
        const targetImg = target.img != null ? target.img :"/icon/item.png";
        const html =
            `<div class="item-box">
                <div class="item-img">         
                    <img src="${targetImg}" alt="Item Image">                   
                </div>
            <div class="text-small text-overflow item-box-name">${target.name}</div>
        </div>`

        document.querySelector(".put-off-item-container").insertAdjacentHTML('beforeend', html);
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

    document.querySelectorAll(".price-control-panel button").forEach(btn => btn.addEventListener("click", (event) => {
        channel.bid(event.target.value);
    }));

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

const createTimer = (id, duration, callback, text, canReset) => {
    // We select the div that we want to turn into a progressbar
    const progressbar = document.getElementById(id);
    const remainingTimer = document.querySelector(".progress-bar-text");
    progressbar.className = 'progressbar';

    // We create the div that changes width to show progress
    const progressbarinner = document.createElement('div');

    progressbarinner.className = 'inner';
    progressbarinner.id = "inner" + progressbar.querySelectorAll(".inner").length;

    // Now we set the animation parameters
    progressbarinner.style.animationDuration = duration + "s";

    // Eventually couple a callback
    if (typeof(callback) === 'function') {
        progressbarinner.addEventListener('animationend', callback);
    }

    // Append the progressbar to the main progressbardiv
    progressbar.appendChild(progressbarinner);

    // When everything is set up we start the animation
    progressbarinner.style.animationPlayState = 'running';


    // Update the timer text
    const updateTimerText = () => {
        // Calculate remaining time
        const totalTime = parseFloat(duration) * 1000; // Convert seconds to milliseconds
        const elapsedTime = totalTime - (progressbarinner.getBoundingClientRect().width / progressbar.getBoundingClientRect().width) * totalTime;
        const remainingTime = Math.max(0, totalTime - elapsedTime);


        // Convert remaining time to seconds
        const seconds = Math.ceil(remainingTime / 100) / 10;


        // Display remaining time
        remainingTimer.innerHTML = `${text} <span class="emphasis-remains">${seconds}s</span> `;
    };

    // Update timer text initially
    updateTimerText();

    // Update timer text on animation frame
    const updateTimer = () => {
        updateTimerText();
        requestAnimationFrame(updateTimer);
    };
    requestAnimationFrame(updateTimer);

}

const resetTimer = (id) => {
    const progressbarInner = document.getElementById(id);

    progressbarInner.classList.remove("inner");
    void progressbarInner.offsetWidth;
    progressbarInner.classList.add("inner")
}
const existingTimer = (id) => {
    const timer = document.getElementById(id);
    return timer !== undefined || true
}













