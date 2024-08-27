package de.zyweck.mealplanner.infrastructure

import dasniko.testcontainers.keycloak.KeycloakContainer
import io.restassured.RestAssured
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.path.json.JsonPath
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
    private val clientId = "api-user"

    companion object {
        @Container
        val postgres =
            PostgreSQLContainer("postgres:16-alpine").apply {
                withDatabaseName("mealplanner-junit")
            }

        @Container
        val keycloakContainer =
            KeycloakContainer().apply {
                withRealmImportFile("/keycloak/mealplanner-realm.json")
            }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri") { keycloakContainer.authServerUrl + "/realms/mealplanner" }
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
        val token = getToken(clientId, "mpadmin", "mpadmin")
        Given {
            auth().oauth2(token)
        } When {
            get("/i/dont/exist")
        } Then {
            statusCode(404)
        }
    }

    private fun getToken(
        clientId: String,
        userName: String,
        password: String,
    ): String {
        val response =
            Given {
                contentType("application/x-www-form-urlencoded")
                formParam("grant_type", "password")
                formParam("client_id", clientId)
                formParam("username", userName)
                formParam("password", password)
            } When {
                post("${keycloakContainer.authServerUrl}/realms/mealplanner/protocol/openid-connect/token")
            }

        val responseBody = response.body.asString()

        return JsonPath(responseBody).getString("access_token")
    }
}
