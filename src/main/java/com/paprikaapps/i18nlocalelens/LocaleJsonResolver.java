package com.paprikaapps.i18nlocalelens;

import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class LocaleJsonResolver {
    private LocaleJsonResolver() {
    }

    public static PsiElement resolve(Project project, TranslationKey translationKey, LocaleLensSettings settings) {
        String basePath = project.getBasePath();

        if (basePath == null) {
            return null;
        }

        for (File candidate : getCandidateFiles(basePath, translationKey, settings)) {
            JsonFile jsonFile = findJsonFile(project, candidate);

            if (jsonFile == null) {
                continue;
            }

            JsonProperty property = findProperty(jsonFile, translationKey.key(), settings.keySeparator);

            if (property != null) {
                return property.getNameElement() != null ? property.getNameElement() : property;
            }
        }

        return null;
    }

    private static List<File> getCandidateFiles(
        String basePath,
        TranslationKey translationKey,
        LocaleLensSettings settings
    ) {
        Set<File> result = new LinkedHashSet<>();
        Map<String, String> namespaceFileMap = parseNamespaceFileMap(settings.namespaceFileMap);
        String namespace = translationKey.namespace() != null ? translationKey.namespace() : settings.defaultNamespace;

        for (String locale : settings.locales) {
            result.addAll(resolveNamespaceFiles(basePath, locale, namespace, namespaceFileMap, settings));

            if (translationKey.namespace() == null && settings.searchAllNamespaceFilesForKeysWithoutNamespace) {
                result.addAll(collectLocaleJsonFiles(basePath, locale, namespaceFileMap, settings));
            }
        }

        return new ArrayList<>(result);
    }

    private static List<File> resolveNamespaceFiles(
        String basePath,
        String locale,
        String namespace,
        Map<String, String> namespaceFileMap,
        LocaleLensSettings settings
    ) {
        String namespaceFile = renderTemplate(
            namespaceFileMap.getOrDefault(namespace, namespace),
            locale,
            namespace,
            namespace
        );
        List<File> result = new ArrayList<>();

        for (String template : settings.pathTemplates) {
            result.add(
                new File(
                    basePath,
                    renderTemplate(template, locale, namespace, namespaceFile)
                )
            );
        }

        return result;
    }

    private static List<File> collectLocaleJsonFiles(
        String basePath,
        String locale,
        Map<String, String> namespaceFileMap,
        LocaleLensSettings settings
    ) {
        List<File> result = new ArrayList<>();
        Set<File> directories = new LinkedHashSet<>();

        for (String template : settings.pathTemplates) {
            directories.add(inferLocaleDirectory(basePath, template, locale, namespaceFileMap, settings));
        }

        for (File directory : directories) {
            collectJsonFiles(directory, result);
        }

        return result;
    }

    private static File inferLocaleDirectory(
        String basePath,
        String template,
        String locale,
        Map<String, String> namespaceFileMap,
        LocaleLensSettings settings
    ) {
        String defaultNamespaceFile = namespaceFileMap.getOrDefault(settings.defaultNamespace, settings.defaultNamespace);
        String renderedTemplate = renderTemplate(template, locale, settings.defaultNamespace, defaultNamespaceFile);
        String directoryTemplate = template.contains("{namespace")
            ? template.substring(0, template.indexOf("{namespace"))
            : new File(renderedTemplate).getParent();

        if (directoryTemplate == null) {
            directoryTemplate = "";
        }

        return new File(
            basePath,
            renderTemplate(directoryTemplate, locale, settings.defaultNamespace, defaultNamespaceFile)
        );
    }

    private static void collectJsonFiles(File directory, List<File> result) {
        File[] children = directory.listFiles();

        if (children == null) {
            return;
        }

        for (File child : children) {
            if (child.isDirectory()) {
                collectJsonFiles(child, result);
            } else if (child.isFile() && child.getName().endsWith(".json")) {
                result.add(child);
            }
        }
    }

    private static JsonFile findJsonFile(Project project, File file) {
        if (!file.isFile()) {
            return null;
        }

        VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);

        if (virtualFile == null) {
            return null;
        }

        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);

        return psiFile instanceof JsonFile ? (JsonFile) psiFile : null;
    }

    private static JsonProperty findProperty(JsonFile jsonFile, String key, String keySeparator) {
        if (!(jsonFile.getTopLevelValue() instanceof JsonObject root)) {
            return null;
        }

        String[] parts = keySeparator == null || keySeparator.isEmpty()
            ? new String[] { key }
            : key.split(Patterns.quote(keySeparator));
        JsonObject currentObject = root;

        for (int i = 0; i < parts.length; i++) {
            JsonProperty property = currentObject.findProperty(parts[i]);

            if (property == null) {
                return null;
            }

            if (i == parts.length - 1) {
                return property;
            }

            if (!(property.getValue() instanceof JsonObject nextObject)) {
                return null;
            }

            currentObject = nextObject;
        }

        return null;
    }

    private static Map<String, String> parseNamespaceFileMap(String value) {
        Map<String, String> result = new LinkedHashMap<>();

        for (String rawLine : value.split("\\R")) {
            String line = rawLine.trim();

            if (line.isEmpty() || !line.contains("=")) {
                continue;
            }

            result.put(line.substring(0, line.indexOf("=")).trim(), line.substring(line.indexOf("=") + 1).trim());
        }

        return result;
    }

    private static String renderTemplate(String template, String locale, String namespace, String namespaceFile) {
        return template
            .replace("{locale}", locale)
            .replace("{language}", locale)
            .replace("{namespace}", namespace)
            .replace("{namespaceFile}", namespaceFile);
    }

    private static final class Patterns {
        private Patterns() {
        }

        static String quote(String value) {
            return java.util.regex.Pattern.quote(value);
        }
    }
}
