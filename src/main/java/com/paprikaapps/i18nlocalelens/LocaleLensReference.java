package com.paprikaapps.i18nlocalelens;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LocaleLensReference extends PsiReferenceBase<PsiElement> {
    private final String rawValue;

    public LocaleLensReference(PsiElement element, TextRange rangeInElement, String rawValue) {
        super(element, rangeInElement, true);
        this.rawValue = rawValue;
    }

    @Override
    public @Nullable PsiElement resolve() {
        Project project = getElement().getProject();
        PsiFile file = getElement().getContainingFile();

        if (file == null) {
            return null;
        }

        LocaleLensSettings settings = project.getService(LocaleLensProjectSettings.class).getState();
        String resolvedValue = StringKeyResolver.resolve(rawValue, file, settings);

        if (resolvedValue == null) {
            return null;
        }

        TranslationKey translationKey = StringKeyResolver.parseTranslationKey(resolvedValue, settings);

        if (translationKey == null) {
            return null;
        }

        return LocaleJsonResolver.resolve(project, translationKey, settings);
    }

    @Override
    public Object @NotNull [] getVariants() {
        return new Object[0];
    }
}
