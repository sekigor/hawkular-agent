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
package org.hawkular.agent.monitor.extension;

import static org.jboss.as.controller.PersistentResourceXMLDescription.builder;

import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PersistentResourceXMLDescription;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

public class SubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>,
        XMLElementWriter<SubsystemMarshallingContext> {

    public static final SubsystemParser INSTANCE = new SubsystemParser();

    private static final PersistentResourceXMLDescription xmlDescription;

    static {
        try {
            xmlDescription =
                    builder(SubsystemDefinition.INSTANCE)
                            .addAttributes(SubsystemDefinition.ATTRIBUTES)
                            .addChild(builder(StorageDefinition.INSTANCE)
                                    .setXmlElementName(StorageDefinition.STORAGE_ADAPTER)
                                    .addAttributes(StorageDefinition.ATTRIBUTES)
                            )
                            .addChild(builder(DiagnosticsDefinition.INSTANCE)
                                    .setXmlElementName(DiagnosticsDefinition.DIAGNOSTICS)
                                    .addAttributes(DiagnosticsDefinition.ATTRIBUTES)
                            )
                            .addChild(builder(MetricSetDefinition.INSTANCE)
                                    .setXmlElementName(MetricSetDefinition.METRIC_SET)
                                    .addAttributes(MetricSetDefinition.ATTRIBUTES)
                                    .addChild(builder(MetricDefinition.INSTANCE)
                                            .setXmlElementName(MetricDefinition.METRIC)
                                            .addAttributes(MetricDefinition.ATTRIBUTES)
                                    )
                            )
                            .build();
        } catch (Throwable t) {
            System.err.println("CANNOT INITIALIZE PARSER: " + SubsystemParser.class);
            throw t;
        }
    }

    private SubsystemParser() {
    }

    @Override
    public void writeContent(XMLExtendedStreamWriter writer, SubsystemMarshallingContext context)
            throws XMLStreamException {

        ModelNode model = new ModelNode();
        //this is bit of workaround for SPRD to work properly (mazz: what does that mean???)
        model.get(SubsystemDefinition.INSTANCE.getPathElement().getKeyValuePair()).set(context.getModelNode());
        xmlDescription.persist(writer, model, SubsystemExtension.NAMESPACE);
    }

    @Override
    public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
        xmlDescription.parse(reader, PathAddress.EMPTY_ADDRESS, list);
    }
}