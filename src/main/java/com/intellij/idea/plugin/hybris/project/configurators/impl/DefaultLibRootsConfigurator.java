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

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.project.configurators.LibRootsConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.CoreHybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.JavaLibraryDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.OotbHybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.PlatformHybrisModuleDescriptor;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.HYBRIS_PLATFORM_CODE_SERVER_JAR_SUFFIX;

/**
 * Created 11:45 PM 24 June 2015.
 *
 * @author Vlad Bozhenok <VladBozhenok@gmail.com>
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class DefaultLibRootsConfigurator implements LibRootsConfigurator {

    @Override
    public void configure(
        @NotNull final ModifiableRootModel modifiableRootModel,
        @NotNull final HybrisModuleDescriptor moduleDescriptor,
        @NotNull IdeModifiableModelsProvider modifiableModelsProvider
    ) {
        final VirtualFile sourceCodeRoot = this.getSourceCodeRoot(moduleDescriptor);

        for (JavaLibraryDescriptor javaLibraryDescriptor : moduleDescriptor.getLibraryDescriptors()) {
            if (!javaLibraryDescriptor.isValid() && javaLibraryDescriptor.getScope() == DependencyScope.COMPILE) {
                continue;
            }
            if (javaLibraryDescriptor.isDirectoryWithClasses()) {
                this.addClassesToModuleLibs(
                    modifiableRootModel,
                    modifiableModelsProvider,
                    sourceCodeRoot,
                    javaLibraryDescriptor
                );
            } else {
                this.addJarFolderToModuleLibs(
                    modifiableRootModel,
                    modifiableModelsProvider,
                    sourceCodeRoot,
                    javaLibraryDescriptor
                );
            }
        }

        if (moduleDescriptor instanceof PlatformHybrisModuleDescriptor) {
            final PlatformHybrisModuleDescriptor hybrisModuleDescriptor = (PlatformHybrisModuleDescriptor) moduleDescriptor;
            hybrisModuleDescriptor.createBootstrapLib(sourceCodeRoot, modifiableModelsProvider);
        }

        if (moduleDescriptor instanceof CoreHybrisModuleDescriptor) {
            addLibsToModule(
                modifiableRootModel,
                modifiableModelsProvider,
                HybrisConstants.PLATFORM_LIBRARY_GROUP,
                true
            );
        }

        if (moduleDescriptor instanceof OotbHybrisModuleDescriptor) {
            final OotbHybrisModuleDescriptor hybrisModuleDescriptor = (OotbHybrisModuleDescriptor) moduleDescriptor;
            if (hybrisModuleDescriptor.hasBackofficeModule()) {
                final File backofficeJarDirectory = new File(
                    hybrisModuleDescriptor.getRootDirectory(),
                    HybrisConstants.BACKOFFICE_JAR_DIRECTORY
                );
                if (backofficeJarDirectory.exists()) {
                    hybrisModuleDescriptor.createGlobalLibrary(
                        modifiableModelsProvider,
                        backofficeJarDirectory,
                        HybrisConstants.BACKOFFICE_LIBRARY_GROUP
                    );
                }
            }
            if (moduleDescriptor.getName().equals(HybrisConstants.BACK_OFFICE_EXTENSION_NAME)) {
                addLibsToModule(
                    modifiableRootModel,
                    modifiableModelsProvider,
                    HybrisConstants.BACKOFFICE_LIBRARY_GROUP,
                    true
                );
            }
        }
    }

    @Nullable
    private VirtualFile getSourceCodeRoot(final @NotNull HybrisModuleDescriptor moduleDescriptor) {
        final VirtualFile sourceCodeRoot;
        final File sourceCodeFile = moduleDescriptor.getRootProjectDescriptor().getSourceCodeFile();

        if (null != sourceCodeFile) {
            final VirtualFile sourceVFile = VfsUtil.findFileByIoFile(sourceCodeFile, true);
            if (null == sourceVFile) {
                sourceCodeRoot = null;
            } else if (sourceVFile.isDirectory()) {
                sourceCodeRoot = sourceVFile;
            } else {
                sourceCodeRoot = JarFileSystem.getInstance().getJarRootForLocalFile(sourceVFile);
            }
        } else {
            sourceCodeRoot = null;
        }

        return sourceCodeRoot;
    }

    private void addClassesToModuleLibs(
        @NotNull final ModifiableRootModel modifiableRootModel,
        @NotNull final IdeModifiableModelsProvider modifiableModelsProvider,
        @Nullable final VirtualFile sourceCodeRoot,
        @NotNull final JavaLibraryDescriptor javaLibraryDescriptor
    ) {
        final Library library = modifiableRootModel.getModuleLibraryTable().createLibrary();
        final Library.ModifiableModel libraryModifiableModel = modifiableModelsProvider
            .getModifiableLibraryModel(library);
        libraryModifiableModel.addRoot(
            VfsUtil.getUrlForLibraryRoot(javaLibraryDescriptor.getLibraryFile()), OrderRootType.CLASSES
        );

        boolean sourceDirAttached = false;
        if (null != javaLibraryDescriptor.getSourcesFile()) {
            final VirtualFile srcDirVF = VfsUtil.findFileByIoFile(javaLibraryDescriptor.getSourcesFile(), true);
            if (null != srcDirVF) {
                libraryModifiableModel.addRoot(srcDirVF, OrderRootType.SOURCES);
                sourceDirAttached = true;
            }
        }

        if (sourceCodeRoot != null && !sourceDirAttached && javaLibraryDescriptor.getLibraryFile().getName().endsWith(HYBRIS_PLATFORM_CODE_SERVER_JAR_SUFFIX)) {
            libraryModifiableModel.addRoot(sourceCodeRoot, OrderRootType.SOURCES);
        }

        if (javaLibraryDescriptor.isExported()) {
            this.setLibraryEntryExported(modifiableRootModel, library);
        }
        setLibraryEntryScope(modifiableRootModel, library, javaLibraryDescriptor.getScope());
    }

    private void addJarFolderToModuleLibs(
        @NotNull final ModifiableRootModel modifiableRootModel,
        @NotNull final IdeModifiableModelsProvider modifiableModelsProvider,
        @Nullable final VirtualFile sourceCodeRoot,
        @NotNull final JavaLibraryDescriptor javaLibraryDescriptor
    ) {
        final LibraryTable projectLibraryTable = modifiableRootModel.getModuleLibraryTable();

        final Library library = projectLibraryTable.createLibrary();
        final Library.ModifiableModel libraryModifiableModel = modifiableModelsProvider
            .getModifiableLibraryModel(library);

        libraryModifiableModel.addJarDirectory(
            VfsUtil.getUrlForLibraryRoot(javaLibraryDescriptor.getLibraryFile()), true
        );

        if (null != javaLibraryDescriptor.getSourcesFile()) {
            final VirtualFile srcDirVF = VfsUtil.findFileByIoFile(javaLibraryDescriptor.getSourcesFile(), true);
            if (null != srcDirVF) {
                libraryModifiableModel.addRoot(srcDirVF, OrderRootType.SOURCES);
            }
        }

        if (javaLibraryDescriptor.isExported()) {
            this.setLibraryEntryExported(modifiableRootModel, library);
        }
        setLibraryEntryScope(modifiableRootModel, library, javaLibraryDescriptor.getScope());
    }

    private void addLibsToModule(
        @NotNull final ModifiableRootModel modifiableRootModel,
        @NotNull IdeModifiableModelsProvider modifiableModelsProvider,
        @NotNull final String libraryName,
        final boolean export
    ) {
        Validate.notNull(modifiableRootModel);

        final LibraryTable.ModifiableModel libraryTableModifiableModel = modifiableModelsProvider
            .getModifiableProjectLibrariesModel();

        Library library = libraryTableModifiableModel.getLibraryByName(libraryName);

        if (null == library) {
            library = libraryTableModifiableModel.createLibrary(libraryName);
        }
        modifiableRootModel.addLibraryEntry(library);

        if (export) {
            setLibraryEntryExported(modifiableRootModel, library);
        }
    }

    private void setLibraryEntryExported(
        @NotNull final ModifiableRootModel modifiableRootModel,
        @NotNull final Library library
    ) {
        final LibraryOrderEntry libraryOrderEntry = findOrderEntryForLibrary(modifiableRootModel, library);

        if (libraryOrderEntry != null) {
            libraryOrderEntry.setExported(true);
        }
    }

    private void setLibraryEntryScope(
        @NotNull final ModifiableRootModel modifiableRootModel,
        @NotNull final Library library,
        @NotNull DependencyScope scope
    ) {
        final LibraryOrderEntry entry = findOrderEntryForLibrary(modifiableRootModel, library);

        if (entry != null) {
            entry.setScope(scope);
        }
    }

    private static LibraryOrderEntry findOrderEntryForLibrary(
        final @NotNull ModifiableRootModel modifiableRootModel,
        final @NotNull Library library
    ) {
        // Workaround of using Library.equals in findLibraryOrderEntry, which doesn't work here, because all empty libs are equal. Use == instead.
        return (LibraryOrderEntry) Arrays
            .stream(modifiableRootModel.getOrderEntries())
            .filter(entry -> entry instanceof LibraryOrderEntry && ((LibraryOrderEntry) entry).getLibrary() == library)
            .findFirst()
            .orElse(modifiableRootModel.findLibraryOrderEntry(library));
    }
}
