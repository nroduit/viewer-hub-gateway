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
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.viewer.hub.gateway.model.BasicAuthenticationFilterParams;

@Component
public class BasicAuthenticationGatewayFilterFactory
		extends AbstractGatewayFilterFactory<BasicAuthenticationFilterParams> {

	public BasicAuthenticationGatewayFilterFactory() {
		super(BasicAuthenticationFilterParams.class);
	}

	@Override
	public GatewayFilter apply(BasicAuthenticationFilterParams basicAuthenticationFilterParams) {
		return new BasicAuthenticationFilter(basicAuthenticationFilterParams);
	}

}