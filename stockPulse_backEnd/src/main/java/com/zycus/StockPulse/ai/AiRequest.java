package com.zycus.StockPulse.ai;

import java.util.List;

public record AiRequest(String model, List<Message> messages) {
    public record Message(String role, String content) {}
}