package com.example.demo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Legacy tests replaced by com.example.books suite")
class DemoApplicationTests {
    @Test
    void disabled() {
    }
}
