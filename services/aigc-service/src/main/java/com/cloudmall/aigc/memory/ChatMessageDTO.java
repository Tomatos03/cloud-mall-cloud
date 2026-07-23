package com.cloudmall.aigc.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.content.MediaContent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {

    private MessageType type;
    private String content;
    private Map<String, Object> metadata;
    private List<Media> media;
    private List<AssistantMessage.ToolCall> toolCalls;
    private List<ToolResponseMessage.ToolResponse> toolResponses;

    public Message toMessage() {
        Map<String, Object> meta = metadata != null ? metadata : Collections.emptyMap();
        return switch (type) {
            case USER -> {
                UserMessage.Builder builder = UserMessage.builder().text(content).metadata(meta);
                if (media != null) {
                    builder.media(media);
                }
                yield builder.build();
            }
            case ASSISTANT -> {
                var builder = AssistantMessage.builder()
                        .content(content)
                        .properties(meta);
                if (toolCalls != null) {
                    builder.toolCalls(toolCalls);
                }
                if (media != null) {
                    builder.media(media);
                }
                yield builder.build();
            }
            case SYSTEM -> SystemMessage.builder().text(content).metadata(meta).build();
            case TOOL -> {
                ToolResponseMessage.Builder builder = ToolResponseMessage.builder().metadata(meta);
                if (toolResponses != null) {
                    builder.responses(toolResponses);
                }
                yield builder.build();
            }
        };
    }

    public static ChatMessageDTO fromMessage(Message message) {
        ChatMessageDTOBuilder builder = ChatMessageDTO.builder()
                .type(message.getMessageType())
                .content(message.getText())
                .metadata(message.getMetadata() != null ? new HashMap<>(message.getMetadata()) : null);

        if (message instanceof MediaContent mediaContent && !mediaContent.getMedia().isEmpty()) {
            builder.media(mediaContent.getMedia());
        }

        if (message instanceof AssistantMessage assistantMessage && assistantMessage.hasToolCalls()) {
            builder.toolCalls(assistantMessage.getToolCalls());
        }

        if (message instanceof ToolResponseMessage toolResponseMessage) {
            builder.toolResponses(toolResponseMessage.getResponses());
        }

        return builder.build();
    }

}
