package com.pino.intellijcodemarker.settings.ui;

import com.intellij.openapi.ui.ComboBox;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("unchecked")
public class IconComboBoxEditor extends DefaultCellEditor {

    private final ComboBox<String> comboBox;

    public IconComboBoxEditor() {
        super(new ComboBox<>(new IconComboBoxModel()));
        this.comboBox = (ComboBox<String>) getComponent();
        this.comboBox.setRenderer(new IconListCellRenderer());

        // Set click count to 1 for normal editing behavior
        setClickCountToStart(1);
        
        // Disable automatic selection of first item when popup opens
        comboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        // Set the current value
        comboBox.setSelectedItem(value);
        
        // Return the comboBox component
        return comboBox;
    }

    @Override
    public Object getCellEditorValue() {
        return comboBox.getSelectedItem();
    }

    @Override
    public boolean stopCellEditing() {
        return super.stopCellEditing();
    }
}
