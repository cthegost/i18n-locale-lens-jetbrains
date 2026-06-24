package com.paprikaapps.i18nlocalelens;

import java.util.ArrayList;
import java.util.List;

public class LocaleLensSettings {
    public List<String> locales = new ArrayList<>(List.of("ru"));
    public List<String> pathTemplates = new ArrayList<>(List.of("public/locales/{locale}/{namespaceFile}.json"));
    public String defaultNamespace = "translation";
    public String namespaceFileMap = "translation=common\ncommon=common";
    public String namespaceSeparator = ":";
    public String keySeparator = ".";
    public boolean searchAllNamespaceFilesForKeysWithoutNamespace = true;
    public boolean resolveTemplateStringExpressions = true;
}
