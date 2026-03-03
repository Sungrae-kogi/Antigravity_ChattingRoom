package com.example.chattingroom;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;

// 싱글톤 컨테이너 패턴 -> 테스트 속도 최적화.
public abstract class BaseIntegrationTest {
    static final MariaDBContainer<?> MARIADB;

    static {
        MARIADB = new MariaDBContainer<>("mariadb:10.11")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass");

        MARIADB.start();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", MARIADB::getJdbcUrl);
        registry.add("spring.datasource.username", MARIADB::getUsername);
        registry.add("spring.datasource.password", MARIADB::getPassword);
        registry.add("spring.sql.init.mode", () -> "always");
    }
}
