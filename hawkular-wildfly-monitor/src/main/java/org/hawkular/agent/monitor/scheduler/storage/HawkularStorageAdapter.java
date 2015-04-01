/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.agent.monitor.scheduler.storage;

import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.hawkular.agent.monitor.log.MsgLogger;
import org.hawkular.agent.monitor.scheduler.config.SchedulerConfiguration;
import org.hawkular.agent.monitor.scheduler.diagnostics.Diagnostics;
import org.hawkular.agent.monitor.scheduler.polling.Task;
import org.hawkular.agent.monitor.service.ServerIdentifiers;

public class HawkularStorageAdapter implements StorageAdapter {

    private final HttpClient httpclient;
    private final KeyResolution keyResolution;
    private SchedulerConfiguration config;
    private Diagnostics diagnostics;
    private ServerIdentifiers selfId;

    public HawkularStorageAdapter() {
        this.httpclient = new DefaultHttpClient();
        this.keyResolution = new KeyResolution();
    }

    @Override
    public SchedulerConfiguration getSchedulerConfiguration() {
        return config;
    }

    @Override
    public void setSchedulerConfiguration(SchedulerConfiguration config) {
        this.config = config;
    }

    @Override
    public void setDiagnostics(Diagnostics diag) {
        this.diagnostics = diag;
    }

    @Override
    public void setSelfIdentifiers(ServerIdentifiers selfId) {
        this.selfId = selfId;
    }

    @Override
    public MetricDataPayloadBuilder getMetricDataPayloadBuilder() {
        return new HawkularMetricDataPayloadBuilder();
    }

    @Override
    public void store(Set<DataPoint> datapoints) {
        if (datapoints == null || datapoints.isEmpty()) {
            return; // nothing to do
        }

        MetricDataPayloadBuilder payloadBuilder = getMetricDataPayloadBuilder();
        for (DataPoint datapoint : datapoints) {
            Task task = datapoint.getTask();
            String key = keyResolution.resolve(task);
            long timestamp = datapoint.getTimestamp();
            double value = datapoint.getValue();
            payloadBuilder.addDataPoint(key, timestamp, value);
        }

        store(payloadBuilder);

        return;
    }

    @Override
    public void store(MetricDataPayloadBuilder payloadBuilder) {
        String jsonPayload = null;
        HttpPost request = null;
        try {
            String tenantId = this.selfId.getFullIdentifier();
            StringBuilder url = new StringBuilder(config.getStorageAdapterConfig().url);
            if (!url.toString().endsWith("/")) {
                url.append("/");
            }
            url.append(tenantId).append("/metrics/numeric/data");
            jsonPayload = payloadBuilder.toPayload().toString();

            request = new HttpPost(url.toString());
            request.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));
            HttpResponse httpResponse = httpclient.execute(request);
            StatusLine statusLine = httpResponse.getStatusLine();

            if (statusLine.getStatusCode() != 200) {
                throw new Exception("status-code=[" + statusLine.getStatusCode() + "], reason=["
                        + statusLine.getReasonPhrase() + "], url=[" + request.getURI() + "]");
            }

            // looks like everything stored successfully
            diagnostics.getMetricRate().mark(payloadBuilder.getNumberDataPoints());

        } catch (Throwable t) {
            MsgLogger.LOG.errorFailedToStoreData(t, jsonPayload);
            diagnostics.getStorageErrorRate().mark(1);
        } finally {
            if (request != null) {
                request.releaseConnection();
            }
        }
    }
}