//package com.example.pokerv2.service;
//
//import com.example.pokerv2.model.User;
//import com.example.pokerv2.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//import java.util.Collection;
//
//@RequiredArgsConstructor
//@Service
//public class MyUserDetailsService implements UserDetailsService {
//    private final UserRepository userRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException{
//        User user = userRepository.findByUserId(userId).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));
//
//
//        return new org.springframework.security.core.userdetails.User(
//                user.getUserId(),
//                user.getPassword(),
//                user.getAuthorities()
//        );
//
//        // 블로그에서는 roles를 지정해주길래 해두었는데 오류가 발생합니다
//        // 그렇다고 roles를 작성을 안하면 return 전체가 오류가 발생하는데 어떻게 해결하면 좋나요?
//        // 일단 alt+enter 눌러서 첫번째 뜨는걸로 수정했습니다. return 맨 앞에 (UserDetails) 추가됐습니다
//    }
//}
