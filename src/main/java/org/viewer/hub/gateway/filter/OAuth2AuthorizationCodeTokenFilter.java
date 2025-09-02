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

package org.viewer.hub.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filter for authorization code: add token in authorization header
 */
public class OAuth2AuthorizationCodeTokenFilter implements GatewayFilter {

	private final ReactiveOAuth2AuthorizedClientManager authorizedClientManager;

	private final String clientRegistrationId;

	public OAuth2AuthorizationCodeTokenFilter(
			ReactiveOAuth2AuthorizedClientManager reactiveOAuth2AuthorizedClientManager, String clientRegistrationId) {
		this.authorizedClientManager = reactiveOAuth2AuthorizedClientManager;
		this.clientRegistrationId = clientRegistrationId;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		return exchange.getPrincipal()
			.cast(Authentication.class)
			.flatMap(auth -> authorize(auth, exchange))
			.flatMap(client -> {
				String token = client.getAccessToken().getTokenValue();
				ServerWebExchange mutated = exchange.mutate()
					.request(r -> r.headers(h -> h.setBearerAuth(token)))
					.build();
				return chain.filter(mutated);
			})
		/* .switchIfEmpty(unauthorized(exchange)) */;
	}

	private Mono<OAuth2AuthorizedClient> authorize(Authentication principal, ServerWebExchange exchange) {
		OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest.withClientRegistrationId(clientRegistrationId)
			.principal(principal.getName())
			.attributes(attrs -> attrs.put(ServerWebExchange.class.getName(), exchange))
			.build();

		return authorizedClientManager.authorize(request);
	}

	private Mono<Void> unauthorized(ServerWebExchange exchange) {
		exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
		return exchange.getResponse().setComplete();
	}

}