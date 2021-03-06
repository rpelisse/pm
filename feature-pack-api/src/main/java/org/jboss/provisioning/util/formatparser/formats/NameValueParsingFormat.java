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

package org.jboss.provisioning.util.formatparser.formats;

import org.jboss.provisioning.util.formatparser.FormatParsingException;
import org.jboss.provisioning.util.formatparser.ParsingContext;
import org.jboss.provisioning.util.formatparser.ParsingFormat;
import org.jboss.provisioning.util.formatparser.ParsingFormatBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class NameValueParsingFormat extends ParsingFormatBase {

    public static final String NAME = "NameValue";

    public static final char SEPARATOR = '=';

    public static NameValueParsingFormat getInstance() {
        return new NameValueParsingFormat(SEPARATOR, WildcardParsingFormat.getInstance());
    }

    public static NameValueParsingFormat newInstance(char separator) {
        return new NameValueParsingFormat(separator, WildcardParsingFormat.getInstance());
    }

    public static NameValueParsingFormat newInstance(ParsingFormat valueFormat) {
        return new NameValueParsingFormat(SEPARATOR, valueFormat);
    }

    public static NameValueParsingFormat newInstance(char separator, ParsingFormat valueFormat) {
        return new NameValueParsingFormat(separator, valueFormat);
    }

    private final char separator;
    private final ParsingFormat valueFormat;

    protected NameValueParsingFormat(char separator, ParsingFormat valueFormat) {
        super(NAME);
        this.separator = separator;
        this.valueFormat = valueFormat;
    }

    public char getSeparator() {
        return separator;
    }

    public ParsingFormat getValueFormat() {
        return valueFormat;
    }

    @Override
    public void react(ParsingContext ctx) throws FormatParsingException {
        if(ctx.charNow() == separator) {
            ctx.popFormats();
        }
    }

    @Override
    public void pushed(ParsingContext ctx) throws FormatParsingException {
        ctx.pushFormat(StringParsingFormat.getInstance());
    }

    @Override
    public void deal(ParsingContext ctx) throws FormatParsingException {
        if(Character.isWhitespace(ctx.charNow())) {
            return;
        }
        ctx.pushFormat(valueFormat);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + separator;
        result = prime * result + ((valueFormat == null) ? 0 : valueFormat.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        NameValueParsingFormat other = (NameValueParsingFormat) obj;
        if (separator != other.separator)
            return false;
        if (valueFormat == null) {
            if (other.valueFormat != null)
                return false;
        } else if (!valueFormat.equals(other.valueFormat))
            return false;
        return true;
    }
}
