package com.pulsegrid.queryapi.api.graphql.scalar;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.function.Function;

public final class DateTimeScalar {

    public static final GraphQLScalarType INSTANCE = GraphQLScalarType.newScalar()
            .name("DateTime")
            .description("Instante en UTC con formato ISO-8601, por ejemplo 2026-10-08T20:30:00Z")
            .coercing(new InstantCoercing())
            .build();

    private DateTimeScalar() {
    }

    private static final class InstantCoercing implements Coercing<Instant, String> {

        @Override
        public String serialize(@NonNull Object dataFetcherResult, @NonNull GraphQLContext context,
                                @NonNull Locale locale) {
            if (dataFetcherResult instanceof Instant instant) {
                return instant.toString();
            }
            throw new CoercingSerializeException("Se esperaba un Instant, llegó " + dataFetcherResult.getClass().getSimpleName());
        }

        @Override
        public Instant parseValue(@NonNull Object input, @NonNull GraphQLContext context, @NonNull Locale locale) {
            if (input instanceof String text) {
                return parse(text, CoercingParseValueException::new);
            }
            throw new CoercingParseValueException("DateTime debe ser un texto ISO-8601");
        }

        @Override
        public Instant parseLiteral(@NonNull Value<?> input, @NonNull CoercedVariables variables,
                                    @NonNull GraphQLContext context, @NonNull Locale locale) {
            if (input instanceof StringValue stringValue) {
                return parse(stringValue.getValue(), CoercingParseLiteralException::new);
            }
            throw new CoercingParseLiteralException("DateTime debe ser un texto ISO-8601");
        }

        @Override
        @NonNull
        public Value<?> valueToLiteral(@NonNull Object input, @NonNull GraphQLContext context, @NonNull Locale locale) {
            return StringValue.newStringValue(serialize(input, context, locale)).build();
        }

        private <E extends RuntimeException> Instant parse(String text, Function<String, E> error) {
            try {
                return OffsetDateTime.parse(text).toInstant();
            } catch (DateTimeParseException ex) {
                throw error.apply("DateTime inválido: '" + text + "' (formato esperado: 2026-10-08T20:30:00Z)");
            }
        }
    }
}
