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
package com.blackducksoftware.integration.hub.dataservice.license;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.component.ComponentService;
import com.blackducksoftware.integration.hub.api.license.LicenseService;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.model.view.ComplexLicenseView;
import com.blackducksoftware.integration.hub.model.view.ComponentSearchResultView;
import com.blackducksoftware.integration.hub.model.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.model.view.LicenseView;
import com.blackducksoftware.integration.hub.model.view.components.VersionBomLicenseView;

public class LicenseDataService {
    private final ComponentService componentService;
    private final LicenseService licenseService;

    public LicenseDataService(final ComponentService componentService, final LicenseService licenseService) {
        this.componentService = componentService;
        this.licenseService = licenseService;
    }

    public ComplexLicenseView getComplexLicenseItemFromComponent(final ExternalId externalId) throws IntegrationException {
        final ComponentSearchResultView componentSearchView = componentService.getExactComponentMatch(externalId);
        final String componentVersionUrl = componentSearchView.componentVersionUrl;
        final ComponentVersionView componentVersion = componentService.getView(componentVersionUrl, ComponentVersionView.class);

        return componentVersion.license;
    }

    public LicenseView getLicenseView(final VersionBomLicenseView versionBomLicenseView) throws IntegrationException {
        return getLicenseView(versionBomLicenseView.license);
    }

    public LicenseView getLicenseView(final ComplexLicenseView complexLicenseView) throws IntegrationException {
        return getLicenseView(complexLicenseView.license);
    }

    public LicenseView getLicenseView(final String licenseUrl) throws IntegrationException {
        if (licenseUrl == null) {
            return null;
        }
        final LicenseView licenseView = licenseService.getView(licenseUrl, LicenseView.class);
        return licenseView;
    }

    public String getLicenseText(final LicenseView licenseView) throws IntegrationException {
        return licenseService.getLicenseText(licenseView);
    }
}
