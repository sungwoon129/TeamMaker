package io.wauction.core.channels.entity;

import lombok.Getter;

import java.util.Arrays;
import java.util.NoSuchElementException;

@Getter
public enum MessageType {
    CHAT("chat") {
        public String makeFullMessage(String msg) {
            return msg;
        }
    },
    READY("ready") {
        public String makeFullMessage(String msg) {
            return "경매시작을 위한 준비가 완료되었습니다.";
        }
    },
    UNREADY("unready") {
        public String makeFullMessage(String msg) {
            return "준비완료상태가 취소되었습니다.";
        }
    },
    PRICE("price") {
        public String makeFullMessage(String price) {
            return price + " 포인트 입찰";
        }
    },
    JOIN("join") {
        public String makeFullMessage(String sender) {
            return sender + "님이 채널에 입장하셨습니다";
        }
    },
    LEAVE("leave") {
        public String makeFullMessage(String sender) {
            return sender + "님이 채널을 떠났습니다.";
        }
    },
    EXCHANGE("exchange") {
        public String makeFullMessage(String sender) {
            return sender + "님이 자리교환을 요청합니다. 수락하시겠습니까?";
        }
    },
    EXCHANGE_RES("exchangeRes") {
        public String makeFullMessage(String resultYne) {
            return resultYne.equals("Y") ? "요청이 수락되었습니다." : "교환 요청이 거절되었습니다.";
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

    public abstract String makeFullMessage(String msg);
}
