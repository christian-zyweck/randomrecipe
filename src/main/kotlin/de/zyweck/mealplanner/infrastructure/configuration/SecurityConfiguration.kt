package de.zyweck.mealplanner.infrastructure.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.web.SecurityFilterChain

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
class SecurityConfiguration {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }.sessionManagement { sessionManagement -> sessionManagement.sessionCreationPolicy(STATELESS) }
            .cors { it.disable() }
            .csrf { it.disable() }
            .oauth2ResourceServer { oauth2 -> oauth2.jwt { } }
            .build()
}
