package co.edu.escuelaing.securetutorialauth0.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.core.convert.converter.Converter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // Deshabilita CSRF para simplificar testing de API
            .csrf(csrf -> csrf.disable())

            // Configura rutas
            .authorizeHttpRequests(auth -> auth
                // recursos estáticos permitidos sin login
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                // API endpoints protegidos solo con JWT
                .requestMatchers("/api/**").authenticated()
                // cualquier otra ruta puede usar login web u otras configuraciones
                .anyRequest().permitAll()
            )

            // JWT Resource Server para API
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            )

            // Login web (opcional, si quieres login en navegador)
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/", true)
            );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(new Converter<Jwt, Collection<GrantedAuthority>>() {
            @Override
            public Collection<GrantedAuthority> convert(Jwt jwt) {
                return extractPermissions(jwt);
            }
        });

        return converter;
    }

    private Collection<GrantedAuthority> extractPermissions(Jwt jwt) {
        List<String> permissions = jwt.getClaimAsStringList("permissions");
        if (permissions == null) return List.of();

        return permissions.stream()
                .map(p -> new SimpleGrantedAuthority("PERM_" + p))
                .collect(Collectors.toList());
    }
}