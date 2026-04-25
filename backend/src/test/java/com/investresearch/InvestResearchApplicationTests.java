package com.investresearch;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.ai.anthropic.api-key=test-placeholder",
        "search.provider=mock"
})
class InvestResearchApplicationTests {

    @Test
    void contextLoads() {
    }
}
