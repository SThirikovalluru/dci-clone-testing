package net.datto.dciservice;

import net.datto.dciservice.configuration.SpringTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(SpringTestConfig.class)
class DciServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
