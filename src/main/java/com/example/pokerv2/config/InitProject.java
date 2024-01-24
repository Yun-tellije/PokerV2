package com.example.pokerv2.config;

import com.example.pokerv2.model.User;
import com.example.pokerv2.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class InitProject {

    private final UserService userService;

    @EventListener(ApplicationReadyEvent.class)
    public void initData() {

        try {
            userService.join(User.builder().userName("a1").password("1234").userId("a1").build());
            userService.join(User.builder().userName("a2").password("1234").userId("a2").build());
            userService.join(User.builder().userName("a3").password("1234").userId("a3").build());
            log.info("Initial Data Init");
        } catch (Exception e) {
        }
    }
}