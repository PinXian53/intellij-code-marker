package com.pino.intellijcodemarker.settings;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
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
        table.getColumnModel().getColumn(1).setHeaderValue("Annotation");
        table.getColumnModel().getColumn(2).setHeaderValue("Method Name");
        table.getColumnModel().getColumn(3).setHeaderValue("Icon");

        // Set up icon column with combo box
        table.getColumnModel().getColumn(3).setCellEditor(new IconComboBoxEditor());
        table.getColumnModel().getColumn(3).setCellRenderer(new IconComboBoxRenderer());

        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(250);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(30);

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

        String[] explanations = {
                "• Each rule needs a class name or an annotation; when both are filled in, both must match",
                "• To select all methods in the class, please leave the method name field empty",
                "• Class names and annotations accept a fully qualified name or a simple name, and both are matched on super classes, interfaces and overridden methods as well",
                "• If multiple rules match, the first one takes precedence"
        };
        for (int i = 0; i < explanations.length; i++) {
            if (i > 0) {
                explanationPanel.add(Box.createVerticalStrut(5));
            }
            JLabel explanation = new JLabel(explanations[i]);
            explanation.setFont(explanation.getFont().deriveFont(Font.PLAIN, 12f));
            explanation.setForeground(UIManager.getColor("Label.foreground"));
            explanationPanel.add(explanation);
        }

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
                !current.getAnnotationName().equals(saved.getAnnotationName()) ||
                !current.getIconName().equals(saved.getIconName()) ||
                !current.getMethodName().equals(saved.getMethodName())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        if (tableModel == null) {
            return;
        }

        // Commit the cell being edited, otherwise its new value would be lost on apply
        if (table != null && table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        List<CodeMarkerSettingsState.ClassIconMapping> mappings = tableModel.getMappings();
        for (int i = 0; i < mappings.size(); i++) {
            CodeMarkerSettingsState.ClassIconMapping mapping = mappings.get(i);
            if (mapping.getClassName().isBlank() && mapping.getAnnotationName().isBlank()) {
                throw new ConfigurationException(
                        "Row " + (i + 1) + ": please fill in a class name or an annotation.");
            }
        }

        CodeMarkerSettingsState settings = CodeMarkerSettingsState.getInstance();
        settings.classIconMappings.clear();
        settings.classIconMappings.addAll(mappings);
        settings.ruleChanged();

        // Drop the cached markers and repaint the gutters of the files that are already open
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            if (!project.isDisposed()) {
                DaemonCodeAnalyzer.getInstance(project).restart();
            }
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
                        original.getAnnotationName(),
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
