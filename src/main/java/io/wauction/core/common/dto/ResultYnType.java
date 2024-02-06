package io.wauction.core.common.dto;

public enum ResultYnType {
    Y("Request processing completed."),
    N("Request processing failed.");

    private String title;

    ResultYnType(String title) {
        this.title = title;
    }
}
