package com.paprikaapps.i18nlocalelens;

import com.intellij.psi.PsiFile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringKeyResolver {
    private static final Pattern STRING_CONSTANT_PATTERN = Pattern.compile(
        "\\b(?:const|let|var)\\s+([A-Za-z_$][\\w$]*)\\s*=\\s*(['\"`])((?:\\\\.|(?!\\2).)*)(\\2)"
    );
    private static final Pattern TEMPLATE_EXPRESSION_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[A-Za-z_$][\\w$]*$");

    private StringKeyResolver() {
    }

    public static String extractStringContent(String text) {
        if (text == null || text.length() < 2) {
            return null;
        }

        char quote = text.charAt(0);

        if (quote != '\'' && quote != '"' && quote != '`') {
            return null;
        }

        if (text.charAt(text.length() - 1) != quote) {
            return null;
        }

        return unescape(text.substring(1, text.length() - 1));
    }

    public static String resolve(String rawValue, PsiFile file, LocaleLensSettings settings) {
        if (!rawValue.contains("${")) {
            return rawValue;
        }

        if (!settings.resolveTemplateStringExpressions) {
            return null;
        }

        return resolveTemplateString(rawValue, collectStringConstants(file), new HashSet<>());
    }

    public static TranslationKey parseTranslationKey(String value, LocaleLensSettings settings) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String separator = settings.namespaceSeparator;

        if (separator == null || separator.isEmpty()) {
            return new TranslationKey(value, null);
        }

        int separatorIndex = value.indexOf(separator);

        if (separatorIndex == -1) {
            return new TranslationKey(value, null);
        }

        String namespace = value.substring(0, separatorIndex);

        if (namespace.isBlank() || namespace.chars().anyMatch(Character::isWhitespace)) {
            return new TranslationKey(value, null);
        }

        return new TranslationKey(value.substring(separatorIndex + separator.length()), namespace);
    }

    private static Map<String, String> collectStringConstants(PsiFile file) {
        Map<String, String> constants = new HashMap<>();
        Matcher matcher = STRING_CONSTANT_PATTERN.matcher(file.getText());

        while (matcher.find()) {
            constants.put(matcher.group(1), unescape(matcher.group(3)));
        }

        return constants;
    }

    private static String resolveTemplateString(
        String value,
        Map<String, String> constants,
        Set<String> resolving
    ) {
        Matcher matcher = TEMPLATE_EXPRESSION_PATTERN.matcher(value);
        StringBuilder result = new StringBuilder();
        int lastIndex = 0;

        while (matcher.find()) {
            String expression = matcher.group(1).trim();
            String resolvedExpression = resolveTemplateExpression(expression, constants, resolving);

            if (resolvedExpression == null) {
                return null;
            }

            result.append(value, lastIndex, matcher.start());
            result.append(resolvedExpression);
            lastIndex = matcher.end();
        }

        result.append(value.substring(lastIndex));

        return result.toString();
    }

    private static String resolveTemplateExpression(
        String expression,
        Map<String, String> constants,
        Set<String> resolving
    ) {
        if (!IDENTIFIER_PATTERN.matcher(expression).matches() ||
            !constants.containsKey(expression) ||
            resolving.contains(expression)
        ) {
            return null;
        }

        resolving.add(expression);

        String value = constants.get(expression);
        String resolved = value.contains("${")
            ? resolveTemplateString(value, constants, resolving)
            : value;

        resolving.remove(expression);

        return resolved;
    }

    private static String unescape(String value) {
        return value
            .replace("\\'", "'")
            .replace("\\\"", "\"")
            .replace("\\`", "`")
            .replace("\\\\", "\\");
    }
}
