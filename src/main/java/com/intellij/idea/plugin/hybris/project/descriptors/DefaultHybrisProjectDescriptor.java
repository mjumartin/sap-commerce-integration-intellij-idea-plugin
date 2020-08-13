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

package com.intellij.idea.plugin.hybris.project.descriptors;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.project.exceptions.HybrisConfigurationException;
import com.intellij.idea.plugin.hybris.project.services.HybrisProjectService;
import com.intellij.idea.plugin.hybris.project.settings.jaxb.localextensions.ExtensionType;
import com.intellij.idea.plugin.hybris.project.settings.jaxb.localextensions.Hybrisconfig;
import com.intellij.idea.plugin.hybris.project.tasks.TaskProgressProcessor;
import com.intellij.idea.plugin.hybris.project.utils.FileUtils;
import com.intellij.idea.plugin.hybris.project.utils.FindHybrisModuleDescriptorByName;
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettings;
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.THashSet;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.GuardedBy;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.intellij.idea.plugin.hybris.common.utils.CollectionUtils.emptyIfNull;
import static com.intellij.idea.plugin.hybris.project.descriptors.DefaultHybrisProjectDescriptor.DIRECTORY_TYPE.HYBRIS;
import static com.intellij.idea.plugin.hybris.project.descriptors.DefaultHybrisProjectDescriptor.DIRECTORY_TYPE.NON_HYBRIS;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.io.FilenameUtils.separatorsToSystem;

