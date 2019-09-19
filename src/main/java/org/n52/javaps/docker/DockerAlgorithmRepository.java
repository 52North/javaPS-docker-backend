/*
 * Copyright 2019 52°North Initiative for Geospatial Open Source
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

import com.github.dockerjava.api.DockerClient;
import org.n52.javaps.algorithm.IAlgorithm;
import org.n52.javaps.description.TypedProcessDescription;
import org.n52.javaps.docker.process.DockerAlgorithm;
import org.n52.javaps.transactional.AbstractTransactionalAlgorithmRepository;
import org.n52.shetland.ogc.wps.ap.ApplicationPackage;
import org.n52.shetland.ogc.wps.ap.DockerExecutionUnit;
import org.n52.shetland.ogc.wps.description.ProcessDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;

/**
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 * @author <a href="mailto:c.autermann@52north.org">Christian Autermann</a>
 */
@Repository
public class DockerAlgorithmRepository extends AbstractTransactionalAlgorithmRepository {
    private static final Logger LOG = LoggerFactory.getLogger(DockerAlgorithmRepository.class);

    private final TypedDescriptionBuilder descriptionBuilder;
    private final DockerConfig dockerConfig;
    private final DockerClient dockerClient;

    @Autowired
    public DockerAlgorithmRepository(DockerConfig dockerConfig,
                                     DockerClient dockerClient,
                                     TypedDescriptionBuilder descriptionBuilder) {
        this.descriptionBuilder = descriptionBuilder;
        this.dockerConfig = dockerConfig;
        this.dockerClient = dockerClient;
    }

    @Override
    public boolean isSupported(ApplicationPackage applicationPackage) {
        return applicationPackage.getExecutionUnit() instanceof DockerExecutionUnit;
    }

    @Override
    public void destroy() {
        if (dockerClient != null) {
            try {
                dockerClient.close();
            } catch (IOException e) {
                LOG.error("error closing docker client", e);
            }
        }
    }

    @Override
    protected TypedProcessDescription createProcessDescription(ApplicationPackage applicationPackage) {
        ProcessDescription description = applicationPackage.getProcessDescription().getProcessDescription();
        return descriptionBuilder.createDescription(description);
    }

    @Override
    protected IAlgorithm createAlgorithm(ApplicationPackage applicationPackage) {
        return new DockerAlgorithm(dockerConfig, dockerClient, descriptionBuilder, applicationPackage);

    }
}
