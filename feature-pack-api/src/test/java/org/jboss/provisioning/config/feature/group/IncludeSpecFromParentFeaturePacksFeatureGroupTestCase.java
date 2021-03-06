/*
 * Copyright 2016-2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.provisioning.config.feature.group;

import org.jboss.provisioning.ArtifactCoords;
import org.jboss.provisioning.ArtifactCoords.Gav;
import org.jboss.provisioning.ProvisioningDescriptionException;
import org.jboss.provisioning.ProvisioningException;
import org.jboss.provisioning.config.FeatureConfig;
import org.jboss.provisioning.config.FeatureGroupConfig;
import org.jboss.provisioning.config.FeaturePackConfig;
import org.jboss.provisioning.repomanager.FeaturePackRepositoryManager;
import org.jboss.provisioning.runtime.ResolvedFeatureId;
import org.jboss.provisioning.spec.ConfigSpec;
import org.jboss.provisioning.spec.FeatureGroupSpec;
import org.jboss.provisioning.spec.FeatureParameterSpec;
import org.jboss.provisioning.spec.FeatureSpec;
import org.jboss.provisioning.state.ProvisionedFeaturePack;
import org.jboss.provisioning.state.ProvisionedState;
import org.jboss.provisioning.test.PmInstallFeaturePackTestBase;
import org.jboss.provisioning.xml.ProvisionedConfigBuilder;
import org.jboss.provisioning.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class IncludeSpecFromParentFeaturePacksFeatureGroupTestCase extends PmInstallFeaturePackTestBase {

    private static final Gav FP1_GAV = ArtifactCoords.newGav("org.jboss.pm.test", "fp1", "1.0.0.Final");
    private static final Gav FP2_GAV = ArtifactCoords.newGav("org.jboss.pm.test", "fp2", "1.0.0.Final");

    @Override
    protected void setupRepo(FeaturePackRepositoryManager repoManager) throws ProvisioningDescriptionException {
        repoManager.installer()
        .newFeaturePack(FP1_GAV)
            .addSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("a", true))
                    .build())
            .addSpec(FeatureSpec.builder("specB")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("b", false))
                    .build())
            .addFeatureGroup(FeatureGroupSpec.builder("fg1")
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("name", "aOne")
                            .setParam("a", "a1"))
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("name", "aTwo")
                            .setParam("a", "a2"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("name", "bOne")
                            .setParam("b", "b1"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("name", "bTwo")
                            .setParam("b", "b2"))
                    .build())
            .getInstaller()
            .newFeaturePack(FP2_GAV)
                .addDependency("fp1", FP1_GAV)
                .addSpec(FeatureSpec.builder("specC")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("c", true))
                    .build())
                .addSpec(FeatureSpec.builder("specD")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("d", false))
                    .build())
                .addFeatureGroup(FeatureGroupSpec.builder("fg2")
                    .addFeatureGroup(FeatureGroupConfig.forGroup("fp1", "fg1"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("name", "cOne")
                            .setParam("c", "c1"))
                    .addFeature(
                            new FeatureConfig("specD")
                            .setParam("name", "dOne")
                            .setParam("d", "d1"))
                    .build())
                .addConfig(ConfigSpec.builder()
                        .setName("config1")
                        .addFeatureGroup(FeatureGroupConfig.builder("fg2", false)
                                .includeSpec("fp1", "specA")
                                .includeSpec("specC")
                                .includeSpec("specD")
                                .build())
                        .build())
            .getInstaller()
        .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forGav(FP2_GAV);
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.forGav(FP2_GAV))
                .addConfig(ProvisionedConfigBuilder.builder().setName("config1")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV, "specA", "name", "aOne"))
                                .setConfigParam("a", "a1")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV, "specA", "name", "aTwo"))
                                .setConfigParam("a", "a2")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP2_GAV, "specC", "name", "cOne"))
                                .setConfigParam("c", "c1")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP2_GAV, "specD", "name", "dOne"))
                                .setConfigParam("d", "d1")
                                .build())
                        .build())
                .build();
    }
}