/**
 * Created 3:55 PM 13 June 2015.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class DefaultHybrisProjectDescriptor implements HybrisProjectDescriptor {

    private static final Logger LOG = Logger.getInstance(DefaultHybrisProjectDescriptor.class);
    @NotNull
    protected final List<HybrisModuleDescriptor> foundModules = new ArrayList<>();
    @NotNull
    protected final List<HybrisModuleDescriptor> modulesChosenForImport = new ArrayList<>();
    @NotNull
    @GuardedBy("lock")
    protected final Set<HybrisModuleDescriptor> alreadyOpenedModules = new HashSet<>();
    protected final Lock lock = new ReentrantLock();
    @NotNull
    protected final Set<String> explicitlyDefinedModules = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    @Nullable
    protected Project project;
    @Nullable
    protected File rootDirectory;
    @Nullable
    protected File modulesFilesDirectory;
    @Nullable
    protected File sourceCodeFile;
    protected boolean openProjectSettingsAfterImport;
    protected boolean importOotbModulesInReadOnlyMode;
    @Nullable
    protected File hybrisDistributionDirectory;
    @Nullable
    protected File externalExtensionsDirectory;
    @Nullable
    protected File externalConfigDirectory;
    @Nullable
    protected File externalDbDriversDirectory;
    @Nullable
    protected String javadocUrl;
    @Nullable
    protected String hybrisVersion;
    protected boolean createBackwardCyclicDependenciesForAddOns;
    protected boolean followSymlink;
    protected boolean excludeTestSources;
    protected boolean scanThroughExternalModule;
    @NotNull
    private ConfigHybrisModuleDescriptor configHybrisModuleDescriptor;
    @NotNull
    private PlatformHybrisModuleDescriptor platformHybrisModuleDescriptor;
    private Set<File> vcs = new HashSet<>();

    private void processLocalExtensions() {
        final ConfigHybrisModuleDescriptor configHybrisModuleDescriptor = findConfigDir();
        if (configHybrisModuleDescriptor == null) {
            return;
        }
        final Hybrisconfig hybrisconfig = unmarshalLocalExtensions(configHybrisModuleDescriptor);
        if (hybrisconfig == null) {
            return;
        }
        processHybrisConfig(hybrisconfig);
        preselectModules(configHybrisModuleDescriptor);
    }

    private void preselectModules(@NotNull final ConfigHybrisModuleDescriptor configHybrisModuleDescriptor) {
        Validate.notNull(configHybrisModuleDescriptor);
        for (HybrisModuleDescriptor hybrisModuleDescriptor : foundModules) {
            if (explicitlyDefinedModules.contains(hybrisModuleDescriptor.getName())) {
                hybrisModuleDescriptor.setInLocalExtensions(true);
            }
            if (hybrisModuleDescriptor instanceof PlatformHybrisModuleDescriptor) {
                PlatformHybrisModuleDescriptor platformDescriptor = (PlatformHybrisModuleDescriptor) hybrisModuleDescriptor;
                Set<HybrisModuleDescriptor> dependenciesTree = Sets.newHashSet(platformDescriptor.getDependenciesTree());
                dependenciesTree.add(configHybrisModuleDescriptor);
                platformDescriptor.setDependenciesTree(dependenciesTree);
            }
        }
        preselectConfigModules(configHybrisModuleDescriptor, foundModules);
    }

    private void preselectConfigModules(
        final ConfigHybrisModuleDescriptor configHybrisModuleDescriptor,
        final List<HybrisModuleDescriptor> foundModules
    ) {
        configHybrisModuleDescriptor.setImportStatus(HybrisModuleDescriptor.IMPORT_STATUS.MANDATORY);
        configHybrisModuleDescriptor.setMainConfig(true);
        configHybrisModuleDescriptor.setPreselected(true);
        List<String> preselectedNames = new ArrayList<>();
        preselectedNames.add(configHybrisModuleDescriptor.getName());
        foundModules
            .stream()
            .filter(e -> e instanceof ConfigHybrisModuleDescriptor)
            .map(e -> (ConfigHybrisModuleDescriptor)e)
            .forEach(e->{
                if (!preselectedNames.contains(e.getName())) {
                    e.setPreselected(true);
                    preselectedNames.add(e.getName());
                }
            });
    }

    @Nullable
    private ConfigHybrisModuleDescriptor findConfigDir() {
        final List<ConfigHybrisModuleDescriptor> foundConfigModules = new ArrayList<>();
        PlatformHybrisModuleDescriptor platformHybrisModuleDescriptor = null;
        for (HybrisModuleDescriptor moduleDescriptor : foundModules) {
            if (moduleDescriptor instanceof ConfigHybrisModuleDescriptor) {
                foundConfigModules.add((ConfigHybrisModuleDescriptor) moduleDescriptor);
            }
            if (moduleDescriptor instanceof PlatformHybrisModuleDescriptor) {
                platformHybrisModuleDescriptor = (PlatformHybrisModuleDescriptor) moduleDescriptor;
            }
        }
        if (platformHybrisModuleDescriptor == null) {
            if (foundConfigModules.size() == 1) {
                return foundConfigModules.get(0);
            }
            return null;
        }
        final File configDir;
        if (externalConfigDirectory != null) {
            configDir = externalConfigDirectory;
            if (!configDir.isDirectory()) {
                return null;
            }
        } else {
            configDir = getExpectedConfigDir(platformHybrisModuleDescriptor);
            if (configDir == null || !configDir.isDirectory()) {
                if (foundConfigModules.size() == 1) {
                    return foundConfigModules.get(0);
                }
                return null;
            }
        }
        for (ConfigHybrisModuleDescriptor configHybrisModuleDescriptor : foundConfigModules) {
            if (FileUtil.filesEqual(configHybrisModuleDescriptor.getRootDirectory(), configDir)) {
                return configHybrisModuleDescriptor;
            }
        }
        final HybrisProjectService hybrisProjectService = ServiceManager.getService(HybrisProjectService.class);

        if (hybrisProjectService.isConfigModule(configDir)) {
            try {
                final ConfigHybrisModuleDescriptor configHybrisModuleDescriptor = new ConfigHybrisModuleDescriptor(
                    configDir,
                    platformHybrisModuleDescriptor.getRootProjectDescriptor()
                );
                LOG.info("Creating Overridden Config module in local.properties for " + configDir.getAbsolutePath());
                foundModules.add(configHybrisModuleDescriptor);
                Collections.sort(foundModules);
                return configHybrisModuleDescriptor;
            } catch (HybrisConfigurationException e) {
                // no-op
            }
        }
        return null;
    }

    private File getExpectedConfigDir(final PlatformHybrisModuleDescriptor platformHybrisModuleDescriptor) {
        final File platformDir = platformHybrisModuleDescriptor.getRootDirectory();
        final File expectedConfigDir = new File(platformDir + HybrisConstants.CONFIG_RELATIVE_PATH);
        if (!expectedConfigDir.isDirectory()) {
            return null;
        }
        final File propertiesFile = new File(expectedConfigDir, HybrisConstants.LOCAL_PROPERTIES);
        if (!propertiesFile.exists()) {
            return expectedConfigDir;
        }

        final Properties properties = new Properties();
        try (FileReader fr = new FileReader(propertiesFile)) {
            properties.load(fr);
        } catch (IOException e) {
            return expectedConfigDir;
        }

        String hybrisConfig = (String) properties.get(HybrisConstants.HYBRIS_CONFIG_DIR_KEY);
        if (hybrisConfig == null) {
            return expectedConfigDir;
        }

        hybrisConfig = hybrisConfig.replace(
            HybrisConstants.PLATFORM_HOME_PLACEHOLDER,
            platformHybrisModuleDescriptor.getRootDirectory().getPath()
        );
        hybrisConfig = separatorsToSystem(hybrisConfig);

        final File hybrisConfigDir = new File(hybrisConfig);
        if (hybrisConfigDir.isDirectory()) {
            return hybrisConfigDir;
        }

        return expectedConfigDir;
    }

    private void processHybrisConfig(@NotNull final Hybrisconfig hybrisconfig) {
        final List<ExtensionType> extensionTypeList = hybrisconfig.getExtensions().getExtension();
        for (ExtensionType extensionType : extensionTypeList) {
            final String name = extensionType.getName();
            if (name != null) {
                explicitlyDefinedModules.add(name);
                continue;
            }
            final String dir = extensionType.getDir();
            final int indexSlash = dir.lastIndexOf('/');
            final int indexBack = dir.lastIndexOf('\\');
            final int index = Math.max(indexSlash, indexBack);
            if (index == -1) {
                explicitlyDefinedModules.add(dir);
            } else {
                explicitlyDefinedModules.add(dir.substring(index + 1));
            }
        }
    }

    @Nullable
    private Hybrisconfig unmarshalLocalExtensions(@NotNull final ConfigHybrisModuleDescriptor configHybrisModuleDescriptor) {
        Validate.notNull(configHybrisModuleDescriptor);

        try {
            return unmarshalLocalExtensions(new File(
                configHybrisModuleDescriptor.getRootDirectory(),
                HybrisConstants.LOCAL_EXTENSIONS_XML
            ));
        } catch (JAXBException e) {
            // Log the error because this is called during project import, it is unlikely that the user is typing in
            // "localextensions.xml" right now.
            LOG.error(
                "Can not unmarshal " + configHybrisModuleDescriptor.getRootDirectory().getAbsolutePath(), e
            );
        }

        return null;
    }

    @Nullable
    public static Hybrisconfig unmarshalLocalExtensions(final File localextensions) throws JAXBException {
        if (!localextensions.exists()) {
            return null;
        }

        final JAXBContext jaxbContext = JAXBContext.newInstance(Hybrisconfig.class);
        if (null == jaxbContext) {
            LOG.error(String.format(
                "Can not unmarshal '%s' because JAXBContext has not been created.",
                localextensions.getAbsolutePath()
            ));

            return null;
        }

        final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

        return (Hybrisconfig) jaxbUnmarshaller.unmarshal(localextensions);
    }

    @Override
    public void setProject(@Nullable final Project project) {
        if (project == null) {
            return;
        }
        // the project may not be hybris based project.
        final HybrisProjectSettingsComponent hybrisProjectSettings = HybrisProjectSettingsComponent.getInstance(project);
        if (hybrisProjectSettings == null) {
            return;
        }
        if (hybrisProjectSettings.getState() != null) {
            if (hybrisProjectSettings.getState().isHybrisProject()) {
                setHybrisProject(project);
            }
        }
    }

    protected void scanDirectoryForHybrisModules(
        @NotNull final File rootDirectory,
        @Nullable final TaskProgressProcessor<File> progressListenerProcessor,
        @Nullable final TaskProgressProcessor<List<File>> errorsProcessor
    ) throws InterruptedException, IOException {
        Validate.notNull(rootDirectory);

        this.foundModules.clear();

        final HybrisApplicationSettings settings = HybrisApplicationSettingsComponent.getInstance().getState();

        final Map<DIRECTORY_TYPE, Set<File>> moduleRootMap = newModuleRootMap();
        LOG.info("Scanning for modules");
        this.findModuleRoots(moduleRootMap, false, rootDirectory, progressListenerProcessor);


        if (externalExtensionsDirectory != null && !FileUtils.isFileUnder(externalExtensionsDirectory, rootDirectory)) {
            LOG.info("Scanning for external modules");
            this.findModuleRoots(moduleRootMap, false, externalExtensionsDirectory, progressListenerProcessor);
        }

        if (hybrisDistributionDirectory != null && !FileUtils.isFileUnder(hybrisDistributionDirectory, rootDirectory)) {
            LOG.info("Scanning for hybris modules out of the project");
            this.findModuleRoots(moduleRootMap, false, hybrisDistributionDirectory, progressListenerProcessor);
        }
        Set<File> moduleRootDirectories = processDirectoriesByTypePriority(moduleRootMap, isScanThroughExternalModule(), progressListenerProcessor);

        final List<HybrisModuleDescriptor> moduleDescriptors = new ArrayList<>();
        final List<File> pathsFailedToImport = new ArrayList<>();

        addRootModule(rootDirectory, moduleDescriptors, pathsFailedToImport, settings.isGroupModules());

        final HybrisModuleDescriptorFactory hybrisModuleDescriptorFactory = ServiceManager.getService(
            HybrisModuleDescriptorFactory.class
        );

        for (File moduleRootDirectory : moduleRootDirectories) {
            try {
                final HybrisModuleDescriptor moduleDescriptor = hybrisModuleDescriptorFactory.createDescriptor(
                    moduleRootDirectory, this
                );
                moduleDescriptors.add(moduleDescriptor);
            } catch (HybrisConfigurationException e) {
                LOG.error("Can not import a module using path: " + pathsFailedToImport, e);

                pathsFailedToImport.add(moduleRootDirectory);
            }
        }

        if (null != errorsProcessor) {
            if (errorsProcessor.shouldContinue(pathsFailedToImport)) {
                throw new InterruptedException("Modules scanning has been interrupted.");
            }
        }

        Collections.sort(moduleDescriptors);

        this.buildDependencies(moduleDescriptors);

        this.foundModules.addAll(moduleDescriptors);
    }

    // scan through eclipse module for hybris custom mudules in its subdirectories
    private Set<File> processDirectoriesByTypePriority(
        @NotNull final Map<DIRECTORY_TYPE, Set<File>> moduleRootMap,
        final boolean scanThroughExternalModule,
        @Nullable final TaskProgressProcessor<File> progressListenerProcessor
    ) throws InterruptedException, IOException {
        final Map<String, File> moduleRootDirectories = new HashMap<>();

        moduleRootMap.get(HYBRIS).forEach(file-> addIfNotExists(moduleRootDirectories, file));

        if (scanThroughExternalModule) {
            LOG.info("Scanning for higher priority modules");
            for (final File nonHybrisDir : moduleRootMap.get(NON_HYBRIS)) {
                final Map<DIRECTORY_TYPE, Set<File>> nonHybrisModuleRootMap = newModuleRootMap();
                this.scanSubrirectories(nonHybrisModuleRootMap, true, nonHybrisDir.toPath(), progressListenerProcessor);
                final Set<File> hybrisModuleSet = nonHybrisModuleRootMap.get(HYBRIS);
                if (hybrisModuleSet.isEmpty()) {
                    LOG.info("Confirmed module " + nonHybrisDir);
                    addIfNotExists(moduleRootDirectories, nonHybrisDir);
                } else {
                    LOG.info("Replaced module " + nonHybrisDir);
                    hybrisModuleSet.forEach(file -> addIfNotExists(moduleRootDirectories, file));
                }
            }
        } else {
            moduleRootMap.get(NON_HYBRIS).forEach(file -> addIfNotExists(moduleRootDirectories, file));
        }
        return Sets.newHashSet(moduleRootDirectories.values());
    }

    private void addIfNotExists(final Map<String, File> moduleRootDirectories, final File file) {
        try {
            // this will resolve symlinks
            final String path = file.getCanonicalPath();
            File current = moduleRootDirectories.get(path);
            if (current == null) {
                moduleRootDirectories.put(path, file);
                return;
            }
            if (hybrisDistributionDirectory != null && !FileUtils.isFileUnder(current, hybrisDistributionDirectory)) {
                if (FileUtils.isFileUnder(file, hybrisDistributionDirectory)) {
                    moduleRootDirectories.put(path, file);
                    return;
                }
            }
            if (externalExtensionsDirectory != null && !FileUtils.isFileUnder(current, externalExtensionsDirectory)) {
                if (FileUtils.isFileUnder(file, externalExtensionsDirectory)) {
                    moduleRootDirectories.put(path, file);
                    return;
                }
            }
            if (rootDirectory != null && !FileUtils.isFileUnder(current, rootDirectory)) {
                if (FileUtils.isFileUnder(file, rootDirectory)) {
                    moduleRootDirectories.put(path, file);
                    return;
                }
            }
        } catch (IOException e) {
            LOG.error("Unable to locate "+file.getAbsolutePath());
        }
    }

    private Map<DIRECTORY_TYPE, Set<File>> newModuleRootMap() {
        final Map<DIRECTORY_TYPE, Set<File>> moduleRootMap = new HashMap<>();
        moduleRootMap.put(HYBRIS, new HashSet<>());
        moduleRootMap.put(NON_HYBRIS, new HashSet<>());
        return moduleRootMap;
    }

    private void addRootModule(
        final File rootDirectory, final List<HybrisModuleDescriptor> moduleDescriptors,
        final List<File> pathsFailedToImport,
        final boolean groupModules
    ) {
        if (groupModules) {
            return;
        }
        try {
            moduleDescriptors.add(new RootModuleDescriptor(rootDirectory, this));
        } catch (HybrisConfigurationException e) {
            LOG.error("Can not import a module using path: " + pathsFailedToImport, e);
            pathsFailedToImport.add(rootDirectory);
        }
    }

    @Override
    public void setHybrisProject(@Nullable final Project project) {
        this.project = project;
    }

    private void findModuleRoots(
        @NotNull final Map<DIRECTORY_TYPE, Set<File>> moduleRootMap,
        final boolean acceptOnlyHybrisModules,
        @NotNull final File rootProjectDirectory,
        @Nullable final TaskProgressProcessor<File> progressListenerProcessor
    ) throws InterruptedException, IOException {
        Validate.notNull(moduleRootMap);
        Validate.notNull(rootProjectDirectory);

        if (null != progressListenerProcessor) {
            if (!progressListenerProcessor.shouldContinue(rootProjectDirectory)) {
                LOG.error("Modules scanning has been interrupted.");
                throw new InterruptedException("Modules scanning has been interrupted.");
            }
        }

        final HybrisProjectService hybrisProjectService = ServiceManager.getService(HybrisProjectService.class);

        if (hybrisProjectService.hasVCS(rootProjectDirectory)) {
            LOG.info("Detected version control service " + rootProjectDirectory.getAbsolutePath());
            vcs.add(rootProjectDirectory.getCanonicalFile());
        }

        if (hybrisProjectService.isHybrisModule(rootProjectDirectory)) {
            LOG.info("Detected hybris module " + rootProjectDirectory.getAbsolutePath());
            moduleRootMap.get(HYBRIS).add(rootProjectDirectory);
            return;
        }
        if (hybrisProjectService.isConfigModule(rootProjectDirectory)) {
            LOG.info("Detected config module " + rootProjectDirectory.getAbsolutePath());
            moduleRootMap.get(HYBRIS).add(rootProjectDirectory);
            return;
        }

        if (!acceptOnlyHybrisModules) {
            if (hybrisProjectService.isGradleModule(rootProjectDirectory) && !FileUtil.filesEqual(
                rootProjectDirectory,
                rootDirectory
            )) {
                LOG.info("Detected gradle module " + rootProjectDirectory.getAbsolutePath());
                moduleRootMap.get(NON_HYBRIS).add(rootProjectDirectory);
                return;
            }

            if (hybrisProjectService.isMavenModule(rootProjectDirectory) && !FileUtil.filesEqual(
                rootProjectDirectory,
                rootDirectory
            )) {
                LOG.info("Detected maven module " + rootProjectDirectory.getAbsolutePath());
                moduleRootMap.get(NON_HYBRIS).add(rootProjectDirectory);
                return;
            }

            if (hybrisProjectService.isPlatformModule(rootProjectDirectory)) {
                LOG.info("Detected platform module " + rootProjectDirectory.getAbsolutePath());
                moduleRootMap.get(HYBRIS).add(rootProjectDirectory);
            } else if (hybrisProjectService.isEclipseModule(rootProjectDirectory) && !FileUtil.filesEqual(
                rootProjectDirectory,
                rootDirectory
            )) {
                LOG.info("Detected eclipse module " + rootProjectDirectory.getAbsolutePath());
                moduleRootMap.get(NON_HYBRIS).add(rootProjectDirectory);
                return;
            }
        }

        scanSubrirectories(moduleRootMap, acceptOnlyHybrisModules, rootProjectDirectory.toPath(), progressListenerProcessor);

    }

    private void scanSubrirectories(
        @NotNull final Map<DIRECTORY_TYPE, Set<File>> moduleRootMap,
        final boolean acceptOnlyHybrisModules, @NotNull final Path rootProjectDirectory,
        @Nullable final TaskProgressProcessor<File> progressListenerProcessor
    ) throws InterruptedException, IOException {
        if (!Files.isDirectory(rootProjectDirectory)) {
            return;
        }

        final DirectoryStream<Path> files = Files.newDirectoryStream(rootProjectDirectory, file -> {
            if (file == null) {
                return false;
            }
            if (!Files.isDirectory(file)) {
                return false;
            }
            if (isDirectoryExcluded(file)) {
                return false;
            }
            if (Files.isSymbolicLink(file) && !followSymlink) {
                return false;
            }
            return true;
        });
        if (files != null) {
            for (Path file : files) {
                this.findModuleRoots(moduleRootMap, acceptOnlyHybrisModules, file.toFile(), progressListenerProcessor);
            }
            files.close();
        }
    }

    private boolean isDirectoryExcluded(final Path file) {
        if (file.toString().endsWith(HybrisConstants.EXCLUDE_BOOTSTRAP_DIRECTORY) ||
            file.toString().endsWith(HybrisConstants.EXCLUDE_DATA_DIRECTORY) ||
            file.toString().endsWith(HybrisConstants.EXCLUDE_ECLIPSEBIN_DIRECTORY) ||
            file.toString().endsWith(HybrisConstants.EXCLUDE_GIT_DIRECTORY) ||
            file.toString().endsWith(HybrisConstants.EXCLUDE_IDEA_DIRECTORY) ||
            file.toString().endsWith(HybrisConstants.EXCLUDE_MACOSX_DIRECTORY) ||
            file.toString().endsWith(HybrisConstants.EXCLUDE_IDEA_MODULE_FILES_DIRECTORY) ||
            file.toString().endsWith(HybrisConstants.EXCLUDE_LIB_DIRECTORY) ||
            file.toString().endsWith(HybrisConstants.EXCLUDE_LOG_DIRECTORY) ||
            file.toString().endsWith(HybrisConstants.EXCLUDE_RESOURCES_DIRECTORY) ||
            file.toString().endsWith(HybrisConstants.EXCLUDE_SVN_DIRECTORY) ||
            file.toString().endsWith(HybrisConstants.EXCLUDE_TEMP_DIRECTORY) ||
            file.toString().endsWith(HybrisConstants.EXCLUDE_TOMCAT_DIRECTORY) ||
            file.toString().endsWith(HybrisConstants.EXCLUDE_TOMCAT_6_DIRECTORY) ||
            file.toString().endsWith(HybrisConstants.EXCLUDE_TCSERVER_DIRECTORY) ||
            file.toString().endsWith(HybrisConstants.EXCLUDE_TMP_DIRECTORY)) {
            return true;
        }
        if (file.toString().contains(HybrisConstants.EXCLUDE_ANT_DIRECTORY)) {
            return true;
        }
        return false;
    }

    protected void buildDependencies(@NotNull final Iterable<HybrisModuleDescriptor> moduleDescriptors) {
        Validate.notNull(moduleDescriptors);

        for (HybrisModuleDescriptor moduleDescriptor : moduleDescriptors) {

            final Set<String> requiredExtensionNames = moduleDescriptor.getRequiredExtensionNames();

            if (isEmpty(requiredExtensionNames)) {
                continue;
            }

            final Set<HybrisModuleDescriptor> dependencies = new HashSet<HybrisModuleDescriptor>(
                requiredExtensionNames.size()
            );

            for (String requiresExtensionName : requiredExtensionNames) {

                final Iterable<HybrisModuleDescriptor> dependsOn = this.findHybrisModuleDescriptorsByName(
                    moduleDescriptors, requiresExtensionName
                );

                if (Iterables.isEmpty(dependsOn)) {
                    LOG.warn(String.format(
                        "Module '%s' contains unsatisfied dependency '%s'.",
                        moduleDescriptor.getName(), requiresExtensionName
                    ));
                } else {
                    for (HybrisModuleDescriptor hybrisModuleDescriptor : dependsOn) {
                        dependencies.add(hybrisModuleDescriptor);
                    }
                }
            }

            if (moduleDescriptor.isAddOn()) {
                this.processAddOnBackwardDependencies(moduleDescriptors, moduleDescriptor, dependencies);
            }

            moduleDescriptor.setDependenciesTree(dependencies);
        }
    }

    @Nullable
    @Override
    public Project getProject() {
        return this.project;
    }

    protected void processAddOnBackwardDependencies(
        @NotNull final Iterable<HybrisModuleDescriptor> moduleDescriptors,
        @NotNull final HybrisModuleDescriptor addOn,
        @NotNull final Set<HybrisModuleDescriptor> addOnDependencies
    ) {
        Validate.notNull(moduleDescriptors);
        Validate.notNull(addOn);
        Validate.notNull(addOnDependencies);

        if (isCreateBackwardCyclicDependenciesForAddOn()) {
            for (HybrisModuleDescriptor moduleDescriptor : moduleDescriptors) {
                if (moduleDescriptor.getRequiredExtensionNames().contains(addOn.getName())) {
                    addOnDependencies.add(moduleDescriptor);
                }
            }
        }
    }

    @NotNull
    protected Iterable<HybrisModuleDescriptor> findHybrisModuleDescriptorsByName(
        @NotNull final Iterable<HybrisModuleDescriptor> moduleDescriptors,
        @NotNull final String requiresExtensionName
    ) {
        Validate.notNull(moduleDescriptors);
        Validate.notEmpty(requiresExtensionName);

        return emptyIfNull(Iterables.filter(
            moduleDescriptors, new FindHybrisModuleDescriptorByName(requiresExtensionName)
        ));
    }

    protected enum DIRECTORY_TYPE {HYBRIS, NON_HYBRIS}


    @NotNull
    @Override
    public List<HybrisModuleDescriptor> getFoundModules() {
        return Collections.unmodifiableList(this.foundModules);
    }

    @NotNull
    @Override
    public List<HybrisModuleDescriptor> getModulesChosenForImport() {
        return this.modulesChosenForImport;
    }

    @Override
    public void setModulesChosenForImport(@NotNull final List<HybrisModuleDescriptor> moduleDescriptors) {
        Validate.notNull(moduleDescriptors);

        this.modulesChosenForImport.clear();
        this.modulesChosenForImport.addAll(moduleDescriptors);
        moduleDescriptors.stream().forEach(module -> {
            if (module instanceof ConfigHybrisModuleDescriptor) {
                configHybrisModuleDescriptor = (ConfigHybrisModuleDescriptor) module;
            }
            if (module instanceof PlatformHybrisModuleDescriptor) {
                platformHybrisModuleDescriptor = (PlatformHybrisModuleDescriptor) module;
            }
        });
    }

    @Nullable
    @Override
    public ConfigHybrisModuleDescriptor getConfigHybrisModuleDescriptor() {
        return configHybrisModuleDescriptor;
    }

    @NotNull
    @Override
    public PlatformHybrisModuleDescriptor getPlatformHybrisModuleDescriptor() {
        return platformHybrisModuleDescriptor;
    }

    @NotNull
    @Override
    public Set<HybrisModuleDescriptor> getAlreadyOpenedModules() {
        if (null == getProject()) {
            return Collections.emptySet();
        }

        this.lock.lock();

        try {
            if (this.alreadyOpenedModules.isEmpty()) {
                this.alreadyOpenedModules.addAll(this.getAlreadyOpenedModules(getProject()));
            }
        } finally {
            this.lock.unlock();
        }

        return Collections.unmodifiableSet(this.alreadyOpenedModules);
    }

    @Nullable
    @Override
    public File getRootDirectory() {
        return this.rootDirectory;
    }

    @Override
    public void clear() {
        this.rootDirectory = null;
        this.hybrisDistributionDirectory = null;
        this.externalExtensionsDirectory = null;
        this.externalConfigDirectory = null;
        this.externalDbDriversDirectory = null;
        this.foundModules.clear();
        this.modulesChosenForImport.clear();
        this.explicitlyDefinedModules.clear();
    }

    @Nullable
    @Override
    public File getModulesFilesDirectory() {
        return this.modulesFilesDirectory;
    }

    @Override
    public void setModulesFilesDirectory(@Nullable final File modulesFilesDirectory) {
        this.modulesFilesDirectory = modulesFilesDirectory;
    }

    @Nullable
    @Override
    public File getSourceCodeFile() {
        return sourceCodeFile;
    }

    @Override
    public void setSourceCodeFile(@Nullable final File sourceCodeFile) {
        this.sourceCodeFile = sourceCodeFile;
    }

    @Override
    public void setHybrisVersion(final String hybrisVersion) {
        this.hybrisVersion = hybrisVersion;
    }

    @Override
    public String getHybrisVersion() {
        return hybrisVersion;
    }

    @Override
    public void setRootDirectoryAndScanForModules(
        @NotNull final File rootDirectory,
        @Nullable final TaskProgressProcessor<File> progressListenerProcessor,
        @Nullable final TaskProgressProcessor<List<File>> errorsProcessor
    ) {
        Validate.notNull(rootDirectory);

        this.rootDirectory = rootDirectory;

        try {
            this.scanDirectoryForHybrisModules(rootDirectory, progressListenerProcessor, errorsProcessor);
            this.processLocalExtensions();
        } catch (InterruptedException | IOException e) {
            LOG.warn(e);

            this.rootDirectory = null;
            this.foundModules.clear();
            this.explicitlyDefinedModules.clear();
        }
    }

    @Override
    public boolean isOpenProjectSettingsAfterImport() {
        return this.openProjectSettingsAfterImport;
    }

    @Override
    public void setOpenProjectSettingsAfterImport(final boolean openProjectSettingsAfterImport) {
        this.openProjectSettingsAfterImport = openProjectSettingsAfterImport;
    }

    @Override
    public boolean isImportOotbModulesInReadOnlyMode() {
        return importOotbModulesInReadOnlyMode;
    }

    @Override
    public void setImportOotbModulesInReadOnlyMode(final boolean importOotbModulesInReadOnlyMode) {
        this.importOotbModulesInReadOnlyMode = importOotbModulesInReadOnlyMode;
    }

    @Override
    @Nullable
    public File getHybrisDistributionDirectory() {
        return hybrisDistributionDirectory;
    }

    @Override
    public void setHybrisDistributionDirectory(@Nullable final File hybrisDistributionDirectory) {
        this.hybrisDistributionDirectory = hybrisDistributionDirectory;
    }

    @Override
    @Nullable
    public File getExternalExtensionsDirectory() {
        return externalExtensionsDirectory;
    }

    @Override
    public void setExternalExtensionsDirectory(@Nullable final File externalExtensionsDirectory) {
        this.externalExtensionsDirectory = externalExtensionsDirectory;
    }

    @Override
    @Nullable
    public File getExternalConfigDirectory() {
        return externalConfigDirectory;
    }

    @Override
    public void setExternalConfigDirectory(@Nullable final File externalConfigDirectory) {
        this.externalConfigDirectory = externalConfigDirectory;
    }

    @Override
    @Nullable
    public File getExternalDbDriversDirectory() {
        return externalDbDriversDirectory;
    }

    @Override
    public void setExternalDbDriversDirectory(@Nullable final File externalDbDriversDirectory) {
        this.externalDbDriversDirectory = externalDbDriversDirectory;
    }

    @Nullable
    @Override
    public String getJavadocUrl() {
        return javadocUrl;
    }

    @Override
    public void setJavadocUrl(@Nullable final String javadocUrl) {
        this.javadocUrl = javadocUrl;
    }

    @Override
    public void setCreateBackwardCyclicDependenciesForAddOns(final boolean createBackwardCyclicDependenciesForAddOns) {
        this.createBackwardCyclicDependenciesForAddOns = createBackwardCyclicDependenciesForAddOns;
    }

    @Override
    public boolean isCreateBackwardCyclicDependenciesForAddOn() {
        return createBackwardCyclicDependenciesForAddOns;
    }

    @Override
    public void setFollowSymlink(final boolean followSymlink) {
        this.followSymlink = followSymlink;
    }

    @Override
    public boolean isFollowSymlink() {
        return followSymlink;
    }

    @Override
    public void setExcludeTestSources(final boolean excludeTestSources) {
        this.excludeTestSources = excludeTestSources;
    }

    @Override
    public boolean isExcludeTestSources() {
        return excludeTestSources;
    }

    @Override
    public void setScanThroughExternalModule(final boolean scanThroughExternalModule) {
        this.scanThroughExternalModule = scanThroughExternalModule;
    }

    @Override
    public boolean isScanThroughExternalModule() {
        return scanThroughExternalModule;
    }

    @Override
    public Set<File> getDetectedVcs() {
        return vcs;
    }

    @NotNull
    protected Set<HybrisModuleDescriptor> getAlreadyOpenedModules(@NotNull final Project project) {
        Validate.notNull(project);

        final HybrisModuleDescriptorFactory hybrisModuleDescriptorFactory = ServiceManager.getService(
            HybrisModuleDescriptorFactory.class
        );

        final Set<HybrisModuleDescriptor> existingModules = new THashSet<HybrisModuleDescriptor>();

        for (Module module : ModuleManager.getInstance(project).getModules()) {
            try {
                final VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();

                if (!ArrayUtils.isEmpty(contentRoots)) {
                    existingModules.add(hybrisModuleDescriptorFactory.createDescriptor(
                        VfsUtil.virtualToIoFile(contentRoots[0]), this
                    ));
                }
            } catch (HybrisConfigurationException e) {
                LOG.error(e);
            }
        }

        return existingModules;
    }

    @Override
    public String toString() {
        return "DefaultHybrisProjectDescriptor{" +
               "rootDirectory=" + rootDirectory +
               ", modulesFilesDirectory=" + modulesFilesDirectory +
               ", sourceCodeFile=" + sourceCodeFile +
               ", openProjectSettingsAfterImport=" + openProjectSettingsAfterImport +
               ", importOotbModulesInReadOnlyMode=" + importOotbModulesInReadOnlyMode +
               ", hybrisDistributionDirectory=" + hybrisDistributionDirectory +
               ", externalExtensionsDirectory=" + externalExtensionsDirectory +
               ", externalConfigDirectory=" + externalConfigDirectory +
               ", externalDbDriversDirectory=" + externalDbDriversDirectory +
               ", javadocUrl='" + javadocUrl + '\'' +
               ", hybrisVersion='" + hybrisVersion + '\'' +
               ", createBackwardCyclicDependenciesForAddOns=" + createBackwardCyclicDependenciesForAddOns +
               ", followSymlink=" + followSymlink +
               ", excludeTestSources=" + excludeTestSources +
               ", scanThroughExternalModule=" + scanThroughExternalModule +
               '}';
    }
}
