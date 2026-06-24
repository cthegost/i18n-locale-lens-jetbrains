package com.paprikaapps.i18nlocalelens;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

@Service(Service.Level.PROJECT)
@State(name = "I18nLocaleLensSettings", storages = @Storage("i18nLocaleLens.xml"))
public final class LocaleLensProjectSettings implements PersistentStateComponent<LocaleLensSettings> {
    private LocaleLensSettings state = new LocaleLensSettings();

    @Override
    public @NotNull LocaleLensSettings getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull LocaleLensSettings state) {
        this.state = state;
    }
}
