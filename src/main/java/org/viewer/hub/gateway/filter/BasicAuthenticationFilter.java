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
import org.springframework.web.server.ServerWebExchange;
import org.viewer.hub.gateway.model.BasicAuthenticationFilterParams;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Filter for basic authentication: add basic credentials in authorization header
 */
public class BasicAuthenticationFilter implements GatewayFilter {

	private final BasicAuthenticationFilterParams basicAuthenticationFilterParams;

	public BasicAuthenticationFilter(BasicAuthenticationFilterParams basicAuthenticationFilterParams) {
		this.basicAuthenticationFilterParams = basicAuthenticationFilterParams;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerWebExchange modifiedExchange = exchange.mutate()
			.request(r -> r.headers(headers -> headers.setBasicAuth(Base64.getEncoder()
					.encodeToString("%s:%s"
							.formatted(basicAuthenticationFilterParams.getUser(),
									basicAuthenticationFilterParams.getPassword())
							.getBytes(StandardCharsets.UTF_8)))))
			.build();

		return chain.filter(modifiedExchange);
	}

}