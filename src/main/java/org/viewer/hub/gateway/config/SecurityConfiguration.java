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


import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.viewer.hub.gateway.config.properties.GatewayRouteDefinitionProperties;

import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration(proxyBeanMethods = false)
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
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
		Set<String> routesPaths = gatewayRouteDefinitionProperties.getRoutes()
			.stream()
			.map(RouteDefinition::getId)
			.map("/%s/**"::formatted)
			.collect(Collectors.toSet());

		http.authorizeExchange((exchange) -> {
			exchange.matchers(EndpointRequest.to(ShutdownEndpoint.class)).denyAll();
			if (!routesPaths.isEmpty()) {
				exchange.pathMatchers(routesPaths.toArray(String[]::new)).permitAll();
			}
			exchange.pathMatchers("/login", "/actuator/**").permitAll();
			exchange.anyExchange().authenticated();
		});

		// Activate OAuth2 client
		http.oauth2Client(withDefaults());

		return http.build();
	}

}