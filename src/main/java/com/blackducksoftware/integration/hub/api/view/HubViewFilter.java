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
package com.blackducksoftware.integration.hub.api.view;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.HubView;
import com.blackducksoftware.integration.hub.model.enumeration.AllowEnum;

public class HubViewFilter<T extends HubView> {
    public List<T> getAccessibleItems(final MetaHandler metaService, final List<T> hubItems) throws HubIntegrationException {
        final List<T> accessibleItems = new ArrayList<>();
        for (final T hubItem : hubItems) {
            final List<AllowEnum> allow = metaService.getAllowedMethods(hubItem);
            if (allow != null && !allow.isEmpty() && allow.contains(AllowEnum.GET) && allow.contains(AllowEnum.PUT)) {
                accessibleItems.add(hubItem);
            }
        }
        return accessibleItems;
    }

}
