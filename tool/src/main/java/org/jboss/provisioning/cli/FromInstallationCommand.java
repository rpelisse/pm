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
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.FileOptionCompleter;
import org.jboss.aesh.terminal.Shell;
import org.jboss.provisioning.DefaultMessageWriter;
import org.jboss.provisioning.ProvisioningManager;

/**
 *
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 */
public abstract class FromInstallationCommand extends PmSessionCommand {

    @Option(name = "src", completer = FileOptionCompleter.class, required = true,
            description = "Customized source installation directory.")
    protected String srcDirArg;

    @Option(name = "verbose", shortName = 'v', hasValue = false,
            description = "Whether or not the output should be verbose")
    boolean verbose;

    protected Path getTargetDir(PmSession session) {
        return srcDirArg == null ? session.getWorkDir() : session.getWorkDir().resolve(srcDirArg);
    }

    protected ProvisioningManager getManager(PmSession session) {
        final Shell shell = session.getShell();
        return ProvisioningManager.builder()
                .setArtifactResolver(MavenArtifactRepositoryManager.getInstance())
                .setInstallationHome(getTargetDir(session))
                .setMessageWriter(new DefaultMessageWriter(shell.out(), shell.out(), verbose))
                .build();
    }
}
