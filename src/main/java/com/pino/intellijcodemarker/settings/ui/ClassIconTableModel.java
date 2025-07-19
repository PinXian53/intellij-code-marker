package com.pino.intellijcodemarker.settings.ui;

import com.pino.intellijcodemarker.resource.IconResource;
import com.pino.intellijcodemarker.settings.CodeMarkerSettingsState;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ClassIconTableModel extends AbstractTableModel {

    public static final int COLUMN_COUNT = 3;
    public static final String DEFAULT_ICON_NAME = IconResource.getDefaultIconName();

    private final List<CodeMarkerSettingsState.ClassIconMapping> mappings = new ArrayList<>();

    @Override
    public int getRowCount() {
        return mappings.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CodeMarkerSettingsState.ClassIconMapping mapping = mappings.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> mapping.getClassName();
            case 1 -> mapping.getMethodName();
            case 2 -> mapping.getIconName();
            default -> "";
        };
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        CodeMarkerSettingsState.ClassIconMapping mapping = mappings.get(rowIndex);
        switch (columnIndex) {
            case 0 -> mapping.setClassName((String) value);
            case 1 -> mapping.setMethodName((String) value);
            case 2 -> mapping.setIconName((String) value);
            default -> {
                return;
            }
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public void addRow() {
        mappings.add(new CodeMarkerSettingsState.ClassIconMapping("", "", DEFAULT_ICON_NAME));
        fireTableRowsInserted(mappings.size() - 1, mappings.size() - 1);
    }

    public void removeRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < mappings.size()) {
            mappings.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public List<CodeMarkerSettingsState.ClassIconMapping> getMappings() {
        return new ArrayList<>(mappings);
    }

    public void setMappings(List<CodeMarkerSettingsState.ClassIconMapping> newMappings) {
        mappings.clear();
        mappings.addAll(newMappings);
        fireTableDataChanged();
    }

    public void moveRow(int fromIndex, int toIndex) {
        if (fromIndex >= 0 && fromIndex < mappings.size() &&
            toIndex >= 0 && toIndex < mappings.size() &&
            fromIndex != toIndex) {

            CodeMarkerSettingsState.ClassIconMapping mapping = mappings.remove(fromIndex);
            mappings.add(toIndex, mapping);
            fireTableRowsUpdated(Math.min(fromIndex, toIndex), Math.max(fromIndex, toIndex));
        }
    }
}
