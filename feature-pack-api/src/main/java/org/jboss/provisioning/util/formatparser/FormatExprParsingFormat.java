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

package org.jboss.provisioning.util.formatparser;

import org.jboss.provisioning.util.formatparser.formats.CompositeParsingFormat;

/**
 *
 * @author Alexey Loubyansky
 */
public class FormatExprParsingFormat extends ParsingFormatBase {

    public static class TypeParameterParsingFormat extends ParsingFormatBase {

        private final char start;
        private final char end;

        protected TypeParameterParsingFormat(String name, char start, char end) {
            super(name);
            this.start = start;
            this.end = end;
        }

        @Override
        public boolean isWrapper() {
            return true;
        }

        @Override
        public String getContentType() {
            return "?";
        }

        @Override
        public void pushed(ParsingContext ctx) throws FormatParsingException {
            if(ctx.charNow() != start) {
                throw new FormatParsingException(FormatErrors.unexpectedStartingCharacter(this, '<', ctx.charNow()));
            }
        }

        @Override
        public void react(ParsingContext ctx) throws FormatParsingException {
            if(ctx.charNow() == end) {
                ctx.end();
            } else {
                ctx.bounce();
            }
        }

        @Override
        public void deal(ParsingContext ctx) throws FormatParsingException {
            if(Character.isWhitespace(ctx.charNow())) {
                return;
            }
            ctx.pushFormat(INSTANCE);
        }

        @Override
        public void eol(ParsingContext ctx) throws FormatParsingException {
            throw new FormatParsingException(FormatErrors.formatIncomplete(this));
        }
    }

    public static final String NAME = "FormatExpr";

    private static final FormatExprParsingFormat INSTANCE = new FormatExprParsingFormat();

    static final String TYPE_PARAM_FORMAT_NAME = "TypeParam";
    static final String LIST_TYPE_FORMAT_NAME = "ListTypeExpr";
    static final String COMPOSITE_TYPE_FORMAT_NAME = "CompositeTypeExpr";

    private static final ParsingFormat TYPE_PARAM_FORMAT = new TypeParameterParsingFormat(TYPE_PARAM_FORMAT_NAME, '<', '>');
    private static final ParsingFormat LIST_TYPE_FORMAT = new TypeParameterParsingFormat(LIST_TYPE_FORMAT_NAME, '[', ']');

    private static final CompositeParsingFormat COMPOSITE_TYPE_FORMAT = CompositeParsingFormat.newInstance(COMPOSITE_TYPE_FORMAT_NAME)
            .setAcceptAll(true)
            .setNameValueSeparator(':')
            .setDefaultValueFormat(INSTANCE);

    public static FormatExprParsingFormat getInstance() {
        return INSTANCE;
    }

    protected FormatExprParsingFormat() {
        super(NAME);
    }

    @Override
    public void pushed(ParsingContext ctx) throws FormatParsingException {
        deal(ctx);
    }

    @Override
    public void deal(ParsingContext ctx) throws FormatParsingException {
        if(Character.isWhitespace(ctx.charNow())) {
            return;
        }
        switch(ctx.charNow()) {
            case '<':
                ctx.pushFormat(TYPE_PARAM_FORMAT);
                break;
            case '{':
                ctx.pushFormat(COMPOSITE_TYPE_FORMAT);
                break;
            case '[':
                ctx.pushFormat(LIST_TYPE_FORMAT);
                break;
            default:
                ctx.content();
        }
    }
}
