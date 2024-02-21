package com.example.pokerv2.stomp;

import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class StompErrorHandler extends StompSubProtocolErrorHandler{

    @Override
    public Message<byte[]> handleClientMessageProcessingError(
            Message<byte[]> clientMessage,
            Throwable ex) {

        Throwable exception = ex;
        if (exception instanceof MessageDeliveryException)
        {
            return errorMessage("UNAUTHORIZED", clientMessage);
        }
//        else if (exception instanceof CustomException) {
//            return errorMessage(((CustomException) exception).getErrorCode().getDetail(), clientMessage);
//        }


        return super.handleClientMessageProcessingError(clientMessage, ex);
    }

    private Message<byte[]> errorMessage(String errorMessage, Message<byte[]> clientMessage) {

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setLeaveMutable(true);
        accessor.setNativeHeader("errorType", errorMessage);
        Set<String> strings = clientMessage.getHeaders().keySet();

        for (String string : strings)
            accessor.setNativeHeader(string, clientMessage.getHeaders().get(string).toString());

        return MessageBuilder.createMessage(clientMessage.getPayload(),
                accessor.getMessageHeaders());
    }
}