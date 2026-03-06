package com.example.chattingroom.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        /**
         * 브라우저에서 /uploads/파일명 으로 요청이 오면
         * 실제 서버 컴퓨터의 특정 폴더를 바라보도록 연결.
         *
         * addResourceHandler(주소) 의미는 누군가가 http://localhost:8080/주소 로 접속하면 요청을 잡으란 의미.
         *  ** 를 붙인 의미는, 그 밑의 모든 파일과 하위 폴더를 다 포함하겠다는 의미.
         *
         *  addResourceLocations : 위에서 잡아챈 요청을 실제 서버 컴퓨터의 어느 폴더로 보낼지를 정의.
         *
         *  Cache-Control이 없는 상태기때문에, F5를 눌러 채팅방을 새로고침하면, 또 똑같이 이미지 URL에 대한 요청을 전송하는것을 F12 네트워크 에서 확인할 수 있다.
         */

        registry.addResourceHandler("/uploads/**").addResourceLocations("file:uploads/");
    }
}
