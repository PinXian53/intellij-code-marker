package com.pino.intellijcodemarker.settings.ui;

import com.intellij.openapi.ui.ComboBox;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class IconComboBoxRenderer extends DefaultTableCellRenderer {
    private final ComboBox<String> comboBox;

    public IconComboBoxRenderer() {
        this.comboBox = new ComboBox<>(new IconComboBoxModel());
        this.comboBox.setRenderer(new IconListCellRenderer());
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        comboBox.setSelectedItem(value);

        if (isSelected) {
            comboBox.setBackground(table.getSelectionBackground());
            comboBox.setForeground(table.getSelectionForeground());
        } else {
            comboBox.setBackground(table.getBackground());
            comboBox.setForeground(table.getForeground());
        }

        return comboBox;
    }
}
