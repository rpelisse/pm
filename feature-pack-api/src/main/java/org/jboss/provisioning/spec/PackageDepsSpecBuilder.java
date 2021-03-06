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
package org.jboss.provisioning.spec;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.provisioning.util.PmCollections;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class PackageDepsSpecBuilder<T extends PackageDepsSpecBuilder<T>> {

    protected Map<String, PackageDependencySpec> localPkgDeps = Collections.emptyMap();
    protected Map<String, Map<String, PackageDependencySpec>> externalPkgDeps = Collections.emptyMap();

    public T addPackageDep(String packageName) {
        return addPackageDep(packageName, false);
    }

    public T addPackageDep(String packageName, boolean optional) {
        return addPackageDep(PackageDependencySpec.forPackage(packageName, optional));
    }

    @SuppressWarnings("unchecked")
    public T addPackageDep(PackageDependencySpec dep) {
        localPkgDeps = PmCollections.putLinked(localPkgDeps, dep.getName(), dep);
        return (T) this;
    }

    public T addPackageDep(String fpDep, String packageName) {
        return addPackageDep(fpDep, packageName, false);
    }

    public T addPackageDep(String fpDep, String packageName, boolean optional) {
        return addPackageDep(fpDep, PackageDependencySpec.forPackage(packageName, optional));
    }

    @SuppressWarnings("unchecked")
    public T addPackageDep(String fpDep, PackageDependencySpec dep) {
        if(fpDep == null) {
            return addPackageDep(dep);
        }
        Map<String, PackageDependencySpec> deps = externalPkgDeps.get(fpDep);
        if(deps == null) {
            externalPkgDeps = PmCollections.put(externalPkgDeps, fpDep, Collections.singletonMap(dep.getName(), dep));
            return (T) this;
        }
        if(deps.size() == 1) {
            if(deps.containsKey(dep.getName())) {
                deps = externalPkgDeps.put(fpDep, Collections.singletonMap(dep.getName(), dep));
            } else {
                final Map.Entry<String, PackageDependencySpec> first = deps.entrySet().iterator().next();
                deps = new HashMap<>(2);
                deps.put(first.getKey(), first.getValue());
                deps.put(dep.getName(), dep);
            }
            if(externalPkgDeps.size() == 1) {
                externalPkgDeps = Collections.singletonMap(fpDep, deps);
            } else {
                externalPkgDeps.put(fpDep, deps);
            }
            return (T) this;
        }
        deps.put(dep.getName(), dep);
        return (T) this;
    }

    public boolean hasPackageDeps() {
        return localPkgDeps != null || !externalPkgDeps.isEmpty();
    }
}