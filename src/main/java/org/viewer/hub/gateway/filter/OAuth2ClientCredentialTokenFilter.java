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
import org.springframework.core.Ordered;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filter for client credential: add token in authorization header
 */
public class OAuth2ClientCredentialTokenFilter implements GatewayFilter, Ordered {

	private final ReactiveOAuth2AuthorizedClientManager authorizedClientManager;

	private final String clientRegistrationId;

	public OAuth2ClientCredentialTokenFilter(
			ReactiveOAuth2AuthorizedClientManager reactiveOAuth2AuthorizedClientManager,
			final String clientRegistrationId) {
		this.authorizedClientManager = reactiveOAuth2AuthorizedClientManager;
		this.clientRegistrationId = clientRegistrationId;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		OAuth2AuthorizeRequest authRequest = OAuth2AuthorizeRequest.withClientRegistrationId(clientRegistrationId)
			.principal("viewer-hub-gateway")
			.build();

		return authorizedClientManager.authorize(authRequest)
			.map(OAuth2AuthorizedClient::getAccessToken)
			.map(OAuth2AccessToken::getTokenValue)
			.flatMap(token -> {
				exchange.getRequest().mutate().headers(headers -> headers.setBearerAuth(token)).build();
				return chain.filter(exchange);
			});
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

}