package io.wauction.core.auction.entity;


public enum ProceedWay {

    NORMAL("일반 경매"),
    FIFTY_DOLLARS("15달러로 팀 구성하기");

    private String description;

    ProceedWay(String description) {
        this.description = description;
    }


}
