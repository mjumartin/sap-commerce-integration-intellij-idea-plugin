package com.intellij.idea.plugin.hybris.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.HYBRIS_DEVELOPER_SPECIFIC_PROJECT_SETTINGS_COMPONENT_NAME;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.HYBRIS_DEVELOPER_SPECIFIC_PROJECT_SETTINGS_FILE_NAME;

@State(name = HYBRIS_DEVELOPER_SPECIFIC_PROJECT_SETTINGS_COMPONENT_NAME, storages = {@Storage(HYBRIS_DEVELOPER_SPECIFIC_PROJECT_SETTINGS_FILE_NAME)})
public class HybrisDeveloperSpecificProjectSettingsComponent
    implements PersistentStateComponent<HybrisDeveloperSpecificProjectSettings> {

    private HybrisDeveloperSpecificProjectSettings state = new HybrisDeveloperSpecificProjectSettings();

    public static HybrisDeveloperSpecificProjectSettingsComponent getInstance(@NotNull final Project project) {
        return ServiceManager.getService(project, HybrisDeveloperSpecificProjectSettingsComponent.class);
    }

    @Nullable
    @Override
    public HybrisDeveloperSpecificProjectSettings getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull final HybrisDeveloperSpecificProjectSettings state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }
}
