/*
 * Copyright 2019-2021 52°North Initiative for Geospatial Open Source
 * Software GmbH
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
package org.n52.javaps.docker;

import org.n52.javaps.description.TypedBoundingBoxInputDescription;
import org.n52.javaps.description.TypedBoundingBoxOutputDescription;
import org.n52.javaps.description.TypedComplexInputDescription;
import org.n52.javaps.description.TypedComplexOutputDescription;
import org.n52.javaps.description.TypedGroupInputDescription;
import org.n52.javaps.description.TypedGroupOutputDescription;
import org.n52.javaps.description.TypedLiteralInputDescription;
import org.n52.javaps.description.TypedLiteralOutputDescription;
import org.n52.javaps.description.TypedProcessDescription;
import org.n52.javaps.description.impl.TypedProcessDescriptionFactory;
import org.n52.javaps.docker.io.DockerInputData;
import org.n52.javaps.docker.io.DockerOutputData;
import org.n52.javaps.io.literal.LiteralType;
import org.n52.javaps.io.literal.LiteralTypeRepository;
import org.n52.javaps.io.literal.xsd.LiteralStringType;
import org.n52.shetland.ogc.wps.description.BoundingBoxInputDescription;
import org.n52.shetland.ogc.wps.description.BoundingBoxOutputDescription;
import org.n52.shetland.ogc.wps.description.ComplexInputDescription;
import org.n52.shetland.ogc.wps.description.ComplexOutputDescription;
import org.n52.shetland.ogc.wps.description.GroupInputDescription;
import org.n52.shetland.ogc.wps.description.GroupOutputDescription;
import org.n52.shetland.ogc.wps.description.LiteralInputDescription;
import org.n52.shetland.ogc.wps.description.LiteralOutputDescription;
import org.n52.shetland.ogc.wps.description.ProcessDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * {@link TypedDescriptionBuilder} that creates typed descriptions from untyped descriptions using {@link
 * DockerInputData} and {@link DockerOutputData}.
 *
 * @author Christian Autermann
 */
@Component
public class DockerTypedDescriptionBuilder implements TypedDescriptionBuilder {
    private final LiteralTypeRepository literalTypeRepository;
    private final TypedProcessDescriptionFactory descriptionFactory;

    /**
     * Creates a new {@link DockerTypedDescriptionBuilder}.
     *
     * @param literalTypeRepository The {@link LiteralTypeRepository}.
     * @param descriptionFactory    The {@link TypedProcessDescriptionFactory} to use.
     */
    @Autowired
    public DockerTypedDescriptionBuilder(LiteralTypeRepository literalTypeRepository,
                                         TypedProcessDescriptionFactory descriptionFactory) {
        this.literalTypeRepository = Objects.requireNonNull(literalTypeRepository);
        this.descriptionFactory = Objects.requireNonNull(descriptionFactory);
    }

    @Override
    public TypedProcessDescription createDescription(ProcessDescription processDescription) {
        return descriptionFactory.process()
                                 .withDescription(processDescription)
                                 .withVersion(processDescription.getVersion())
                                 .withInput(processDescription.getInputDescriptions().stream()
                                                              .map(this::createProcessInputDescription))
                                 .withOutput(processDescription.getOutputDescriptions().stream()
                                                               .map(this::createProcessOutputDescription))
                                 .build();
    }

    @Override
    public TypedLiteralInputDescription createLiteralInputDescription(LiteralInputDescription input) {
        LiteralType<?> literalType = literalTypeRepository.getLiteralType(input).orElseGet(LiteralStringType::new);
        return descriptionFactory.literalInput(input).withType(literalType).build();
    }

    @Override
    public TypedLiteralOutputDescription createLiteralOutputDescription(LiteralOutputDescription output) {
        LiteralType<?> literalType = literalTypeRepository.getLiteralType(output).orElseGet(LiteralStringType::new);
        return descriptionFactory.literalOutput(output).withType(literalType).build();
    }

    @Override
    public TypedGroupInputDescription createGroupInputDescription(GroupInputDescription input) {
        return descriptionFactory.groupInput().withDescription(input)
                                 .withInput(input.getInputDescriptions().stream()
                                                 .map(this::createProcessInputDescription))
                                 .build();
    }

    @Override
    public TypedGroupOutputDescription createGroupOutputDescription(GroupOutputDescription output) {
        return descriptionFactory.groupOutput().withDescription(output)
                                 .withOutput(output.getOutputDescriptions().stream()
                                                   .map(this::createProcessOutputDescription))
                                 .build();
    }

    @Override
    public TypedBoundingBoxInputDescription creatBoundingBoxInputDescription(BoundingBoxInputDescription input) {
        return descriptionFactory.boundingBoxInput(input).build();
    }

    @Override
    public TypedBoundingBoxOutputDescription creatBoundingBoxOutputDescription(BoundingBoxOutputDescription output) {
        return descriptionFactory.boundingBoxOutput(output).build();
    }

    @Override
    public TypedComplexInputDescription createComplexInputDescription(ComplexInputDescription input) {
        return descriptionFactory.complexInput(input).withType(DockerInputData.class).build();
    }

    @Override
    public TypedComplexOutputDescription createComplexOutputDescription(ComplexOutputDescription output) {
        return descriptionFactory.complexOutput(output).withType(DockerOutputData.class).build();
    }

}
