package dev.maldallija.maldallijabe.common.config

import dev.maldallija.maldallijabe.auth.adapter.`in`.filter.AdministratorAuthorizationFilter
import dev.maldallija.maldallijabe.auth.adapter.`in`.filter.AuthExceptionHandlerFilter
import dev.maldallija.maldallijabe.auth.adapter.`in`.filter.AuthenticationFilter
import dev.maldallija.maldallijabe.auth.adapter.`in`.filter.CustomAccessDeniedHandler
import dev.maldallija.maldallijabe.auth.adapter.`in`.filter.CustomAuthenticationEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val authExceptionHandlerFilter: AuthExceptionHandlerFilter,
    private val authenticationFilter: AuthenticationFilter,
    private val administratorAuthorizationFilter: AdministratorAuthorizationFilter,
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val customAccessDeniedHandler: CustomAccessDeniedHandler,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { it.anyRequest().permitAll() }
            .exceptionHandling { exception ->
                exception
                    .authenticationEntryPoint(customAuthenticationEntryPoint)
                    .accessDeniedHandler(customAccessDeniedHandler)
            }.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(authExceptionHandlerFilter, AuthenticationFilter::class.java)
            .addFilterAfter(administratorAuthorizationFilter, AuthenticationFilter::class.java)
            .build()

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
