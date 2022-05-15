package sestra

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import sestra.projects.impl.TestContainersPostgres

@SpringBootTest(properties = [TestContainersPostgres.url])
class SestraApplicationTests {

    @Test
    fun contextLoads() {
    }
}
