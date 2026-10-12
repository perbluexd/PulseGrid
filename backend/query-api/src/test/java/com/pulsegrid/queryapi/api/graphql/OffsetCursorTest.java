package com.pulsegrid.queryapi.api.graphql;

import com.pulsegrid.queryapi.api.graphql.pagination.OffsetCursor;
import com.pulsegrid.queryapi.application.error.InvalidQueryArgumentException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OffsetCursorTest {

    @Test
    void shouldRoundTripTheOffset() {
        assertThat(OffsetCursor.decode(OffsetCursor.encode(42))).isEqualTo(42);
    }

    @Test
    void shouldRejectTamperedCursors() {
        assertThatThrownBy(() -> OffsetCursor.decode("no-es-un-cursor"))
                .isInstanceOf(InvalidQueryArgumentException.class);
    }
}
