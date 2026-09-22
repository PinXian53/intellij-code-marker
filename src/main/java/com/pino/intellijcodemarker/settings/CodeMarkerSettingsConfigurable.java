package com.pino.intellijcodemarker.settings;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.ui.RowsDnDSupport;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.intellij.util.xmlb.XmlSerializer;
import com.pino.intellijcodemarker.resource.IconResource;
import com.pino.intellijcodemarker.settings.ui.ClassIconTableModel;
import com.pino.intellijcodemarker.settings.ui.IconComboBoxEditor;
import com.pino.intellijcodemarker.settings.ui.IconComboBoxRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
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

        // Rows can be dragged to a new position; the reorder buttons below make that discoverable
        RowsDnDSupport.install(table, tableModel);

        // Create toolbar with add/remove/reorder and import/export buttons
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
                })
                .setMoveUpAction(button -> moveSelectedRow(-1))
                .setMoveDownAction(button -> moveSelectedRow(1))
                .setMoveUpActionName("Move Rule Up")
                .setMoveDownActionName("Move Rule Down")
                .addExtraAction(new DumbAwareAction("Export Rules\u2026",
                        "Save the rules in this table to a file", AllIcons.ToolbarDecorator.Export) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        exportRules();
                    }
                })
                .addExtraAction(new DumbAwareAction("Import Rules\u2026",
                        "Load rules from a previously exported file", AllIcons.ToolbarDecorator.Import) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        importRules();
                    }
                });

        JPanel tablePanel = decorator.createPanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        // Add explanatory text
        JPanel explanationPanel = new JPanel();
        explanationPanel.setLayout(new BoxLayout(explanationPanel, BoxLayout.Y_AXIS));
        explanationPanel.setBorder(JBUI.Borders.emptyTop(10));

        String[] explanations = {
                "• A rule needs a class name or an annotation",
                "• Empty method name matches all methods",
                "• Names may be simple or fully qualified",
                "• Super classes and interfaces count too",
                "• First match wins; drag rows to reorder"
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
        stopEditing();

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

    private void moveSelectedRow(int delta) {
        stopEditing();
        int selectedRow = table.getSelectedRow();
        int targetRow = selectedRow + delta;
        if (!tableModel.canExchangeRows(selectedRow, targetRow)) {
            return;
        }
        tableModel.exchangeRows(selectedRow, targetRow);
        table.setRowSelectionInterval(targetRow, targetRow);
        table.scrollRectToVisible(table.getCellRect(targetRow, 0, true));
    }

    private void exportRules() {
        stopEditing();

        FileSaverDescriptor descriptor = new FileSaverDescriptor(
                "Export Code Marker Rules", "Save the rules in this table to a file", "xml");
        VirtualFileWrapper target = FileChooserFactory.getInstance()
                .createSaveFileDialog(descriptor, mainPanel)
                .save((VirtualFile) null, "code-marker-rules.xml");
        if (target == null) {
            return;
        }

        // The exported file uses the same shape as the settings file, so it can be hand edited
        CodeMarkerSettingsState exported = new CodeMarkerSettingsState();
        exported.classIconMappings.addAll(tableModel.getMappings());
        try {
            JDOMUtil.write(XmlSerializer.serialize(exported), target.getFile().toPath());
        } catch (Exception e) {
            Messages.showErrorDialog(mainPanel, "Could not write the file: " + e.getMessage(), "Export Failed");
        }
    }

    private void importRules() {
        stopEditing();

        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.singleFile()
                .withTitle("Import Code Marker Rules")
                .withDescription("Select a file exported from Code Marker")
                .withExtensionFilter("xml");
        VirtualFile file = FileChooser.chooseFile(descriptor, mainPanel, null, null);
        if (file == null) {
            return;
        }

        List<CodeMarkerSettingsState.ClassIconMapping> imported;
        int unknownIcons;
        try {
            CodeMarkerSettingsState state = XmlSerializer.deserialize(
                    JDOMUtil.load(Path.of(file.getPath())), CodeMarkerSettingsState.class);
            imported = new ArrayList<>();
            unknownIcons = 0;
            for (CodeMarkerSettingsState.ClassIconMapping mapping : state.classIconMappings) {
                String iconName = orEmpty(mapping.getIconName());
                if (IconResource.getIconPath(iconName) == null) {
                    // The file may come from another plugin version that knows other icons
                    iconName = ClassIconTableModel.DEFAULT_ICON_NAME;
                    unknownIcons++;
                }
                imported.add(new CodeMarkerSettingsState.ClassIconMapping(
                        orEmpty(mapping.getClassName()),
                        orEmpty(mapping.getAnnotationName()),
                        orEmpty(mapping.getMethodName()),
                        iconName));
            }
        } catch (Exception e) {
            Messages.showErrorDialog(mainPanel,
                    "Could not read the rules from this file: " + e.getMessage(), "Import Failed");
            return;
        }

        if (imported.isEmpty()) {
            Messages.showInfoMessage(mainPanel, "This file contains no rules.", "Nothing to Import");
            return;
        }

        List<CodeMarkerSettingsState.ClassIconMapping> current = tableModel.getMappings();
        if (!current.isEmpty()) {
            int choice = Messages.showYesNoCancelDialog(mainPanel,
                    "Replace the " + current.size() + " rule(s) in the table with the "
                            + imported.size() + " imported one(s), or add them below?",
                    "Import Rules", "Replace", "Add", "Cancel", null);
            if (choice == Messages.CANCEL) {
                return;
            }
            if (choice == Messages.NO) {
                imported.addAll(0, current);
            }
        }

        tableModel.setMappings(imported);

        if (unknownIcons > 0) {
            Messages.showInfoMessage(mainPanel,
                    unknownIcons + " rule(s) referenced an unknown icon and now use the default one.",
                    "Imported");
        }
    }

    private void stopEditing() {
        if (table != null && table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
