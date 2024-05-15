package io.wauction.core.channels.entity;

import io.wauction.core.auction.entity.document.Bid;
import lombok.Getter;

import java.util.Arrays;
import java.util.NoSuchElementException;

@Getter
public enum MessageType {
    CHAT("chat") {
        public String makeFullMessage(String ...msg) {
            return msg[0];
        }
    },
    READY("ready") {
        public String makeFullMessage(String ...msg) {
            return "경매시작을 위한 준비가 완료되었습니다.";
        }
    },
    UNREADY("unready") {
        public String makeFullMessage(String ...msg) {
            return "준비완료상태가 취소되었습니다.";
        }
    },
    JOIN("join") {
        public String makeFullMessage(String ...sender) {
            return sender[0] + "님이 채널에 입장하셨습니다";
        }
    },
    LEAVE("leave") {
        public String makeFullMessage(String ...sender) {
            return sender[0] + "님이 채널을 떠났습니다.";
        }
    },
    EXCHANGE("exchange") {
        public String makeFullMessage(String ...sender) {
            return sender[0] + "님이 자리교환을 요청합니다. 수락하시겠습니까?";
        }
    },
    EXCHANGE_RES("exchangeRes") {
        public String makeFullMessage(String ...resultYne) {
            return resultYne[0].equals("Y") ? "요청이 수락되었습니다." : "교환 요청이 거절되었습니다.";
        }
    },
    START("start") {
        public String makeFullMessage(String ...msg) {
            return "경매를 시작합니다.";
        }
    },
    BID("bid") {
        public String makeFullMessage(String ...price) {
            return Integer.parseInt(price[0]) + " 포인트 입찰";
        }
    },
    NEXT("next") {
        public String makeFullMessage(String ...item) {
            return "다음 순서 " + item[0];
        }
    },
    COMPLETE_HIGHLIGHT_PLAY("complete_highlight_play") {
        public String makeFullMessage(String ...msg) {
            return msg[0];
        }
    },
    COMPLETE_BEFORE_BID("complete_before_bid") {
        public String makeFullMessage(String ...msg) {
            return "경매 시작";
        }
    },
    END_BID_TIMER("end_bid_timer") {
        public String makeFullMessage(String ...msg) {
            return "입찰 종료";
        }
    },
    COMPLETE_COUNT("complete_count") {
        public String makeFullMessage(String ...msg) {
            return msg[0];
        }
    },
    SOLD("sold") {
        public String makeFullMessage(String ...bidInfo) {
            // ex) [낙찰된 대상 이름] [낙찰된 포인트] 낙찰
            return bidInfo[0] + bidInfo[1] + " 낙찰";
        }

    },
    FAIL_IN_BID("fail_in_bid") {
        public String makeFullMessage(String ...item) {
            return item + " 유찰";
        }
    };



    private final String title;

    MessageType(String title) {
        this.title = title;
    }

    public static MessageType findByTitle(String title) {
        return Arrays.stream(MessageType.values())
                .filter(type -> type.name().equalsIgnoreCase(title))
                .findAny()
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 메시지 타입입니다."));
    }

    public abstract String makeFullMessage(String ... msg);
}
