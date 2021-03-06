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
package com.blackducksoftware.integration.hub.api.bom;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_BOM_IMPORT;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubService;

import okhttp3.Response;

public class BomImportService extends HubService {
    private static final List<String> BOM_IMPORT_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_BOM_IMPORT);

    public BomImportService(final RestConnection restConnection) {
        super(restConnection);
    }

    public void importBomFile(final File file) throws IntegrationException {
        importBomFile(file, "application/ld+json");
    }

    public void importBomFile(final File file, final String mediaType) throws IntegrationException {
        Response response = null;
        try {
            final HubRequest hubRequest = getHubRequestFactory().createRequest(BOM_IMPORT_SEGMENTS);
            String jsonPayload;
            try {
                jsonPayload = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            } catch (final IOException e) {
                throw new IntegrationException("Failed to import Bom file: " + file.getAbsolutePath() + " to the Hub with because parsing the file to json failed: " + e.getMessage(), e);
            }
            response = hubRequest.executePost(mediaType, jsonPayload);
        } finally {
            IOUtils.closeQuietly(response);
        }
    }

}
