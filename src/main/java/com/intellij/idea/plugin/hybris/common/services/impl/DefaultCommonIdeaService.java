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

package com.intellij.idea.plugin.hybris.common.services.impl;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.common.Version;
import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.PlatformHybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettings;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.TYPING_EDITOR_ACTIONS;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.UNDO_REDO_EDITOR_ACTIONS;

/**
 * Created 10:24 PM 10 February 2016.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class DefaultCommonIdeaService implements CommonIdeaService {
    private static final Logger LOG = Logger.getInstance(DefaultCommonIdeaService.class);
    private static final Version _1905 = Version.parseVersion("1905.0");

    private final CommandProcessor commandProcessor;


    public DefaultCommonIdeaService(@NotNull final CommandProcessor commandProcessor) {
        Validate.notNull(commandProcessor);

        this.commandProcessor = commandProcessor;
    }

    @Override
    public boolean isTypingActionInProgress() {
        final boolean isTyping = StringUtils.equalsAnyIgnoreCase(
            this.commandProcessor.getCurrentCommandName(), TYPING_EDITOR_ACTIONS
        );

        final boolean isUndoOrRedo = StringUtils.startsWithAny(
            this.commandProcessor.getCurrentCommandName(), UNDO_REDO_EDITOR_ACTIONS
        );

        return isTyping || isUndoOrRedo;
    }

    @Override
    @NotNull
    public Optional<String> getHybrisDirectory(@NotNull final Project project) {
        Validate.notNull(project);

        return Optional.ofNullable(HybrisProjectSettingsComponent.getInstance(project))
                       .map(HybrisProjectSettingsComponent::getState)
                       .map(HybrisProjectSettings::getHybrisDirectory);
    }

    @Override
    @NotNull
    public Optional<String> getCustomDirectory(@NotNull final Project project) {
        Validate.notNull(project);

        return Optional.ofNullable(HybrisProjectSettingsComponent.getInstance(project))
                       .map(HybrisProjectSettingsComponent::getState)
                       .map(HybrisProjectSettings::getCustomDirectory);
    }



    @Override
    public boolean isHybrisProject(@NotNull final Project project) {
        return HybrisProjectSettingsComponent.getInstance(project).getState().isHybrisProject();
    }

    @Override
    public boolean isOutDatedHybrisProject(@NotNull final Project project) {
        final HybrisProjectSettings hybrisProjectSettings = HybrisProjectSettingsComponent.getInstance(project)
                                                                                          .getState();
        final String version = hybrisProjectSettings.getImportedByVersion();
        if (version == null) {
            return true;
        }
        final String[] versionParts = version.split("\\.");
        if (versionParts.length < 2) {
            return true;
        }
        final String majorVersion = versionParts[0];
        final String minorVersion = versionParts[1];
        try {
            final int majorVersionNumber = Integer.parseInt(majorVersion);
            final int minorVersionNumber = Integer.parseInt(minorVersion);
            final int versionNumber = majorVersionNumber * 100 + minorVersionNumber;
            return versionNumber < 900;
        } catch (NumberFormatException nfe) {
            return true;
        }
    }

    @Override
    public boolean isPotentiallyHybrisProject(@NotNull final Project project) {
        final Module[] modules = ModuleManager.getInstance(project).getModules();
        if (modules.length == 0) {
            return false;
        }
        final ArrayList<String> moduleNames = Arrays.stream(modules)
                                                    .map(Module::getName)
                                                    .collect(Collectors.toCollection(ArrayList::new));

        final Collection<String> acceleratorNames = Arrays.asList("*cockpits", "*core", "*facades", "*storefront");
        if (matchAllModuleNames(acceleratorNames, moduleNames)) {
            return true;
        }
        final Collection<String> webservicesNames = Arrays.asList("*hmc", "hmc", "platform");
        return matchAllModuleNames(webservicesNames, moduleNames);
    }

    @Override
    public PlatformHybrisModuleDescriptor getPlatformDescriptor(final HybrisProjectDescriptor hybrisProjectDescriptor) {
        return (PlatformHybrisModuleDescriptor) hybrisProjectDescriptor
            .getFoundModules()
            .stream()
            .filter(e -> e instanceof PlatformHybrisModuleDescriptor)
            .findAny()
            .orElse(null);
    }

    private boolean is2019plus(final Project project) {
        final String hybrisVersion = HybrisProjectSettingsComponent.getInstance(project).getState().getHybrisVersion();

        if (StringUtils.isBlank(hybrisVersion)) {
            return false;
        }
        Version projectVersion = Version.parseVersion(hybrisVersion);
        return projectVersion.compareTo(_1905) >= 0;
    }

    @Override
    public String getBackofficeWebInfLib(final Project project) {
        return is2019plus(project) ? HybrisConstants.BACKOFFICE_WEB_INF_LIB_2019 : HybrisConstants.BACKOFFICE_WEB_INF_LIB;
    }

    @Override
    public String getBackofficeWebInfClasses(final Project project) {
        return is2019plus(project) ? HybrisConstants.BACKOFFICE_WEB_INF_CLASSES_2019 : HybrisConstants.BACKOFFICE_WEB_INF_CLASSES;
    }

    private Properties getLocalProperties(final Project project) {
        final String configDir = HybrisProjectSettingsComponent.getInstance(project).getState().getConfigDirectory();
        if (configDir == null) {
            return null;
        }
        final File propFile = new File(configDir, HybrisConstants.LOCAL_PROPERTIES);
        if (!propFile.exists()) {
            return null;
        }
        final Properties prop = new Properties();
        try (final FileReader fr = new FileReader(propFile)) {
            prop.load(fr);
            return prop;
        } catch (IOException e) {
            LOG.info(e.getMessage(), e);
        }
        return null;
    }

    private boolean matchAllModuleNames(
        @NotNull final Collection<String> namePatterns,
        @NotNull final Collection<String> moduleNames
    ) {
        return namePatterns.stream()
                          .allMatch(pattern -> matchModuleName(pattern, moduleNames));
    }

    private boolean matchModuleName(@NotNull final String pattern, final Collection<String> moduleNames) {
        String regex = ("\\Q" + pattern + "\\E").replace("*", "\\E.*\\Q");
        return moduleNames.stream()
                          .parallel()
                          .anyMatch(p -> p.matches(regex));
    }
}
