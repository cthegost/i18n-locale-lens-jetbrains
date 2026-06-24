package com.paprikaapps.i18nlocalelens;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public final class LocaleLensReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PsiElement.class),
            new PsiReferenceProvider() {
                @Override
                public PsiReference @NotNull [] getReferencesByElement(
                    @NotNull PsiElement element,
                    @NotNull ProcessingContext context
                ) {
                    String rawValue = StringKeyResolver.extractStringContent(element.getText());

                    if (rawValue == null || !looksLikeTranslationKey(rawValue)) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    return new PsiReference[] {
                        new LocaleLensReference(
                            element,
                            TextRange.create(1, element.getTextLength() - 1),
                            rawValue
                        )
                    };
                }
            }
        );
    }

    private static boolean looksLikeTranslationKey(String value) {
        return value.contains(".") || value.contains(":") || value.contains("${");
    }
}
