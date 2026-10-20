package com.zycus.StockPulse.ai;

import java.util.List;

public record AiResponse(
    String id,
    List<Choice> choices
) {
    public record Choice(int index, AiRequest.Message message, String finishReason) {}
}