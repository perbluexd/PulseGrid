package com.pulsegrid.queryapi.api.graphql.pagination;

import com.pulsegrid.queryapi.application.error.InvalidQueryArgumentException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class OffsetCursor {

    private static final String PREFIX = "offset:";

    private OffsetCursor() {
    }

    public static String encode(int offset) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((PREFIX + offset).getBytes(StandardCharsets.UTF_8));
    }

    public static int decode(String cursor) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            if (!decoded.startsWith(PREFIX)) {
                throw new InvalidQueryArgumentException("Cursor inválido");
            }
            return Integer.parseInt(decoded.substring(PREFIX.length()));
        } catch (IllegalArgumentException ex) {
            throw new InvalidQueryArgumentException("Cursor inválido");
        }
    }
}
