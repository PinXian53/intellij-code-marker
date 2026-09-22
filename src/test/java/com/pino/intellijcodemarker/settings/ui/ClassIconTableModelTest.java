package com.pino.intellijcodemarker.settings.ui;

import com.pino.intellijcodemarker.settings.CodeMarkerSettingsState;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ClassIconTableModelTest {

    private ClassIconTableModel model;

    @Before
    public void setUp() {
        model = new ClassIconTableModel();
        model.setMappings(List.of(
                rule("first"), rule("second"), rule("third")));
    }

    @Test
    public void exchangeRowsMovesARuleToTheNewPosition() {
        model.exchangeRows(0, 2);
        assertOrder("second", "third", "first");

        model.exchangeRows(2, 1);
        assertOrder("second", "first", "third");
    }

    @Test
    public void canExchangeRowsRejectsPositionsOutsideTheTable() {
        assertTrue(model.canExchangeRows(0, 2));
        assertFalse(model.canExchangeRows(0, 0));
        assertFalse(model.canExchangeRows(-1, 1));
        assertFalse(model.canExchangeRows(1, 3));
    }

    @Test
    public void exchangeRowsIgnoresInvalidPositions() {
        model.exchangeRows(0, 3);
        assertOrder("first", "second", "third");
    }

    private void assertOrder(String... classNames) {
        assertEquals(classNames.length, model.getRowCount());
        for (int i = 0; i < classNames.length; i++) {
            assertEquals(classNames[i], model.getValueAt(i, 0));
        }
    }

    private static CodeMarkerSettingsState.ClassIconMapping rule(String className) {
        return new CodeMarkerSettingsState.ClassIconMapping(className, "", "", "document");
    }
}
