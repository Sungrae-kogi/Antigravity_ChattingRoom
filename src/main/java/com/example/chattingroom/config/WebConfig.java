package com.example.chattingroom.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
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
         *
         *  3. 실무 적용 사례 (어떤 전략을 어디에 쓸까?)
         * 이 원리를 바탕으로 실제 서비스(네이버, 카카오 등)에서는 데이터의 종류에 따라 캐싱 전략을 완전히 다르게 가져갑니다.
         *
         * 사례 1: 채팅방 사진, 프로필 이미지 (maxAge 길게)
         *
         * 적용: 우리가 방금 적용한 방식입니다. 유저가 올린 강아지 사진은 시간이 지나도 고양이 사진으로 변하지 않습니다.
         *
         * 효과: 불변하는 데이터이므로 1년(365 days)씩 길게 설정하여 트래픽을 아예 차단합니다.
         *
         * 사례 2: 프론트엔드 CSS, JS 파일 (noCache)
         *
         * 적용: 화면 디자인(CSS)이나 자바스크립트는 개발자가 코드를 수정해서 서버를 재배포하면 내용이 바뀝니다. 이때 유저의 브라우저가 과거의 캐시를 고집하면 화면이 깨집니다.
         *
         * 효과: noCache를 걸어두면 브라우저가 매번 "개발자님이 혹시 CSS 수정하셨나요?"라고 묻고, 수정되지 않았을 때만(304 상태 코드) 캐시를 사용합니다. 데이터 전송량은 줄이면서 항상 최신 화면을 보장합니다.
         *
         * 사례 3: 은행 계좌 내역, 개인정보 페이지 (noStore)
         *
         * 적용: 잔액 조회나 결제 영수증 같은 민감한 이미지는 절대 PC방 컴퓨터의 브라우저 찌꺼기(캐시)로 남아있으면 안 됩니다.
         *
         * 효과: noStore를 적용하면 브라우저는 뒤로 가기를 누르든 새로고침을 하든 무조건 서버에서 데이터를 새로 받아옵니다. 보안이 생명일 때 사용합니다.
         */

        registry
                .addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)));    // 이미지같은건 불변하는 데이터이므로 1년 같이 길게 설정하여 트래픽을 차단.
    }
}
