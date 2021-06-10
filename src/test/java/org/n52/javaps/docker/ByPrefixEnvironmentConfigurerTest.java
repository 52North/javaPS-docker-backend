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

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Optional;


public class ByPrefixEnvironmentConfigurerTest {

    @Test
    public void testEnvVarPrefixing() {
        ByPrefixEnvironmentConfigurer conf = new ByPrefixEnvironmentConfigurer();

        Map<String, String> sysEnv = System.getenv();
        Optional<String> pfOpt = sysEnv.keySet().stream().sorted((s1, s2) -> s2.length() - s1.length()).findFirst();
        String pf = pfOpt.get().substring(0, 4);
        String postFix = pfOpt.get().substring(4);

        Environment env = new Environment();
        conf.setPrefix(pf);
        conf.configure(env);

        Assert.assertThat(env.get(postFix), CoreMatchers.equalTo(sysEnv.get(pfOpt.get())));
    }

}
