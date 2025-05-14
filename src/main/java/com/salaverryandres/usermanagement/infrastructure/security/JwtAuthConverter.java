package com.salaverryandres.usermanagement.infrastructure.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JwtAuthConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String GROUPS_CLAIM = "cognito:groups";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Extrae las authorities estándar (aunque Cognito no las suele usar)
        JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
        authorities.addAll(defaultConverter.convert(jwt));

        // Extrae grupos de Cognito y los convierte a roles Spring
        List<String> groups = jwt.getClaimAsStringList(GROUPS_CLAIM);
        if (groups != null) {
            authorities.addAll(groups.stream()
                    .map(group -> new SimpleGrantedAuthority(ROLE_PREFIX + group))
                    .toList());
        }

        return authorities;
    }
}
