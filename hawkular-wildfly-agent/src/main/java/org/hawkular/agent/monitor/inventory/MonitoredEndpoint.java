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
package org.hawkular.agent.monitor.inventory;

import javax.net.ssl.SSLContext;

import org.hawkular.agent.monitor.extension.MonitorServiceConfiguration.EndpointConfiguration;

/**
 * A named protocol specific endpoint to monitor.
 *
 * @author John Mazzitelli
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 *
 */
public final class MonitoredEndpoint {

    /** A convenience constant */
    private static final MonitoredEndpoint DEFAULT_LOCAL_DMR_ENDPOINT = new MonitoredEndpoint("_self", null, null);

    /**
     * @return {@link #DEFAULT_LOCAL_DMR_ENDPOINT}
     */
    public static MonitoredEndpoint getDefaultLocalDmrEndpoint() {
        return DEFAULT_LOCAL_DMR_ENDPOINT;
    }

    /**
     * Returns a new {@link MonitoredEndpoint} using name and {@link ConnectionData} from the given
     * {@code endpointConfiguration}.
     *
     * @param endpointConfiguration the configuration to read from when creating the result
     * @param sslContext the {@link SSLContext} to associate with the result or {@code null} if no context should be
     *            associated
     * @return a new {@link MonitoredEndpoint}
     */
    public static MonitoredEndpoint of(EndpointConfiguration endpointConfiguration, SSLContext sslContext) {
        return new MonitoredEndpoint(endpointConfiguration.getName(), endpointConfiguration.getConnectionData(),
                sslContext);
    }

    private final ConnectionData connectionData;
    private final String name;

    private final SSLContext sslContext;

    MonitoredEndpoint(String name, ConnectionData connectionData, SSLContext sslContext) {
        super();
        if (name == null) {
            throw new IllegalArgumentException("Cannot create a new [" + getClass().getName() + "] with a null name");
        }
        this.name = name;
        this.connectionData = connectionData;
        this.sslContext = sslContext;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MonitoredEndpoint)) {
            return false;
        }
        return name.equals(((MonitoredEndpoint) obj).name);
    }

    /**
     * @return the {@link ConnectionData} to use when connecting to the endpoint or {@code null} if this is a local
     *         endpoint
     */
    public ConnectionData getConnectionData() {
        return connectionData;
    }

    /**
     * @return the name of this {@link MonitoredEndpoint}
     */
    public String getName() {
        return name;
    }

    /**
     * @return the {@link SSLContext} associated with this endpoint
     */
    public SSLContext getSSLContext() {
        return sslContext;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * @return {@code true} if this is a local endpoint - i.e. the connection does not need a hostname, username,
     *         password, etc.; a shorthand for {@code connectionData == null}
     */
    public boolean isLocal() {
        return connectionData == null;
    }

    @Override
    public String toString() {
        return String.format("Endpoint[%s]:[%s]", getName(), getConnectionData());
    }
}
