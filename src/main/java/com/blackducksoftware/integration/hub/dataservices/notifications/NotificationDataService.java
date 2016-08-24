package com.blackducksoftware.integration.hub.dataservices.notifications;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import com.blackducksoftware.integration.hub.api.ComponentVersionRestService;
import com.blackducksoftware.integration.hub.api.NotificationRestService;
import com.blackducksoftware.integration.hub.api.PolicyRestService;
import com.blackducksoftware.integration.hub.api.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.VersionBomPolicyRestService;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.hub.dataservices.AbstractDataService;
import com.blackducksoftware.integration.hub.dataservices.notifications.items.AbstractItemCount;
import com.blackducksoftware.integration.hub.dataservices.notifications.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notifications.items.NotificationCountData;
import com.blackducksoftware.integration.hub.dataservices.notifications.items.NotificationItemCount;
import com.blackducksoftware.integration.hub.dataservices.notifications.items.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservices.notifications.items.VulnerabilityItemCount;
import com.blackducksoftware.integration.hub.dataservices.notifications.transforms.AbstractNotificationTransform;
import com.blackducksoftware.integration.hub.dataservices.notifications.transforms.PolicyViolationOverrideTransform;
import com.blackducksoftware.integration.hub.dataservices.notifications.transforms.PolicyViolationTransform;
import com.blackducksoftware.integration.hub.dataservices.notifications.transforms.VulnerabilityTransform;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class NotificationDataService extends AbstractDataService {
	private final NotificationRestService notificationService;
	private final ProjectVersionRestService projectVersionService;
	private final PolicyRestService policyService;
	private final VersionBomPolicyRestService bomVersionPolicyService;
	private final ComponentVersionRestService componentVersionService;
	private final Map<Class<?>, AbstractNotificationTransform> transformMap;
	private final Map<Class<?>, AbstractItemCount> counterMap;
	private final ExecutorService executorService;
	private final ExecutorCompletionService<List<NotificationContentItem>> completionService;
	private final PolicyNotificationFilter policyFilter;

	public NotificationDataService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser,
			final PolicyNotificationFilter policyFilter) {
		super(restConnection, gson, jsonParser);
		notificationService = new NotificationRestService(restConnection, jsonParser);
		projectVersionService = new ProjectVersionRestService(restConnection, gson, jsonParser);
		policyService = new PolicyRestService(restConnection, gson, jsonParser);
		bomVersionPolicyService = new VersionBomPolicyRestService(restConnection, gson, jsonParser);
		componentVersionService = new ComponentVersionRestService(restConnection, gson, jsonParser);
		this.policyFilter = policyFilter;
		transformMap = createTransformMap();
		counterMap = createCounterMap();
		final ThreadFactory threadFactory = Executors.defaultThreadFactory();
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);
		completionService = new ExecutorCompletionService<>(executorService);
	}

	private Map<Class<?>, AbstractNotificationTransform> createTransformMap() {
		final Map<Class<?>, AbstractNotificationTransform> transformMap = new HashMap<>();
		transformMap.put(RuleViolationNotificationItem.class, new PolicyViolationTransform(notificationService,
				projectVersionService, policyService, bomVersionPolicyService, componentVersionService, policyFilter));
		transformMap.put(PolicyOverrideNotificationItem.class, new PolicyViolationOverrideTransform(notificationService,
				projectVersionService, policyService, bomVersionPolicyService, componentVersionService, policyFilter));
		transformMap.put(VulnerabilityNotificationItem.class, new VulnerabilityTransform(notificationService,
				projectVersionService, policyService, bomVersionPolicyService, componentVersionService));

		return transformMap;
	}

	private Map<Class<?>, AbstractItemCount> createCounterMap() {
		final Map<Class<?>, AbstractItemCount> counterMap = new HashMap<>();
		counterMap.put(RuleViolationNotificationItem.class, new NotificationItemCount());
		counterMap.put(PolicyOverrideNotificationItem.class, new NotificationItemCount());
		counterMap.put(VulnerabilityNotificationItem.class, new VulnerabilityItemCount());

		return counterMap;
	}

	public List<NotificationContentItem> getAllNotifications(final Date startDate, final Date endDate)
			throws IOException, URISyntaxException, BDRestException {
		final List<NotificationContentItem> contentList = new ArrayList<>();
		final List<NotificationItem> itemList = notificationService.getAllNotifications(startDate, endDate);

		final int count = itemList.size();

		for (final NotificationItem item : itemList) {
			final Class<? extends NotificationItem> key = item.getClass();
			if (transformMap.containsKey(key)) {
				final AbstractNotificationTransform converter = transformMap.get(key);
				final TransformCallable callable = new TransformCallable(item, converter);
				completionService.submit(callable);
			}
		}

		for (int index = 0; index < count; index++) {
			try {
				final Future<List<NotificationContentItem>> future = completionService.take();
				contentList.addAll(future.get());
			} catch (final ExecutionException | InterruptedException e) {

			}
		}

		return contentList;
	}

	public NotificationCountData getNotificationCounts(final Date startDate, final Date endDate)
			throws IOException, URISyntaxException, BDRestException {

		final List<NotificationItem> itemList = notificationService.getAllNotifications(startDate, endDate);
		final int count = itemList.size();

		for (final NotificationItem item : itemList) {
			final Class<? extends NotificationItem> key = item.getClass();
			if (counterMap.containsKey(key)) {
				final AbstractItemCount counter = counterMap.get(key);
				counter.increment(item);
			}
		}

		final AbstractItemCount policyRuleCounter = counterMap.get(RuleViolationNotificationItem.class);
		final AbstractItemCount policyOverrideCounter = counterMap.get(PolicyOverrideNotificationItem.class);
		final VulnerabilityItemCount vulnCounter = (VulnerabilityItemCount) counterMap
				.get(VulnerabilityNotificationItem.class);

		final int policyViolationCount = policyRuleCounter.getCount();
		final int policyOverrideCount = policyOverrideCounter.getCount();
		final int vulnCount = vulnCounter.getCount();
		final int vulnAddedCount = vulnCounter.getAddedCount();
		final int vulnUpdatedCount = vulnCounter.getUpdatedCount();
		final int vulnDeletedCount = vulnCounter.getDeletedCount();

		// reset counters
		for (final Map.Entry<Class<?>, AbstractItemCount> entry : counterMap.entrySet()) {
			entry.getValue().reset();
		}

		return new NotificationCountData(startDate, endDate, count, policyViolationCount, policyOverrideCount,
				vulnCount, vulnAddedCount, vulnUpdatedCount, vulnDeletedCount);
	}

	private class TransformCallable implements Callable<List<NotificationContentItem>> {
		private final NotificationItem item;
		private final AbstractNotificationTransform converter;

		public TransformCallable(final NotificationItem item, final AbstractNotificationTransform converter) {
			this.item = item;
			this.converter = converter;
		}

		@Override
		public List<NotificationContentItem> call() throws Exception {
			return converter.transform(item);
		}
	}
}
