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
package org.jboss.provisioning.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.jboss.provisioning.ArtifactCoords;
import org.jboss.provisioning.ArtifactException;
import org.jboss.provisioning.ArtifactRepositoryManager;
import org.jboss.provisioning.plugin.FpMavenErrors;

/**
 *
 * @author Alexey Loubyansky
 */
class MavenArtifactRepositoryManager implements ArtifactRepositoryManager {

    private static final ArtifactRepositoryManager INSTANCE = new MavenArtifactRepositoryManager();

    static ArtifactRepositoryManager getInstance() {
        return INSTANCE;
    }

    private final RepositorySystem repoSystem;
    private final RepositorySystemSession session;

    private MavenArtifactRepositoryManager() {
        repoSystem = Util.newRepositorySystem();
        session = Util.newRepositorySession(repoSystem);
    }

    @Override
    public Path resolve(ArtifactCoords coords) throws ArtifactException {
        final ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(new DefaultArtifact(coords.getGroupId(), coords.getArtifactId(), coords.getClassifier(),
                coords.getExtension(), coords.getVersion()));
        final ArtifactResult result;
        try {
            result = repoSystem.resolveArtifact(session, request);
        } catch (org.eclipse.aether.resolution.ArtifactResolutionException e) {
            throw new ArtifactException(FpMavenErrors.artifactResolution(coords), e);
        }
        if (!result.isResolved()) {
            throw new ArtifactException(FpMavenErrors.artifactResolution(coords));
        }
        if (result.isMissing()) {
            throw new ArtifactException(FpMavenErrors.artifactMissing(coords));
        }
        return Paths.get(result.getArtifact().getFile().toURI());
    }

    @Override
    public void install(ArtifactCoords coords, Path file) throws ArtifactException {
        final InstallRequest request = new InstallRequest();
        request.addArtifact(new DefaultArtifact(coords.getGroupId(), coords.getArtifactId(), coords.getClassifier(),
                coords.getExtension(), coords.getVersion(), Collections.emptyMap(), file.toFile()));
        try {
            repoSystem.install(session, request);
        } catch (InstallationException ex) {
            Logger.getLogger(MavenArtifactRepositoryManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void deploy(ArtifactCoords coords, Path file) throws ArtifactException {
        final DeployRequest request = new DeployRequest();
        request.addArtifact(new DefaultArtifact(coords.getGroupId(), coords.getArtifactId(), coords.getClassifier(),
                coords.getExtension(), coords.getVersion(), Collections.emptyMap(), file.toFile()));
        try {
            repoSystem.deploy(session, request);
        } catch (DeploymentException ex) {
            Logger.getLogger(MavenArtifactRepositoryManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
