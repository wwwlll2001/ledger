package com.assignment.ledger.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;


/**
 * Active the elasticsearch client connection configuration, if not specified in the configuration, the config
 * spring.data.elasticsearch.client.reactive.endpoints in the config file will not work.
 */
@Configuration
public class ElasticsearchClientConfig extends ElasticsearchConfiguration {

    @Value("${spring.data.elasticsearch.client.reactive.endpoints}")
    private String reactiveEndpoints;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(reactiveEndpoints)
                .build();
    }
}
