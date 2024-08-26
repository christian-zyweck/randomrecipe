package de.zyweck.mealplanner.infrastructure

import io.restassured.RestAssured
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalToIgnoringCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
class AppKtTest(
    @LocalServerPort private val localPort: Int,
) {
    companion object {
        @Container
        val postgres =
            PostgreSQLContainer("postgres:16-alpine").apply {
                withDatabaseName("mealplanner-junit")
            }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @BeforeEach
    fun setup() {
        RestAssured.port = localPort
    }

    @Test
    fun `application should launch and health actuator should reply 'up'`() {
        When {
            get("/actuator/health")
        } Then {
            statusCode(200)
            body("status", equalToIgnoringCase("up"))
        }
    }
}
