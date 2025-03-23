package com.ssaha;

import java.util.Objects;

public record Message(String msg) {
    public Message {
        Objects.requireNonNull(msg);
    }
}
