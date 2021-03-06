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

package org.jboss.provisioning.type.builtin;

import org.jboss.provisioning.ProvisioningException;
import org.jboss.provisioning.type.FeatureParameterType;
import org.jboss.provisioning.type.ParameterTypeConversionException;
import org.jboss.provisioning.util.formatparser.FormatParser;
import org.jboss.provisioning.util.formatparser.FormatParsingException;
import org.jboss.provisioning.util.formatparser.ParsingFormat;

/**
 *
 * @author Alexey Loubyansky
 */
public class FormattedParameterType implements FeatureParameterType {

    protected final String name;
    protected final ParsingFormat format;

    protected FormattedParameterType(String name, ParsingFormat format) {
        this.name = name;
        this.format = format;
    }

    /* (non-Javadoc)
     * @see org.jboss.provisioning.type.FeatureParameterType#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public Object fromString(String str) throws ParameterTypeConversionException {
        if(str == null) {
            return null;
        }
        try {
            return FormatParser.parse(format, str);
        } catch (FormatParsingException e) {
            throw new ParameterTypeConversionException("Failed to parse " + getName() + " parameter value", e);
        }
    }

    @Override
    public String toString(Object o) throws ParameterTypeConversionException {
        return o == null ? null : o.toString();
    }

    @Override
    public boolean isSupportsMerging() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.jboss.provisioning.type.FeatureParameterType#merge(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object merge(Object original, Object other) throws ProvisioningException {
        return other;
    }
}
