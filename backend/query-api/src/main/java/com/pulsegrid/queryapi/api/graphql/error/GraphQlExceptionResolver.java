package com.pulsegrid.queryapi.api.graphql.error;

import com.pulsegrid.queryapi.application.error.DownstreamAccessDeniedException;
import com.pulsegrid.queryapi.application.error.DownstreamServiceUnavailableException;
import com.pulsegrid.queryapi.application.error.InvalidQueryArgumentException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GraphQlExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof DownstreamServiceUnavailableException unavailable) {
            return error(env, ErrorType.INTERNAL_ERROR, unavailable.getMessage(),
                    Map.of("code", "DOWNSTREAM_UNAVAILABLE", "service", unavailable.getService()));
        }
        if (ex instanceof DownstreamAccessDeniedException denied) {
            return error(env, ErrorType.FORBIDDEN, denied.getMessage(),
                    Map.of("code", "ACCESS_DENIED", "service", denied.getService()));
        }
        if (ex instanceof InvalidQueryArgumentException invalid) {
            return error(env, ErrorType.BAD_REQUEST, invalid.getMessage(), Map.of("code", "INVALID_ARGUMENT"));
        }
        return null;
    }

    private GraphQLError error(DataFetchingEnvironment env, ErrorType type, String message, Map<String, Object> extensions) {
        return GraphqlErrorBuilder.newError(env)
                .errorType(type)
                .message(message)
                .extensions(extensions)
                .build();
    }
}
