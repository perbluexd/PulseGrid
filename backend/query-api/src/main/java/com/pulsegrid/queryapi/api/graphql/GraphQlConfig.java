package com.pulsegrid.queryapi.api.graphql;

import com.pulsegrid.queryapi.api.graphql.scalar.DateTimeScalar;
import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class GraphQlConfig {

    @Bean
    public RuntimeWiringConfigurer scalarWiring() {
        return wiring -> wiring.scalar(DateTimeScalar.INSTANCE);
    }

    @Bean
    public MaxQueryDepthInstrumentation maxQueryDepthInstrumentation(@Value("${app.graphql.max-query-depth}") int maxDepth) {
        return new MaxQueryDepthInstrumentation(maxDepth);
    }

    @Bean
    public MaxQueryComplexityInstrumentation maxQueryComplexityInstrumentation(
            @Value("${app.graphql.max-query-complexity}") int maxComplexity) {
        return new MaxQueryComplexityInstrumentation(maxComplexity);
    }
}
