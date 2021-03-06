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
import org.jboss.provisioning.runtime.ResolvedFeatureId;
import org.jboss.provisioning.spec.ConfigSpec;
import org.jboss.provisioning.spec.FeatureGroupSpec;
import org.jboss.provisioning.spec.FeatureId;
import org.jboss.provisioning.spec.FeatureParameterSpec;
import org.jboss.provisioning.spec.FeatureSpec;
import org.jboss.provisioning.state.ProvisionedFeaturePack;
import org.jboss.provisioning.state.ProvisionedState;
import org.jboss.provisioning.test.PmInstallFeaturePackTestBase;
import org.jboss.provisioning.repomanager.FeaturePackRepositoryManager;
import org.jboss.provisioning.xml.ProvisionedConfigBuilder;
import org.jboss.provisioning.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class MergingSubsetsOfSameFeatureGroupTestCase extends PmInstallFeaturePackTestBase {

    private static final Gav FP_GAV = ArtifactCoords.newGav("org.jboss.pm.test", "fp1", "1.0.0.Final");

    @Override
    protected void setupRepo(FeaturePackRepositoryManager repoManager) throws ProvisioningDescriptionException {
        repoManager.installer()
        .newFeaturePack(FP_GAV)
            .addSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("a", true))
                    .build())
            .addSpec(FeatureSpec.builder("specB")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("p1", true))
                    .addParam(FeatureParameterSpec.create("p2", true))
                    .addParam(FeatureParameterSpec.create("p3", true))
                    .addParam(FeatureParameterSpec.create("p4", "spec"))
                    .build())
            .addSpec(FeatureSpec.builder("specC")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("p1", true))
                    .addParam(FeatureParameterSpec.create("p2", true))
                    .addParam(FeatureParameterSpec.create("p3", true))
                    .addParam(FeatureParameterSpec.create("p4", true))
                    .addParam(FeatureParameterSpec.create("p5", "spec"))
                    .build())
            .addFeatureGroup(FeatureGroupSpec.builder("fg1")
                    .addFeatureGroup(FeatureGroupConfig.forGroup("fg2"))
                    .addFeatureGroup(FeatureGroupConfig.builder("fg3")
                            .setInheritFeatures(true)
                            .excludeSpec("specA")
                            .excludeSpec("specC")
                            .includeFeature(FeatureId.create("specB", "name", "fg3b1"),
                                    new FeatureConfig("specB").setParam("name", "fg3b1").setParam("p1", "fg1")
                            )
                            .includeFeature(FeatureId.create("specC", "name", "fg3c1"),
                                    new FeatureConfig("specC").setParam("name", "fg3c1").setParam("p1", "fg1").setParam("p2", "fg1")
                            )
                            .includeFeature(FeatureId.create("specC", "name", "fg3c3"))
                            .build())
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("name", "fg1a1")
                            .setParam("a", "a1"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("name", "fg1b1"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("name", "fg3b2")
                            .setParam("p2", "fg1"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("name", "fg1c1"))
                    .build())
            .addFeatureGroup(FeatureGroupSpec.builder("fg2")
                    .addFeatureGroup(FeatureGroupConfig.builder("fg3")
                            .setInheritFeatures(false)
                            .includeSpec("specB")
                            .includeFeature(FeatureId.create("specB", "name", "fg3b1"),
                                    new FeatureConfig("specB").setParam("name", "fg3b1").setParam("p2", "fg2").setParam("p1", "fg2")
                            )
                            .includeSpec("specC")
                            .includeFeature(FeatureId.create("specC", "name", "fg3c1"),
                                    new FeatureConfig("specC").setParam("name", "fg3c1")
                                    .setParam("p3", "fg2")
                                    .setParam("p2", "fg2")
                                    .setParam("p1", "fg2")
                            )
                            .excludeFeature(FeatureId.create("specC", "name", "fg3c2"))
                            .excludeFeature(FeatureId.create("specC", "name", "fg3c3"))
                            .excludeFeature(FeatureId.create("specC", "name", "fg3c4"))
                            .build())
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("name", "fg2a1")
                            .setParam("a", "a2"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("name", "fg2b1"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("name", "fg2c1"))
                    .build())
            .addFeatureGroup(FeatureGroupSpec.builder("fg3")
                    .addFeatureGroup(FeatureGroupConfig.forGroup("fg1"))
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("name", "fg3a1")
                            .setParam("a", "a3"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("name", "fg3b1")
                            .setParam("p1", "fg3")
                            .setParam("p2", "fg3")
                            .setParam("p3", "fg3"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("name", "fg3b2")
                            .setParam("p1", "fg3")
                            .setParam("p2", "fg3"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("name", "fg3c1")
                            .setParam("p1", "fg3")
                            .setParam("p2", "fg3")
                            .setParam("p3", "fg3")
                            .setParam("p4", "fg3"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("name", "fg3c2")
                            .setParam("p5", "fg3"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("name", "fg3c3")
                            .setParam("p5", "fg3"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("name", "fg3c4")
                            .setParam("p5", "fg3"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("name", "fg3c5")
                            .setParam("p5", "fg3"))
                    .build())
            .addConfig(ConfigSpec.builder()
                    .setProperty("prop1", "value1")
                    .setProperty("prop2", "value2")
                    .addFeatureGroup(FeatureGroupConfig.forGroup("fg1"))
                    .addFeature(new FeatureConfig("specC").setParam("name", "fg3c1").setParam("p1", "config"))
                    .build())
//            .newPackage("p1", true)
//                .getFeaturePack()
            .getInstaller()
        .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forGav(FP_GAV);
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP_GAV)
//                        .addPackage("p1")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setProperty("prop1", "value1")
                        .setProperty("prop2", "value2")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specB", "name", "fg3b1"))
                                .setConfigParam("p1", "fg1")
                                .setConfigParam("p2", "fg3")
                                .setConfigParam("p3", "fg3")
                                .setConfigParam("p4", "spec")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specB", "name", "fg3b2"))
                                .setConfigParam("p1", "fg3")
                                .setConfigParam("p2", "fg1")
                                .setConfigParam("p4", "spec")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specB", "name", "fg2b1"))
                                .setConfigParam("p4", "spec")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specB", "name", "fg1b1"))
                                .setConfigParam("p4", "spec")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specC", "name", "fg3c1"))
                                .setConfigParam("p1", "config")
                                .setConfigParam("p2", "fg1")
                                .setConfigParam("p3", "fg3")
                                .setConfigParam("p4", "fg3")
                                .setConfigParam("p5", "spec")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specC", "name", "fg3c5"))
                                .setConfigParam("p5", "fg3")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specC", "name", "fg2c1"))
                                .setConfigParam("p5", "spec")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specC", "name", "fg3c3"))
                                .setConfigParam("p5", "fg3")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specC", "name", "fg1c1"))
                                .setConfigParam("p5", "spec")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specA", "name", "fg2a1"))
                                .setConfigParam("a", "a2")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specA", "name", "fg1a1"))
                                .setConfigParam("a", "a1")
                                .build())
                        .build())
                .build();
    }
}
