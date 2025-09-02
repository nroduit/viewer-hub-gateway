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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;

/**
 * Manage OAuth2 configuration
 */
@Configuration(proxyBeanMethods = false)
public class OAuth2ClientConfiguration {

	/**
	 * Register an OAuth2AuthorizedClientManager and associate it with an
	 * OAuth2AuthorizedClientProvider that provides support for client_credentials
	 * authorization grant type
	 * @param client Client repository
	 * @return OAuth2AuthorizedClientManager Client manager
	 */
	@Bean
	public ReactiveOAuth2AuthorizedClientManager clientCredentialsAuthorizedClientManager(
			ReactiveClientRegistrationRepository client) {
		ReactiveOAuth2AuthorizedClientService service = new InMemoryReactiveOAuth2AuthorizedClientService(client);
		AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager manager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
				client, service);
		ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder
			.builder()
			.clientCredentials()
			.build();
		manager.setAuthorizedClientProvider(authorizedClientProvider);
		return manager;
	}

	/**
	 * Register an OAuth2AuthorizedClientManager and associate it with an
	 * OAuth2AuthorizedClientProvider that provides support for authorization code
	 * authorization grant type
	 * @param client Client repository
	 * @return OAuth2AuthorizedClientManager Client manager
	 */
	@Bean
	public ReactiveOAuth2AuthorizedClientManager authorizationCodeAuthorizedClientManager(
			ReactiveClientRegistrationRepository client) {
		ReactiveOAuth2AuthorizedClientService service = new InMemoryReactiveOAuth2AuthorizedClientService(client);
		AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager manager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
				client, service);
		ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder
			.builder()
			.authorizationCode()
			.build();
		manager.setAuthorizedClientProvider(authorizedClientProvider);
		return manager;
	}

}