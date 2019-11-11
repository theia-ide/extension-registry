/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg;

import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurationContext;
import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurer;

/**
 * Configuration of Elasticsearch analysis.
 */
public class AnalysisConfig implements ElasticsearchAnalysisConfigurer {

	@Override
	public void configure(ElasticsearchAnalysisConfigurationContext context) {
        context.tokenFilter("substring")
                .type("ngram")
                .param("min_gram", 2)
                .param("max_gram", 3);
		context.analyzer("substring").custom()
                .tokenizer("standard")
                .tokenFilters("asciifolding", "lowercase", "substring");
    }

}