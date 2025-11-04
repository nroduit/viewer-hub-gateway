/*
 *  Copyright (c) 2022-2025 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.viewer.hub.gateway.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.viewer.hub.gateway.config.properties.GatewayRouteDefinitionProperties;
import org.viewer.hub.gateway.constant.Token;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration(proxyBeanMethods = false)
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Slf4j
public class SecurityConfiguration {

	@Bean
	SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http,
			GatewayRouteDefinitionProperties gatewayRouteDefinitionProperties) {
		// Disable cors because it is manage by api gateway
		http.cors(ServerHttpSecurity.CorsSpec::disable);

		// State-less session (state in access-token only)
		http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance());

		// Disable CSRF because of state-less session-management
		http.csrf(ServerHttpSecurity.CsrfSpec::disable);

		// Format the gateway routes to allow
		Set<String> routePaths = gatewayRouteDefinitionProperties.getRoutes()
			.stream()
			.map(RouteDefinition::getId)
			.map("/%s/**"::formatted)
			.collect(Collectors.toSet());

		// Define the routes permissions
		http.authorizeExchange((exchange) -> {
			exchange.matchers(EndpointRequest.to(ShutdownEndpoint.class)).denyAll();
			if (!routePaths.isEmpty()) {
				exchange.pathMatchers(routePaths.toArray(String[]::new))
						.hasRole(Token.ROLE_IMAGING_GTW_READ);
			}
			exchange.pathMatchers("/login", "/actuator/**").permitAll();
			exchange.anyExchange().authenticated();
		});

		// Activate OAuth2 client
		http.oauth2Client(withDefaults());

		// Activate OAuth2 resource server in order to validate the incoming Jwt
		http.oauth2ResourceServer(oauth2 -> oauth2
				.jwt(jwt -> jwt.jwtAuthenticationConverter(this::jwtAuthenticationConverter)));

		return http.build();
	}

	// Custom converter which retrieve roles from JWT
	private Mono<? extends AbstractAuthenticationToken> jwtAuthenticationConverter(Jwt jwt) {
		List<SimpleGrantedAuthority>  authorities = retrieveRolesFromAccessToken(jwt);
		return Mono.just(new JwtAuthenticationToken(jwt, authorities));
	}

	/**
	 * Retrieve role from JWT (access token).
	 * @param jwt jwt token
	 * @return roles found
	 */
	private List<SimpleGrantedAuthority> retrieveRolesFromAccessToken(Jwt jwt) {
		// Extract Jwt roles
		return Optional.ofNullable(jwt.getClaims())
				.map(claims -> (Map<String, Object>) claims.get(Token.RESOURCE_ACCESS))
				.map(resourceAccess -> (Map<String, Object>) resourceAccess.get(Token.RESOURCE_NAME))
				.map(resourceNameMap -> (List<String>) resourceNameMap.get(Token.ROLES))
				.orElse(Collections.emptyList())
				.stream()
				.map(roleName -> String.format("%s%s", Token.PREFIX_ROLE, roleName))
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());
	}

}