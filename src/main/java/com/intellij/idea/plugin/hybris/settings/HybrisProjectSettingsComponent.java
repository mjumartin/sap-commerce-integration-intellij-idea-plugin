/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.intellij.idea.plugin.hybris.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.HYBRIS_PROJECT_SETTINGS_COMPONENT_NAME;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.HYBRIS_PROJECT_SETTINGS_FILE_NAME;

/**
 * Created 6:43 PM 28 June 2015.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
@State(name = HYBRIS_PROJECT_SETTINGS_COMPONENT_NAME, storages = {@Storage(HYBRIS_PROJECT_SETTINGS_FILE_NAME)})
public class HybrisProjectSettingsComponent implements PersistentStateComponent<HybrisProjectSettings> {

    private final HybrisProjectSettings hybrisProjectSettings = new HybrisProjectSettings();

    public static HybrisProjectSettingsComponent getInstance(@NotNull final Project project) {
        return ServiceManager.getService(project, HybrisProjectSettingsComponent.class);
    }

    @NotNull
    @Override
    public HybrisProjectSettings getState() {
        return this.hybrisProjectSettings;
    }

    @Override
    public void loadState(final HybrisProjectSettings state) {
        XmlSerializerUtil.copyBean(state, this.hybrisProjectSettings);
    }
}
