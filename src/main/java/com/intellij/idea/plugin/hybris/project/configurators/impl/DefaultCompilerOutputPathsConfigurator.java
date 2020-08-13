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

import com.intellij.idea.plugin.hybris.project.configurators.CompilerOutputPathsConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.util.PathUtil;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created 12:18 AM 25 June 2015.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class DefaultCompilerOutputPathsConfigurator implements CompilerOutputPathsConfigurator {

    @Override
    public void configure(
        @NotNull final ModifiableRootModel modifiableRootModel,
        @NotNull final HybrisModuleDescriptor moduleDescriptor
    ) {
        Validate.notNull(modifiableRootModel);
        Validate.notNull(moduleDescriptor);

        final CompilerModuleExtension compilerModuleExtension = modifiableRootModel.getModuleExtension(
            CompilerModuleExtension.class
        );

        final File outputDirectory = getOutputDirectory(moduleDescriptor);
        compilerModuleExtension.setCompilerOutputPath(VfsUtilCore.pathToUrl(outputDirectory.getAbsolutePath()));
        compilerModuleExtension.setCompilerOutputPathForTests(VfsUtilCore.pathToUrl(outputDirectory.getAbsolutePath()));

        compilerModuleExtension.setExcludeOutput(true);
        compilerModuleExtension.inheritCompilerOutputPath(false);
    }

    // TODO: There is a problem here. For web modules, ant compiles their non-web src folder
    //   into /classes and the web/src into WEB-INF/classes. IntelliJ will put all of in into
    //   WEB-INF/classes which will cause the ant-compiled stuff to remain behind and be picked up by
    //   the root classloader.
    private File getOutputDirectory(final HybrisModuleDescriptor moduleDescriptor) {
        final boolean isWebModule = moduleDescriptor.getWebRoot() != null;
        final boolean isBackOfficeModule = new File(moduleDescriptor.getRootDirectory(), "backoffice").exists();
        final File outputDirectory;
        final boolean allowWebModuleOutputToWebInf = false;

        if (allowWebModuleOutputToWebInf && isWebModule) {
            outputDirectory = new File(
                moduleDescriptor.getWebRoot(),
                PathUtil.toSystemDependentName("WEB-INF/classes")
            );
        } else if (isBackOfficeModule) {
            outputDirectory = new File(
                moduleDescriptor.getRootDirectory(),
                PathUtil.toSystemDependentName("backoffice/classes")
            );
        } else {
            outputDirectory = new File(moduleDescriptor.getRootDirectory(), "classes");
        }
        return outputDirectory;
    }
}
