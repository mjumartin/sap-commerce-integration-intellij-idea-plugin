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

package com.intellij.idea.plugin.hybris.project.configurators;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by Martin Zdarsky (martin.zdarsky@hybris.com) on 18/08/15.
 */
public interface ConfiguratorFactory {

    @NotNull
    List<FacetConfigurator> getFacetConfigurators();

    @NotNull
    SpringConfigurator getSpringConfigurator();

    @NotNull
    ModulesDependenciesConfigurator getModulesDependenciesConfigurator();

    @NotNull
    CompilerOutputPathsConfigurator getCompilerOutputPathsConfigurator();

    @NotNull
    ContentRootConfigurator getRegularContentRootConfigurator();

    @NotNull
    ContentRootConfigurator getReadOnlyContentRootConfigurator();

    @NotNull
    LibRootsConfigurator getLibRootsConfigurator();

    @NotNull
    GroupModuleConfigurator getGroupModuleConfigurator();

    @NotNull
    JavadocModuleConfigurator getJavadocModuleConfigurator();

    @NotNull
    ModuleSettingsConfigurator getModuleSettingsConfigurator();

    @NotNull
    VersionControlSystemConfigurator getVersionControlSystemConfigurator();

    @NotNull
    RunConfigurationConfigurator getDebugRunConfigurationConfigurator();

    @Nullable
    RunConfigurationConfigurator getTestRunConfigurationConfigurator();

    @Nullable
    MavenConfigurator getMavenConfigurator();

    @NotNull
    SearchScopeConfigurator getSearchScopeConfigurator();

    @NotNull
    LoadedConfigurator getLoadedConfigurator();

    @Nullable
    GradleConfigurator getGradleConfigurator();

    @Nullable
    DataSourcesConfigurator getDataSourcesConfigurator();

    @Nullable
    JavaCompilerConfigurator getCompilerConfigurator();
}
