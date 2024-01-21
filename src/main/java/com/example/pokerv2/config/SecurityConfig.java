package com.example.pokerv2.config;

import com.example.pokerv2.service.MyUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.PrintWriter;
import java.util.Arrays;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final MyUserDetailsService myUserDetailsService;

    @Bean
    public BCryptPasswordEncoder encodePwd() {
        return new BCryptPasswordEncoder();
    }

    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailsService).passwordEncoder(encodePwd());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{


        http.   cors(corsCustomizer -> corsCustomizer.configurationSource(corsConfigurationSource()))
                .csrf((csrfConfig) ->
                    csrfConfig.disable()
                ) // csrf 설정 disable
                  // TODO csrf란?
                .authorizeHttpRequests((authorizeRequests) ->
                        authorizeRequests
                                .requestMatchers("/join","/login").permitAll()
                                .anyRequest().authenticated()
                ) // 자원 요청 별 권한 설정
//                .exceptionHandling((exceptionConfig) ->
//                        exceptionConfig
//                                .authenticationEntryPoint(unauthorizedEntryPoint)
//                                .accessDeniedHandler(accessDeniedHandler)
//                ) // 401(인증) 403(권한) 관련 예외처리
                .formLogin((formLogin) ->
                        formLogin
                                .usernameParameter("username")
                                .passwordParameter("password")
                                .loginProcessingUrl("/login")
                                .successHandler((httpServletRequest, httpServletResponse, authentication) -> {
                                }) //
                )

                .userDetailsService(myUserDetailsService);

        return http.build();
    }
//    public final AuthenticationEntryPoint unauthorizedEntryPoint =
//            (request, response, authException) -> {
////                ErrorResponse fail = new ErrorResponse(HttpStatus.UNAUTHORIZED, "Spring security unauthorized...");
//                response.setStatus(HttpStatus.UNAUTHORIZED.value());
//                String json = new ObjectMapper().writeValueAsString(fail);
//                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//                PrintWriter writer = response.getWriter();
//                writer.write(json);
//                writer.flush();
//            };
//
//    public  final AccessDeniedHandler accessDeniedHandler =
//            (request, response, accessDeniedException) -> {
//                ErrorResponse fail = new ErrorResponse(HttpStatus.FORBIDDEN, "Spring security forbidden...");
//                response.setStatus(HttpStatus.FORBIDDEN.value());
//                String json = new ObjectMapper().writeValueAsString(fail);
//                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//                PrintWriter writer = response.getWriter();
//                writer.write(json);
//                writer.flush();
//            };

//    @Getter
//    @RequiredArgsConstructor
//    public class ErrorResponse {
//
//        private final HttpStatus status;
//        private final String message;
//    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("HEAD","POST","GET","DELETE","PUT"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
