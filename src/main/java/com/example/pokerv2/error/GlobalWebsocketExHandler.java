package com.example.pokerv2.error;

import com.example.pokerv2.dto.MessageDto;
import com.example.pokerv2.enums.MessageType;
import com.example.pokerv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalWebsocketExHandler {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserRepository userRepository;
    private static final String BOARD_ID = "board-id";

    @MessageExceptionHandler
    public void handleCustomException(CustomException ex, Message<?> message, Principal principal) {

        String boardId = message.getHeaders().get(BOARD_ID).toString();
        userRepository.findByUserId(principal.getName())
                .ifPresent(u -> {
                    simpMessagingTemplate.convertAndSendToUser(principal.getName(), "/queue/error/" + boardId + "/" + u.getId(), new MessageDto(MessageType.ERROR.toString(), ex.getMessage()));
                });
    }

}