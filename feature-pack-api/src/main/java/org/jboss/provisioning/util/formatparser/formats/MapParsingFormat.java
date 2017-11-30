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

import org.jboss.provisioning.util.formatparser.FormatErrors;
import org.jboss.provisioning.util.formatparser.FormatParsingException;
import org.jboss.provisioning.util.formatparser.ParsingContext;
import org.jboss.provisioning.util.formatparser.ParsingFormatBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class MapParsingFormat extends ParsingFormatBase {

    public static final String NAME = "Map";

    public static MapParsingFormat getInstance() {
        return new MapParsingFormat();
    }

    protected KeyValueParsingFormat entryFormat;

    protected MapParsingFormat() {
        this(NAME, KeyValueParsingFormat.getInstance());
    }

    protected MapParsingFormat(String name) {
        this(name, KeyValueParsingFormat.getInstance());
    }

    protected MapParsingFormat(String name, KeyValueParsingFormat entryFormat) {
        super(name, NAME);
        this.entryFormat = entryFormat;
    }

    public boolean isAcceptsKey(Object key) {
        return true;
    }

    public MapParsingFormat setEntryFormat(KeyValueParsingFormat entryFormat) {
        this.entryFormat = entryFormat;
        return this;
    }

    @Override
    public void react(ParsingContext ctx) throws FormatParsingException {
        switch(ctx.charNow()) {
            case ',' :
                ctx.popFormats();
                break;
            case '}':
                ctx.end();
                break;
            default:
                ctx.bounce();
        }
    }

    @Override
    public void deal(ParsingContext ctx) throws FormatParsingException {
        if(Character.isWhitespace(ctx.charNow())) {
            return;
        }
        ctx.pushFormat(entryFormat);
    }

    @Override
    public void eol(ParsingContext ctx) throws FormatParsingException {
        throw new FormatParsingException(FormatErrors.formatIncomplete(this));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((entryFormat == null) ? 0 : entryFormat.hashCode());
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
        MapParsingFormat other = (MapParsingFormat) obj;
        if (entryFormat == null) {
            if (other.entryFormat != null)
                return false;
        } else if (!entryFormat.equals(other.entryFormat))
            return false;
        return true;
    }
}