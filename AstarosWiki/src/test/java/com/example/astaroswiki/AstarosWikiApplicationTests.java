package com.example.astaroswiki;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AstarosWikiApplicationTests {

    @Test
    @Disabled("需要 MySQL + Redis 运行 — 集成测试环境待配置。Service 层单元测试覆盖见 AuthServiceTest / PageServiceTest")
    void contextLoads() {
    }

}
