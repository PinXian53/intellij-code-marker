package com.pino.intellijcodemarker.settings.ui;

import com.intellij.openapi.ui.ComboBox;

import javax.swing.*;

@SuppressWarnings("unchecked")
public class IconComboBoxEditor extends DefaultCellEditor {

    private final ComboBox<String> comboBox;

    public IconComboBoxEditor() {
        super(new ComboBox<>(new IconComboBoxModel()));
        this.comboBox = (ComboBox<String>) getComponent();
        this.comboBox.setRenderer(new IconListCellRenderer());

        // Set click count to 1 for immediate editing
        setClickCountToStart(1);
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
