package com.wasteai.dto;

import java.util.List;

public record SessionResponse(String sessionId, List<MessageDto> messages) {
}
