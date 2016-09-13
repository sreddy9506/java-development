package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.restlet.data.Method;

import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationItem;
import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationTypeEnum;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class CodeLocationRestService extends HubRestService<CodeLocationItem> {
	private final List<String> getCodeLocationsSegments = Arrays.asList(UrlConstants.SEGMENT_API,
			UrlConstants.SEGMENT_CODE_LOCATIONS);

	public CodeLocationRestService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser, new TypeToken<CodeLocationItem>() {
		}.getType(), new TypeToken<List<CodeLocationItem>>() {
		}.getType());
	}

	public List<CodeLocationItem> getAllCodeLocations() throws IOException, BDRestException, URISyntaxException {
		final HubRequest codeLocationItemRequest = new HubRequest(getRestConnection(), getJsonParser());
		codeLocationItemRequest.setMethod(Method.GET);
		codeLocationItemRequest.setLimit(100);
		codeLocationItemRequest.addUrlSegments(getCodeLocationsSegments);

		final JsonObject jsonObject = codeLocationItemRequest.executeForResponseJson();
		final List<CodeLocationItem> allCodeLocations = getAll(jsonObject, codeLocationItemRequest);
		return allCodeLocations;
	}

	public List<CodeLocationItem> getCodeLocations(final CodeLocationTypeEnum codeLocationType, final String groupId,
			final String artifactId, final String version) throws IOException, BDRestException, URISyntaxException {
		final String gav = groupId + "/" + artifactId + "/" + version;
		final HubRequest codeLocationItemRequest = new HubRequest(getRestConnection(), getJsonParser());
		codeLocationItemRequest.setMethod(Method.GET);
		codeLocationItemRequest.setLimit(100);
		codeLocationItemRequest.setQ(gav);
		codeLocationItemRequest.addQueryParameter("codeLocationType", codeLocationType.toString());
		codeLocationItemRequest.addUrlSegments(getCodeLocationsSegments);

		final JsonObject jsonObject = codeLocationItemRequest.executeForResponseJson();
		final List<CodeLocationItem> allCodeLocations = getAll(jsonObject, codeLocationItemRequest);
		return allCodeLocations;
	}

}
