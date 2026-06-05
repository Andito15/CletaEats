package com.cletaeats.config

import com.cletaeats.security.JwtAuthenticationFilter
import jakarta.servlet.DispatcherType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors(Customizer.withDefaults())
            .csrf { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    .requestMatchers(
                        "/",
                        "/health",
                        "/error",
                        "/uploads/images/**"
                    ).permitAll()

                    .requestMatchers(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/uploads/imagen"
                    ).permitAll()

                    .requestMatchers("/api/auth/me").authenticated()

                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/restaurantes/**"
                    ).permitAll()

                    .requestMatchers("/api/admin/**").hasRole("ADMIN")

                    .requestMatchers(HttpMethod.PATCH, "/api/repartidores/ubicacion")
                    .hasRole("REPARTIDOR")

                    .requestMatchers(HttpMethod.GET, "/api/clientes/pedidos/*/tracking")
                    .hasRole("CLIENTE")

                    .requestMatchers("/api/clientes/**")
                    .hasAnyRole("CLIENTE", "ADMIN")

                    .requestMatchers("/api/repartidores/**")
                    .hasAnyRole("REPARTIDOR", "ADMIN")

                    .anyRequest().authenticated()
            }
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        configuration.allowedOriginPatterns = listOf(
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "https://*.netlify.app",
            "https://*.vercel.app",
            "https://cletaeats.onrender.com"
        )

        configuration.allowedMethods = listOf(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS"
        )

        configuration.allowedHeaders = listOf("*")
        configuration.exposedHeaders = listOf("Authorization")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)

        return source
    }
}