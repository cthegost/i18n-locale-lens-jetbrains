package com.paprikaapps.i18nlocalelens;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class LocaleLensConfigurable implements SearchableConfigurable {
    private final Project project;
    private JPanel panel;
    private final JBTextField localesField = new JBTextField();
    private final JBTextArea pathTemplatesArea = new JBTextArea(4, 40);
    private final JBTextField defaultNamespaceField = new JBTextField();
    private final JBTextArea namespaceFileMapArea = new JBTextArea(4, 40);
    private final JBTextField namespaceSeparatorField = new JBTextField();
    private final JBTextField keySeparatorField = new JBTextField();
    private final JBCheckBox searchAllFilesCheckbox =
        new JBCheckBox("Search all namespace files for keys without namespace");
    private final JBCheckBox resolveTemplateStringsCheckbox =
        new JBCheckBox("Resolve simple template string expressions");

    public LocaleLensConfigurable(Project project) {
        this.project = project;
    }

    @Override
    public @NotNull String getId() {
        return "com.paprikaapps.i18n-locale-lens";
    }

    @Override
    public @Nls String getDisplayName() {
        return "i18n Locale Lens";
    }

    @Override
    public @Nullable JComponent createComponent() {
        resetFields();

        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Locales, comma-separated:", localesField)
            .addLabeledComponent("Path templates, one per line:", pathTemplatesArea)
            .addLabeledComponent("Default namespace:", defaultNamespaceField)
            .addLabeledComponent("Namespace to file map, one key=value per line:", namespaceFileMapArea)
            .addLabeledComponent("Namespace separator:", namespaceSeparatorField)
            .addLabeledComponent("Key separator:", keySeparatorField)
            .addComponent(searchAllFilesCheckbox)
            .addComponent(resolveTemplateStringsCheckbox)
            .addComponent(new JBLabel("Supported placeholders: {locale}, {language}, {namespace}, {namespaceFile}"))
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();

        return panel;
    }

    @Override
    public boolean isModified() {
        LocaleLensSettings settings = getSettings();

        return !splitCommaSeparated(localesField.getText()).equals(settings.locales) ||
            !splitLines(pathTemplatesArea.getText()).equals(settings.pathTemplates) ||
            !Objects.equals(defaultNamespaceField.getText().trim(), settings.defaultNamespace) ||
            !Objects.equals(namespaceFileMapArea.getText().trim(), settings.namespaceFileMap.trim()) ||
            !Objects.equals(namespaceSeparatorField.getText(), settings.namespaceSeparator) ||
            !Objects.equals(keySeparatorField.getText(), settings.keySeparator) ||
            searchAllFilesCheckbox.isSelected() != settings.searchAllNamespaceFilesForKeysWithoutNamespace ||
            resolveTemplateStringsCheckbox.isSelected() != settings.resolveTemplateStringExpressions;
    }

    @Override
    public void apply() {
        LocaleLensSettings settings = getSettings();

        settings.locales = splitCommaSeparated(localesField.getText());
        settings.pathTemplates = splitLines(pathTemplatesArea.getText());
        settings.defaultNamespace = defaultNamespaceField.getText().trim();
        settings.namespaceFileMap = namespaceFileMapArea.getText().trim();
        settings.namespaceSeparator = namespaceSeparatorField.getText();
        settings.keySeparator = keySeparatorField.getText();
        settings.searchAllNamespaceFilesForKeysWithoutNamespace = searchAllFilesCheckbox.isSelected();
        settings.resolveTemplateStringExpressions = resolveTemplateStringsCheckbox.isSelected();
    }

    @Override
    public void reset() {
        resetFields();
    }

    @Override
    public void disposeUIResources() {
        panel = null;
    }

    private void resetFields() {
        LocaleLensSettings settings = getSettings();

        localesField.setText(String.join(", ", settings.locales));
        pathTemplatesArea.setText(String.join("\n", settings.pathTemplates));
        defaultNamespaceField.setText(settings.defaultNamespace);
        namespaceFileMapArea.setText(settings.namespaceFileMap);
        namespaceSeparatorField.setText(settings.namespaceSeparator);
        keySeparatorField.setText(settings.keySeparator);
        searchAllFilesCheckbox.setSelected(settings.searchAllNamespaceFilesForKeysWithoutNamespace);
        resolveTemplateStringsCheckbox.setSelected(settings.resolveTemplateStringExpressions);
    }

    private LocaleLensSettings getSettings() {
        return project.getService(LocaleLensProjectSettings.class).getState();
    }

    private static List<String> splitCommaSeparated(String value) {
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(item -> !item.isEmpty())
            .toList();
    }

    private static List<String> splitLines(String value) {
        return Arrays.stream(value.split("\\R"))
            .map(String::trim)
            .filter(item -> !item.isEmpty())
            .toList();
    }
}
