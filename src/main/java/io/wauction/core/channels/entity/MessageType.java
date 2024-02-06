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
    };

    private final String title;

    MessageType(String title) {
        this.title = title;
    }

    public static MessageType findByTitle(String title) {
        return Arrays.stream(MessageType.values())
                .filter(type -> type.getTitle().equals(title))
                .findAny()
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 메시지 타입입니다."));
    }

    public abstract String makeFullMessage(String msg);
}
