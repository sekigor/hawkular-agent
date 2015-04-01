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
package org.hawkular.agent.monitor.scheduler.polling;

import org.hawkular.agent.monitor.scheduler.config.Interval;
import org.hawkular.dmrclient.Address;

/**
 * Represents a monitoring task - essentially identifies a metric that is to be collected
 * from a resource with an absolute address within a domain.
 */
public class Task {

    private final String host;
    private final String server;
    private final Address address;
    private final String attribute;
    private final String subref;
    private final Interval interval;

    public Task(
            String host, String server,
            Address address,
            String attribute,
            String subref,
            Interval interval) {

        this.host = host;
        this.server = server;
        this.address = address;
        this.attribute = attribute;
        this.subref = subref;
        this.interval = interval;
    }

    public Address getAddress() {
        return address;
    }

    public String getAttribute() {
        return attribute;
    }

    public Interval getInterval() {
        return interval;
    }

    public String getSubref() {
        return subref;
    }

    public String getHost() {
        return host;
    }

    public String getServer() {
        return server;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("MonitorTask: ");
        str.append("address=[").append(address).append("]");
        str.append(", attribute=[").append(attribute).append("]");
        str.append(", subref=[").append(subref).append("]");
        str.append(", host=[").append(host).append("]");
        str.append(", server=[").append(server).append("]");
        str.append(", interval=[").append(interval).append("]");
        return str.toString();
    }
}