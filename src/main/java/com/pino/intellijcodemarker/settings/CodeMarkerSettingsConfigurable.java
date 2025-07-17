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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeMarkerSettingsConfigurable implements Configurable {

    private static final String[] AVAILABLE_ICONS = {
            "microsoft-corporation/document.svg",
            "microsoft-corporation/document-add.svg",
            "microsoft-corporation/document-edit.svg",
            "microsoft-corporation/mail.svg",
            "microsoft-corporation/shield.svg",
            "microsoft-corporation/star.svg",
            "microsoft-corporation/alert.svg",
            "microsoft-corporation/message.svg",
            "microsoft-corporation/cloud.svg",
            "microsoft-corporation/database.svg",
            "material-extensions/readme.svg",
            "material-extensions/openapi.svg",
            "material-extensions/openapi-light.svg",
            "material-extensions/swagger.svg",
            "material-extensions/test-1.svg",
            "material-extensions/test-2.svg",
            "material-extensions/todo.svg",
            "material-extensions/key.svg",
            "material-extensions/hurl.svg",
            "material-extensions/palette.svg",
            "material-extensions/slug.svg",
            "material-extensions/tree.svg",
            "devicon/azuresql.svg",
            "devicon/elasticsearch.svg",
            "devicon/gcp.svg",
            "devicon/hadoop.svg",
            "devicon/mongodb.svg",
            "devicon/postgresql.svg",
            "devicon/rabbitmq.svg",
            "devicon/redis.svg",
    };
    private static final Map<String, Icon> iconCache = new HashMap<>();

    private JPanel mainPanel;
    private JBTable table;
    private ClassIconTableModel tableModel;

    private static Icon loadSvgIcon(String iconName) {
        if (iconCache.containsKey(iconName)) {
            return iconCache.get(iconName);
        }
        var icon = IconLoader.getIcon("/icons/" + iconName, CodeMarkerSettingsConfigurable.class);
        iconCache.put(iconName, icon);
        return icon;
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Code Marker";
    }

    @Override
    public @Nullable JComponent createComponent() {
        if (mainPanel == null && !GraphicsEnvironment.isHeadless()) {
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
        table.getColumnModel().getColumn(1).setHeaderValue("Method Name");
        table.getColumnModel().getColumn(2).setHeaderValue("Icon");

        // Set up icon column with combo box
        table.getColumnModel().getColumn(2).setCellEditor(new IconComboBoxEditor());
        table.getColumnModel().getColumn(2).setCellRenderer(new IconComboBoxRenderer());

        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(250);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(30);

        // Enable drag-and-drop for row reordering
        table.setDragEnabled(true);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(new TableRowTransferHandler());

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

        // Add explanatory text
        JPanel explanationPanel = new JPanel();
        explanationPanel.setLayout(new BoxLayout(explanationPanel, BoxLayout.Y_AXIS));
        explanationPanel.setBorder(JBUI.Borders.emptyTop(10));

        JLabel explanation1 = new JLabel("• To select all methods in the class, please leave the method name field empty");
        explanation1.setFont(explanation1.getFont().deriveFont(Font.PLAIN, 12f));
        explanation1.setForeground(UIManager.getColor("Label.foreground"));

        JLabel explanation2 = new JLabel("• If multiple rules match, the first one takes precedence");
        explanation2.setFont(explanation2.getFont().deriveFont(Font.PLAIN, 12f));
        explanation2.setForeground(UIManager.getColor("Label.foreground"));

        explanationPanel.add(explanation1);
        explanationPanel.add(Box.createVerticalStrut(5));
        explanationPanel.add(explanation2);

        mainPanel.add(explanationPanel, BorderLayout.SOUTH);

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
                !current.getIconName().equals(saved.getIconName()) ||
                !current.getMethodName().equals(saved.getMethodName())) {
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
            // Create deep copies of the mappings to prevent immediate changes to settings
            List<CodeMarkerSettingsState.ClassIconMapping> copiedMappings = new ArrayList<>();
            for (CodeMarkerSettingsState.ClassIconMapping original : settings.classIconMappings) {
                copiedMappings.add(new CodeMarkerSettingsState.ClassIconMapping(
                    original.getClassName(),
                    original.getMethodName(),
                    original.getIconName()
                ));
            }
            tableModel.setMappings(copiedMappings);
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
            return 3;
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
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        public void addRow() {
            mappings.add(new CodeMarkerSettingsState.ClassIconMapping("", "", AVAILABLE_ICONS[0]));
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

    // TransferHandler for drag-and-drop row reordering
    private class TableRowTransferHandler extends TransferHandler {
        private final DataFlavor localObjectFlavor = new DataFlavor(Integer.class, "application/x-java-Integer");
        private int[] indices = null;
        private int addIndex = -1;
        private int addCount = 0;

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            boolean canImport = support.getComponent() instanceof JTable && support.isDrop() && support.isDataFlavorSupported(localObjectFlavor);
            support.setShowDropLocation(canImport);
            return canImport;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            assert (c == table);
            indices = table.getSelectedRows();
            return new Transferable() {
                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[]{localObjectFlavor};
                }

                @Override
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return localObjectFlavor.equals(flavor);
                }

                @Override
                public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                    if (!isDataFlavorSupported(flavor)) {
                        throw new UnsupportedFlavorException(flavor);
                    }
                    return indices[0];
                }
            };
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.MOVE;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
            int dropRow = dl.getRow();
            int max = tableModel.getRowCount();
            if (dropRow < 0 || dropRow > max) {
                dropRow = max;
            }

            addIndex = dropRow;
            addCount = indices != null ? indices.length : 0;

            try {
                Integer rowFrom = (Integer) support.getTransferable().getTransferData(localObjectFlavor);
                if (rowFrom != null && rowFrom != dropRow) {
                    if (dropRow > rowFrom) {
                        dropRow--;
                    }
                    tableModel.moveRow(rowFrom, dropRow);
                    table.getSelectionModel().addSelectionInterval(dropRow, dropRow);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void exportDone(JComponent c, Transferable data, int action) {
            if ((action == TransferHandler.MOVE) && (indices != null)) {
                // Cleanup is handled in importData
            }
            indices = null;
            addCount = 0;
            addIndex = -1;
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

                var nameElement = iconName.split("/");
                var text = nameElement[nameElement.length - 1].replace(".svg", "");
                setText(text);
            }

            return this;
        }
    }

    // Combo box editor for icon column
    private class IconComboBoxEditor extends DefaultCellEditor {
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
            // This will automatically trigger setValueAt in the table model
            boolean result = super.stopCellEditing();
            return result;
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
