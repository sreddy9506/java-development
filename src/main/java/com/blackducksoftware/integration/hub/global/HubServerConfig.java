/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.global;

import java.io.Serializable;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.Credentials;
import com.blackducksoftware.integration.hub.model.HubComponent;
import com.blackducksoftware.integration.hub.proxy.ProxyInfo;
import com.blackducksoftware.integration.hub.rest.ApiKeyRestConnection;
import com.blackducksoftware.integration.hub.rest.ApiKeyRestConnectionBuilder;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnectionBuilder;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.IntLogger;

public class HubServerConfig extends HubComponent implements Serializable {
    private static final long serialVersionUID = -1581638027683631935L;

    private final URL hubUrl;
    private final int timeoutSeconds;
    private final Credentials credentials;
    private final String apiKey;
    private final ProxyInfo proxyInfo;
    private final boolean alwaysTrustServerCertificate;

    public HubServerConfig(final URL url, final int timeoutSeconds, final Credentials credentials, final ProxyInfo proxyInfo, final boolean alwaysTrustServerCertificate) {
        this.hubUrl = url;
        this.timeoutSeconds = timeoutSeconds;
        this.credentials = credentials;
        this.apiKey = null;
        this.proxyInfo = proxyInfo;
        this.alwaysTrustServerCertificate = alwaysTrustServerCertificate;
    }

    public HubServerConfig(final URL url, final int timeoutSeconds, final String apiKey, final ProxyInfo proxyInfo, final boolean alwaysTrustServerCertificate) {
        this.hubUrl = url;
        this.timeoutSeconds = timeoutSeconds;
        this.credentials = null;
        this.apiKey = apiKey;
        this.proxyInfo = proxyInfo;
        this.alwaysTrustServerCertificate = alwaysTrustServerCertificate;
    }

    public boolean shouldUseProxyForHub() {
        return proxyInfo != null && proxyInfo.shouldUseProxyForUrl(hubUrl);
    }

    public void print(final IntLogger logger) {
        if (getHubUrl() != null) {
            logger.alwaysLog("--> Hub Server Url : " + getHubUrl());
        }
        if (getGlobalCredentials() != null && StringUtils.isNotBlank(getGlobalCredentials().getUsername())) {
            logger.alwaysLog("--> Hub User : " + getGlobalCredentials().getUsername());
        }
        if (StringUtils.isNotBlank(apiKey)) {
            logger.alwaysLog("--> Hub API Key Used");
        }
        if (alwaysTrustServerCertificate) {
            logger.alwaysLog("--> Trust Hub certificate : " + isAlwaysTrustServerCertificate());
        }
        if (proxyInfo != null) {
            if (StringUtils.isNotBlank(proxyInfo.getHost())) {
                logger.alwaysLog("--> Proxy Host : " + proxyInfo.getHost());
            }
            if (proxyInfo.getPort() > 0) {
                logger.alwaysLog("--> Proxy Port : " + proxyInfo.getPort());
            }
            if (StringUtils.isNotBlank(proxyInfo.getIgnoredProxyHosts())) {
                logger.alwaysLog("--> No Proxy Hosts : " + proxyInfo.getIgnoredProxyHosts());
            }
            if (StringUtils.isNotBlank(proxyInfo.getUsername())) {
                logger.alwaysLog("--> Proxy Username : " + proxyInfo.getUsername());
            }
        }
    }

    public RestConnection createRestConnection(final IntLogger logger) throws EncryptionException {
        if (StringUtils.isNotBlank(apiKey)) {
            return createApiKeyRestConnection(logger);
        } else {
            return createCredentialsRestConnection(logger);
        }
    }

    public CredentialsRestConnection createCredentialsRestConnection(final IntLogger logger) throws EncryptionException {
        final CredentialsRestConnectionBuilder builder = new CredentialsRestConnectionBuilder();
        builder.setLogger(logger);
        builder.setBaseUrl(getHubUrl().toString());
        builder.setTimeout(getTimeout());
        builder.applyCredentials(getGlobalCredentials());
        builder.setAlwaysTrustServerCertificate(isAlwaysTrustServerCertificate());
        builder.applyProxyInfo(getProxyInfo());

        return builder.build();
    }

    public ApiKeyRestConnection createApiKeyRestConnection(final IntLogger logger) {
        final ApiKeyRestConnectionBuilder builder = new ApiKeyRestConnectionBuilder();
        builder.setLogger(logger);
        builder.setBaseUrl(getHubUrl().toString());
        builder.setTimeout(getTimeout());
        builder.setApiKey(getApiKey());
        builder.setAlwaysTrustServerCertificate(isAlwaysTrustServerCertificate());
        builder.applyProxyInfo(getProxyInfo());

        return builder.build();
    }

    public URL getHubUrl() {
        return hubUrl;
    }

    public Credentials getGlobalCredentials() {
        return credentials;
    }

    public String getApiKey() {
        return apiKey;
    }

    public ProxyInfo getProxyInfo() {
        return proxyInfo;
    }

    public int getTimeout() {
        return timeoutSeconds;
    }

    public boolean isAlwaysTrustServerCertificate() {
        return alwaysTrustServerCertificate;
    }

}
