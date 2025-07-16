package com.pino.intellijcodemarker.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeMarkerSettingsConfigurable implements Configurable {

    private JPanel mainPanel;
    private JBTable table;
    private ClassIconTableModel tableModel;

    private static final String[] AVAILABLE_ICONS = {
            "microsoft-corporation/document.svg",
            "microsoft-corporation/mail.svg",
            "microsoft-corporation/shield.svg",
            "microsoft-corporation/start.svg",
            "microsoft-corporation/alert.svg",
            "microsoft-corporation/chat.svg",
            "microsoft-corporation/cloud.svg",
            "material-extensions/readme.svg",
            "material-extensions/open-api.svg",
            "material-extensions/swagger.svg",
            "material-extensions/test-1.svg",
            "material-extensions/test-2.svg",
            "material-extensions/todo.svg",
            "material-extensions/key.svg",
            "material-extensions/lock.svg",
            "microsoft-corporation/database.svg",
            "material-extensions/db.svg",
            "devicon/postgresql.svg",
            "devicon/redis.svg",
    };

    private static final Map<String, Icon> iconCache = new HashMap<>();

    private static Icon loadSvgIcon(String iconName) {
        if (iconCache.containsKey(iconName)) {
            return iconCache.get(iconName);
        }

        try {
            Icon icon = IconLoader.getIcon("/icons/" + iconName, CodeMarkerSettingsConfigurable.class);
            iconCache.put(iconName, icon);
            return icon;
        } catch (Exception e) {
            // Fallback to a default icon if loading fails
            return IconLoader.getIcon("/icons/info.svg", CodeMarkerSettingsConfigurable.class);
        }
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Code Marker";
    }

    @Override
    public @Nullable JComponent createComponent() {
        if (mainPanel == null) {
            createUI();
        }
        return mainPanel;
    }

    private void createUI() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(JBUI.Borders.empty(10));

        // Create table model and table
        tableModel = new ClassIconTableModel();
        table = new JBTable(tableModel);

        // Set up table columns
        table.getColumnModel().getColumn(0).setHeaderValue("Class Name");
        table.getColumnModel().getColumn(1).setHeaderValue("Icon");

        // Set up icon column with combo box
        table.getColumnModel().getColumn(1).setCellEditor(new IconComboBoxEditor());
        table.getColumnModel().getColumn(1).setCellRenderer(new IconComboBoxRenderer());

        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(300);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);

        // Create toolbar with add/remove buttons
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table)
                .setAddAction(e -> {
                    tableModel.addRow();
                    int newRow = tableModel.getRowCount() - 1;
                    table.setRowSelectionInterval(newRow, newRow);
                    table.editCellAt(newRow, 0);
                })
                .setRemoveAction(e -> {
                    int selectedRow = table.getSelectedRow();

                    // If table is in editing mode, get the editing row
                    if (table.isEditing()) {
                        selectedRow = table.getEditingRow();
                        // Stop editing to ensure proper state
                        table.getCellEditor().stopCellEditing();
                    }

                    if (selectedRow >= 0) {
                        tableModel.removeRow(selectedRow);
                    }
                });

        JPanel tablePanel = decorator.createPanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        // Load current settings
        reset();
    }

    @Override
    public boolean isModified() {
        if (tableModel == null) return false;

        CodeMarkerSettingsState settings = CodeMarkerSettingsState.getInstance();
        List<CodeMarkerSettingsState.ClassIconMapping> currentMappings = tableModel.getMappings();

        if (currentMappings.size() != settings.classIconMappings.size()) {
            return true;
        }

        for (int i = 0; i < currentMappings.size(); i++) {
            CodeMarkerSettingsState.ClassIconMapping current = currentMappings.get(i);
            CodeMarkerSettingsState.ClassIconMapping saved = settings.classIconMappings.get(i);

            if (!current.getClassName().equals(saved.getClassName()) ||
                !current.getIconName().equals(saved.getIconName())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        if (tableModel != null) {
            CodeMarkerSettingsState settings = CodeMarkerSettingsState.getInstance();
            settings.classIconMappings.clear();
            settings.classIconMappings.addAll(tableModel.getMappings());
        }
    }

    @Override
    public void reset() {
        if (tableModel != null) {
            CodeMarkerSettingsState settings = CodeMarkerSettingsState.getInstance();
            tableModel.setMappings(new ArrayList<>(settings.classIconMappings));
        }
    }

    @Override
    public void disposeUIResources() {
        mainPanel = null;
        table = null;
        tableModel = null;
    }

    // Table model for class-icon mappings
    private static class ClassIconTableModel extends AbstractTableModel {
        private final List<CodeMarkerSettingsState.ClassIconMapping> mappings = new ArrayList<>();

        @Override
        public int getRowCount() {
            return mappings.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            CodeMarkerSettingsState.ClassIconMapping mapping = mappings.get(rowIndex);
            return columnIndex == 0 ? mapping.getClassName() : mapping.getIconName();
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            CodeMarkerSettingsState.ClassIconMapping mapping = mappings.get(rowIndex);
            if (columnIndex == 0) {
                mapping.setClassName((String) value);
            } else {
                mapping.setIconName((String) value);
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        public void addRow() {
            mappings.add(new CodeMarkerSettingsState.ClassIconMapping("", AVAILABLE_ICONS[0]));
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
    }

    // Custom combo box model for icons
    private static class IconComboBoxModel extends DefaultComboBoxModel<String> {
        public IconComboBoxModel() {
            super(AVAILABLE_ICONS);
        }
    }

    // Custom list cell renderer for displaying icons with text
    private static class IconListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof String iconName) {
                var icon = loadSvgIcon(iconName);
                setIcon(icon);
                setText(iconName.replace(".svg", "")); // Display filename without extension
            }

            return this;
        }
    }

    // Combo box editor for icon column
    private static class IconComboBoxEditor extends DefaultCellEditor {
        private final ComboBox<String> comboBox;

        public IconComboBoxEditor() {
            super(new ComboBox<>(new IconComboBoxModel()));
            this.comboBox = (ComboBox<String>) getComponent();
            this.comboBox.setRenderer(new IconListCellRenderer());
        }
    }

    // Combo box renderer for icon column
    private static class IconComboBoxRenderer extends DefaultTableCellRenderer {
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
}
