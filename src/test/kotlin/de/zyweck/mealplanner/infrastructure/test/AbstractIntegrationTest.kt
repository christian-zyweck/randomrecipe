package de.zyweck.mealplanner.infrastructure.test

import dasniko.testcontainers.keycloak.KeycloakContainer
import io.restassured.RestAssured
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.When
import io.restassured.path.json.JsonPath
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
abstract class AbstractIntegrationTest(
    private val localPort: Int,
) {
    companion object {
        val postgresContainer =
            PostgreSQLContainer("postgres:16-alpine").apply {
                withDatabaseName("mealplanner-junit")
            }

        val keycloakContainer =
            KeycloakContainer().apply {
                withRealmImportFile("/keycloak/mealplanner-realm.json")
            }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri") { keycloakContainer.authServerUrl + "/realms/mealplanner" }
        }

        @BeforeAll
        @JvmStatic
        fun startContainers() {
            postgresContainer.start()
            keycloakContainer.start()
        }

        @AfterAll
        @JvmStatic
        fun stopContainers() {
            postgresContainer.stop()
            keycloakContainer.stop()
        }
    }

    @BeforeEach
    fun setup() {
        RestAssured.port = localPort
    }

    fun getAccessToken(
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
