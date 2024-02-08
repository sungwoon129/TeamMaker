package io.wauction.core.channels.entity;

public enum ChannelState {

    WAITING("대기 중"),
    PLAYING("진행 중"),
    FULL("만원"),
    END("종료");

    private final String text;

    ChannelState(String text) {
        this.text = text;
    }

}
