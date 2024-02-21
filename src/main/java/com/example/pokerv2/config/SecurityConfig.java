package com.example.pokerv2.config;

import com.example.pokerv2.model.User;
import com.example.pokerv2.repository.UserRepository;
import com.example.pokerv2.service.AuthFailureHandler;
import com.example.pokerv2.service.UserAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig  {

    private final UserAuthenticationService userAuthenticationService;
    private final UserRepository userRepository;
    private final AuthFailureHandler authFailureHandler;
    private final static String SUBSCRIBE_HEADER = "SUBSCRIBE-ID";
    @Bean
    public BCryptPasswordEncoder encodePwd() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.cors(corsCustomizer -> corsCustomizer.configurationSource(corsConfigurationSource()))
                .csrf((csrfConfig) -> csrfConfig.disable())
                .authorizeHttpRequests((authorizeRequests) ->
                        authorizeRequests
                                .requestMatchers("/join", "/login", "/test/**").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin((formLogin) ->
                        formLogin
                                .loginProcessingUrl("/login")
                                .successHandler((httpServletRequest, httpServletResponse, authentication) -> {
                                    Optional<User> user = userRepository.findByUserId(authentication.getName());
                                    if(user.isPresent()) {
                                        httpServletResponse.setHeader(SUBSCRIBE_HEADER, user.get().getId().toString());
                                    }
                                }).failureHandler(authFailureHandler)
                )
                .userDetailsService(userAuthenticationService);

        return http.build();
    }

//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.userDetailsService(userAuthenticationService).passwordEncoder(encodePwd());
//    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("HEAD","POST","GET","DELETE","PUT"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList(SUBSCRIBE_HEADER));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}