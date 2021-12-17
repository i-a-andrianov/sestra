package sestra.common.rest

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager

@Configuration
@EnableWebSecurity
class WebSecurityConfig(
    @Value("\${password.admin}") private val adminPassword: String
) : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
            .antMatchers("/api/**")
            .authenticated()
            .anyRequest()
            .permitAll()

        http.httpBasic()
        http.csrf().disable()
        http.formLogin().disable()
    }

    @Bean
    override fun userDetailsService(): UserDetailsService {
        val user = User.builder()
            .username("admin")
            .password(adminPassword)
            .roles("ADMIN")
            .passwordEncoder(passwordEncoder()::encode)
            .build()

        return InMemoryUserDetailsManager(user)
    }

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()
}
