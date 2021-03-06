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
package com.blackducksoftware.integration.hub.api.group;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_GROUPS;

import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.exception.DoesNotExistException;
import com.blackducksoftware.integration.hub.model.view.UserGroupView;
import com.blackducksoftware.integration.hub.model.view.UserView;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubService;

public class GroupService extends HubService {
    public GroupService(final RestConnection restConnection) {
        super(restConnection);
    }

    public List<UserGroupView> getAllGroups() throws IntegrationException {
        return getAllViewsFromApi(SEGMENT_GROUPS, UserGroupView.class);
    }

    public UserGroupView getGroupByName(final String groupName) throws IntegrationException {
        final List<UserGroupView> allGroups = getAllGroups();
        for (final UserGroupView group : allGroups) {
            if (group.name.equalsIgnoreCase(groupName)) {
                return group;
            }
        }
        throw new DoesNotExistException("This Group does not exist. Group name : " + groupName);
    }

    public List<UserView> getAllUsersForGroup(final UserGroupView userGroupView) throws IntegrationException {
        return getAllViewsFromLink(userGroupView, MetaHandler.USERS_LINK, UserView.class);
    }

}
