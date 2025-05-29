package com.gogo.api_gateway_service.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

public class ReactiveKeycloakRealmRoleConverter
        implements Converter<Jwt, Flux<GrantedAuthority>> {

    @Override
    public Flux<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");

        if (realmAccess == null || !realmAccess.containsKey("roles")) {
            return Flux.empty();
        }

        List<String> roles = (List<String>) realmAccess.get("roles");

        return Flux.fromIterable(
                roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role)) // Spring Security needs ROLE_ prefix
                        .toList()
        );
    }
}


