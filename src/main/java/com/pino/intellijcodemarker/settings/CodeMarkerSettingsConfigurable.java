package com.pino.intellijcodemarker.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.pino.intellijcodemarker.settings.ui.ClassIconTableModel;
import com.pino.intellijcodemarker.settings.ui.IconComboBoxEditor;
import com.pino.intellijcodemarker.settings.ui.IconComboBoxRenderer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.List;

public class CodeMarkerSettingsConfigurable implements Configurable {

    private JPanel mainPanel;
    private JBTable table;
    private ClassIconTableModel tableModel;

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

    // TransferHandler for drag-and-drop row reordering
    private class TableRowTransferHandler extends TransferHandler {
        private final DataFlavor localObjectFlavor = new DataFlavor(Integer.class, "application/x-java-Integer");
        private int[] indices = null;

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

            try {
                int rowFrom = (Integer) support.getTransferable().getTransferData(localObjectFlavor);
                if (rowFrom != dropRow) {
                    if (dropRow > rowFrom) {
                        dropRow--;
                    }
                    tableModel.moveRow(rowFrom, dropRow);
                    table.getSelectionModel().addSelectionInterval(dropRow, dropRow);
                    return true;
                }
            } catch (Exception e) {
                // do nothing
            }

            return false;
        }

        @Override
        protected void exportDone(JComponent c, Transferable data, int action) {
            indices = null;
        }
    }

}
