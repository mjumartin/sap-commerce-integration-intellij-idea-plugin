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

package com.intellij.idea.plugin.hybris.project.configurators.impl;

import com.intellij.find.FindSettings;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.project.configurators.SearchScopeConfigurator;
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.scope.packageSet.FilePatternPackageSet;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.psi.search.scope.packageSet.NamedScopeManager;
import com.intellij.psi.search.scope.packageSet.UnionPackageSet;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.SEARCH_SCOPE_GROUP_PREFIX;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.SEARCH_SCOPE_Y_PREFIX;

/**
 * Created by Martin Zdarsky-Jones on 04/07/2017.
 */
public class DefaultSearchScopeConfigurator implements SearchScopeConfigurator {

    @Override
    public void configure(
        @NotNull final Project project,
        @NotNull final ModifiableModuleModel model
    ) {
        final String customGroupName = HybrisApplicationSettingsComponent.getInstance().getState().getGroupCustom();
        final String commerceGroupName = HybrisApplicationSettingsComponent.getInstance().getState().getGroupHybris();
        final String nonHybrisGroupName = HybrisApplicationSettingsComponent.getInstance()
                                                                            .getState()
                                                                            .getGroupNonHybris();
        final String platformGroupName = HybrisApplicationSettingsComponent.getInstance().getState().getGroupPlatform();
        final List<NamedScope> newScopes = new ArrayList<>();
        NamedScope customScope = null;
        NamedScope platformScope = null;
        NamedScope commerceScope = null;
        NamedScope hybrisScope = null;

        if (groupExists(model, customGroupName)) {
            customScope = createScope(customGroupName);
            newScopes.add(customScope);

            newScopes.add(new NamedScope(
                SEARCH_SCOPE_Y_PREFIX + " " + HybrisI18NBundleUtils.message("scope.custom.ts.files"),
                createCustomTSFilesPattern()
            ));

            newScopes.add(new NamedScope(
                SEARCH_SCOPE_Y_PREFIX + " " + HybrisI18NBundleUtils.message("scope.custom.ts.beans.impex.files"),
                createCustomTsImpexBeansFilesPattern()
            ));
        }
        if (groupExists(model, platformGroupName)) {
            platformScope = createScope(platformGroupName);
            newScopes.add(platformScope);
        }
        if (groupExists(model, commerceGroupName)) {
            commerceScope = createScope(commerceGroupName);
            newScopes.add(commerceScope);
        }
        if (platformScope != null && commerceScope != null) {
            hybrisScope = createScopeFor2Groups(platformGroupName, commerceGroupName);
            newScopes.add(hybrisScope);
        }
        if (groupExists(model, nonHybrisGroupName)) {
            newScopes.add(createScope(nonHybrisGroupName));
        }
        newScopes.add(new NamedScope(
            SEARCH_SCOPE_Y_PREFIX + " " + HybrisI18NBundleUtils.message("scope.all.ts.files"),
            new FilePatternPackageSet(null, "*//*-items.xml")
        ));
        ApplicationManager.getApplication().invokeLater(() -> addOrReplaceScopes(project, newScopes));

        NamedScope defaultScope = customScope != null ? customScope : hybrisScope != null ? hybrisScope : platformScope;
        if (defaultScope != null) {
            FindSettings.getInstance().setCustomScope(defaultScope.getName());
            FindSettings.getInstance().setDefaultScopeName(defaultScope.getName());
        }
    }

    @NotNull
    public static FilePatternPackageSet createCustomTSFilesPattern() {
        final String customGroupName = HybrisApplicationSettingsComponent.getInstance().getState().getGroupCustom();
        return new FilePatternPackageSet(SEARCH_SCOPE_GROUP_PREFIX + customGroupName, "*//*-items.xml");
    }

    @NotNull
    public static UnionPackageSet createCustomTsImpexBeansFilesPattern() {
        final String customGroupName = HybrisApplicationSettingsComponent.getInstance().getState().getGroupCustom();
        final FilePatternPackageSet tsFilePatternPackageSet = new FilePatternPackageSet(
            SEARCH_SCOPE_GROUP_PREFIX + customGroupName,
            "*//*-items.xml"
        );
        final FilePatternPackageSet beansFilePatternPackageSet = new FilePatternPackageSet(
            SEARCH_SCOPE_GROUP_PREFIX + customGroupName,
            "*//*-beans.xml"
        );
        final FilePatternPackageSet impexFilePatternPackageSet = new FilePatternPackageSet(
            SEARCH_SCOPE_GROUP_PREFIX + customGroupName,
            "*//*.impex"
        );

        return new UnionPackageSet(
            new UnionPackageSet(tsFilePatternPackageSet, beansFilePatternPackageSet),
            impexFilePatternPackageSet
        );
    }

    private static void addOrReplaceScopes(@NotNull Project project, @NotNull List<NamedScope> newScopes) {
        final Set<String> newScopeNames = newScopes
            .stream()
            .map(NamedScope::getName)
            .collect(Collectors.toSet());

        final NamedScopeManager namedScopeManager = NamedScopeManager.getInstance(project);
        final NamedScope[] existingScopes = namedScopeManager.getEditableScopes();

        final NamedScope[] filteredScopes = Arrays
            .stream(existingScopes)
            .filter(it -> !newScopeNames.contains(it.getName()))
            .toArray(NamedScope[]::new);

        namedScopeManager.setScopes(ArrayUtil.mergeArrays(
            filteredScopes,
            newScopes.toArray(new NamedScope[0])
        ));
    }

    private static boolean groupExists(@NotNull ModifiableModuleModel model, final String groupName) {
        return Arrays
            .stream(model.getModules())
            .map(model::getModuleGroupPath)
            .anyMatch(groupPath -> groupPath != null && groupPath.length > 0 && groupPath[0].equals(groupName));
    }

    @NotNull
    private static NamedScope createScope(@NotNull final String groupName) {
        final FilePatternPackageSet filePatternPackageSet = new FilePatternPackageSet(
            SEARCH_SCOPE_GROUP_PREFIX + groupName,
            "*//*"
        );
        return new NamedScope(SEARCH_SCOPE_Y_PREFIX + " " + groupName, filePatternPackageSet);
    }

    @NotNull
    private static NamedScope createScopeFor2Groups(@NotNull final String firstGroupName, @NotNull final String secondGroupName) {
        final FilePatternPackageSet firstFilePatternPackageSet = new FilePatternPackageSet(
            SEARCH_SCOPE_GROUP_PREFIX + firstGroupName,
            "*//*"
        );
        final FilePatternPackageSet secondFilePatternPackageSet = new FilePatternPackageSet(
            SEARCH_SCOPE_GROUP_PREFIX + secondGroupName,
            "*//*"
        );
        final UnionPackageSet unionPackageSet = new UnionPackageSet(
            firstFilePatternPackageSet,
            secondFilePatternPackageSet
        );
        return new NamedScope(
            SEARCH_SCOPE_Y_PREFIX + " " + firstGroupName + " " + secondGroupName,
            unionPackageSet
        );
    }
}
