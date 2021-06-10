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

import org.n52.javaps.docker.process.DockerAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class ByPrefixEnvironmentConfigurer implements DockerEnvironmentConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(DockerAlgorithm.class);
    private String prefix = "DOCKER_ENV_";

    @Value("${docker.envPrefix}")
    public void setPrefix(String pf) {
        if (pf != null && pf.length() > 0) {
            this.prefix = pf;
        }
    }


    @Override
    public void configure(Environment environment) {
        LOG.info("Setting environment variables with prefix: {}", this.prefix);
        Map<String, String> env = System.getenv();
        env.keySet().stream().filter(k -> k.startsWith(this.prefix)).forEach(k -> {
            String v = env.get(k);
            environment.put(k.substring(k.indexOf(this.prefix) + this.prefix.length()),
                    env.get(k));
        });
    }
}
