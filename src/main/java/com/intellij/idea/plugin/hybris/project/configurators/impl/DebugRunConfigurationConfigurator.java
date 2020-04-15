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

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.remote.RemoteConfigurationType;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.project.configurators.HybrisConfiguratorCache;
import com.intellij.idea.plugin.hybris.project.configurators.RunConfigurationConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.AbstractHybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.ConfigHybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.PlatformHybrisModuleDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 17/10/2016.
 */
public class DebugRunConfigurationConfigurator implements RunConfigurationConfigurator {

    @Override
    public void configure(
        @NotNull final HybrisProjectDescriptor hybrisProjectDescriptor,
        @NotNull final Project project,
        @NotNull final HybrisConfiguratorCache cache
    ) {

        final RunManager runManager = RunManager.getInstance(project);
        createRemoteDebug(runManager, hybrisProjectDescriptor, cache);
    }

    private void createRemoteDebug(
        @NotNull final RunManager runManager,
        @NotNull final HybrisProjectDescriptor hybrisProjectDescriptor,
        @NotNull final HybrisConfiguratorCache cache
    ) {
        final RemoteConfigurationType remoteConfigurationType = ConfigurationTypeUtil.findConfigurationType(
            RemoteConfigurationType.class);
        final ConfigurationFactory configurationFactory = remoteConfigurationType.getConfigurationFactories()[0];
        final String configurationName = HybrisI18NBundleUtils.message(
            "hybris.project.import.run.configuration.remote.debug");

        if (runManager.findConfigurationByName(configurationName) != null) {
            return;
        }
        final RunnerAndConfigurationSettings runner = runManager.createRunConfiguration(
            configurationName,
            configurationFactory
        );
        final RemoteConfiguration remoteConfiguration = (RemoteConfiguration) runner.getConfiguration();
        remoteConfiguration.PORT = getDebugPort(hybrisProjectDescriptor, cache);

        ApplicationManager.getApplication().invokeLater(() -> ApplicationManager.getApplication().runWriteAction(() -> {
            runner.setSingleton(true);
            runner.setActivateToolWindowBeforeRun(true);
            runManager.addConfiguration(runner, true);
            runManager.setSelectedConfiguration(runner);
        }));
    }

    private String getDebugPort(
        @NotNull final HybrisProjectDescriptor hybrisProjectDescriptor,
        @NotNull HybrisConfiguratorCache cache
    ) {
        final CommonIdeaService commonIdeaService = ServiceManager.getService(CommonIdeaService.class);
        final ConfigHybrisModuleDescriptor configDescriptor = hybrisProjectDescriptor.getConfigHybrisModuleDescriptor();
        String port = findPortProperty(configDescriptor, HybrisConstants.LOCAL_PROPERTIES, cache);

        if (port != null) {
            return port;
        }
        final PlatformHybrisModuleDescriptor platformDescriptor = commonIdeaService.getPlatformDescriptor(
            hybrisProjectDescriptor);

        if (platformDescriptor != null) {
            port = findPortProperty(platformDescriptor, HybrisConstants.PROJECT_PROPERTIES, cache);

            if (port != null) {
                return port;
            }
        }
        return HybrisConstants.DEBUG_PORT;
    }


    private String findPortProperty(
        final AbstractHybrisModuleDescriptor moduleDescriptor,
        final String fileName,
        HybrisConfiguratorCache cache
    ) {
        if (moduleDescriptor == null) {
            return null;
        }
        final String debugOptions = cache.findPropertyInFile(
            new File(moduleDescriptor.getRootDirectory(), fileName),
            HybrisConstants.TOMCAT_JAVA_DEBUG_OPTIONS
        );
        if (debugOptions == null) {
            return null;
        }
        final Optional<String> transport = Arrays.stream(debugOptions.split(" "))
                                                 .filter(e -> e.startsWith(HybrisConstants.X_RUNJDWP_TRANSPORT))
                                                 .findAny();
        if (!transport.isPresent()) {
            return null;
        }
        final Optional<String> address = Arrays.stream(transport.get().split(","))
                                               .filter(e -> e.startsWith(HybrisConstants.ADDRESS))
                                               .findAny();
        return address.map(s -> s.split("=")[1]).orElse(null);
    }
}
