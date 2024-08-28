package de.zyweck.mealplanner.infrastructure

import de.zyweck.mealplanner.infrastructure.test.AbstractIntegrationTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalToIgnoringCase
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = RANDOM_PORT)
class AppKtTest(
    @LocalServerPort private val localPort: Int,
) : AbstractIntegrationTest(localPort) {
    private val clientId = "api-user"

    @Test
    fun `application should launch and health actuator should reply 'up'`() {
        When {
            get("/actuator/health")
        } Then {
            statusCode(200)
            body("status", equalToIgnoringCase("up"))
        }
    }

    @Test
    fun `unauthenticated request to a non-existent controller should result in HTTP-403`() {
        When {
            get("/i/dont/exist")
        } Then {
            statusCode(401)
        }
    }

    @Test
    fun `authorized request to a non-existent controller should result in http-404`() {
        val token = getAccessToken(clientId, "mpadmin", "mpadmin")
        Given {
            auth().oauth2(token)
        } When {
            get("/i/dont/exist")
        } Then {
            statusCode(404)
        }
    }
}
