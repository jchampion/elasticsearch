/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.search.aggregations.pipeline.cumulativesum;

import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CumulativeSumParser implements PipelineAggregator.Parser {

    public static final ParseField FORMAT = new ParseField("format");
    public static final ParseField GAP_POLICY = new ParseField("gap_policy");
    public static final ParseField UNIT = new ParseField("unit");

    @Override
    public String type() {
        return CumulativeSumPipelineAggregator.TYPE.name();
    }

    @Override
    public CumulativeSumPipelineAggregatorBuilder parse(String pipelineAggregatorName,
            XContentParser parser, QueryParseContext context) throws IOException {
        XContentParser.Token token;
        String currentFieldName = null;
        String[] bucketsPaths = null;
        String format = null;

        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == XContentParser.Token.VALUE_STRING) {
                if (context.parseFieldMatcher().match(currentFieldName, FORMAT)) {
                    format = parser.text();
                } else if (context.parseFieldMatcher().match(currentFieldName, BUCKETS_PATH)) {
                    bucketsPaths = new String[] { parser.text() };
                } else {
                    throw new ParsingException(parser.getTokenLocation(),
                            "Unknown key for a " + token + " in [" + pipelineAggregatorName + "]: [" + currentFieldName + "].");
                }
            } else if (token == XContentParser.Token.START_ARRAY) {
                if (context.parseFieldMatcher().match(currentFieldName, BUCKETS_PATH)) {
                    List<String> paths = new ArrayList<>();
                    while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
                        String path = parser.text();
                        paths.add(path);
                    }
                    bucketsPaths = paths.toArray(new String[paths.size()]);
                } else {
                    throw new ParsingException(parser.getTokenLocation(),
                            "Unknown key for a " + token + " in [" + pipelineAggregatorName + "]: [" + currentFieldName + "].");
                }
            } else {
                throw new ParsingException(parser.getTokenLocation(),
                        "Unexpected token " + token + " in [" + pipelineAggregatorName + "].");
            }
        }

        if (bucketsPaths == null) {
            throw new ParsingException(parser.getTokenLocation(), "Missing required field [" + BUCKETS_PATH.getPreferredName()
                    + "] for derivative aggregation [" + pipelineAggregatorName + "]");
        }

        CumulativeSumPipelineAggregatorBuilder factory =
                new CumulativeSumPipelineAggregatorBuilder(pipelineAggregatorName, bucketsPaths[0]);
        if (format != null) {
            factory.format(format);
        }
        return factory;
    }

    @Override
    public CumulativeSumPipelineAggregatorBuilder getFactoryPrototype() {
        return CumulativeSumPipelineAggregatorBuilder.PROTOTYPE;
    }

}
