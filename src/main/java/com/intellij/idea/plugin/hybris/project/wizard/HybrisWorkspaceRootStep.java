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

package com.intellij.idea.plugin.hybris.project.wizard;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.project.AbstractHybrisProjectImportBuilder;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.idea.plugin.hybris.project.tasks.SearchHybrisDistributionDirectoryTaskModalWindow;
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettings;
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettings;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.ProjectImportWizardStep;
import com.intellij.ui.JBColor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.project.utils.FileUtils.toFile;
import static java.lang.Character.getNumericValue;
import static java.util.Collections.reverse;
import static java.util.Collections.sort;

/**
 * @author Vlad Bozhenok <VladBozhenok@gmail.com>
 */
public class HybrisWorkspaceRootStep extends ProjectImportWizardStep implements NonGuiSupport {

    private static final Logger LOG = Logger.getInstance(HybrisWorkspaceRootStep.class);

    private JPanel rootPanel;
    private TextFieldWithBrowseButton storeModuleFilesInChooser;
    private JCheckBox storeModuleFilesInCheckBox;
    private TextFieldWithBrowseButton sourceCodeFilesInChooser;
    private JTextField projectNameTextField;
    private JCheckBox importOotbModulesInReadOnlyModeCheckBox;
    private TextFieldWithBrowseButton hybrisDistributionDirectoryFilesInChooser;
    private TextFieldWithBrowseButton externalExtensionsDirectoryFilesInChooser;
    private JTextField javadocUrlTextField;
    private JCheckBox sourceCodeCheckBox;
    private JLabel storeModuleFilesInLabel;
    private JPanel directoryOverridePanel;
    private JCheckBox directoryOverrideCheckBox;
    private JLabel directoryOverrideLabel;
    private JLabel sourceCodeLabel;
    private JLabel importOotbModulesInReadOnlyModeLabel;
    private JLabel externalExtensionsPresentLabel;
    private JCheckBox circularDependencyCheckBox;
    private JTextPane circularDependencyIsNeededTextPane;
    private JLabel circularDependencyIsNeededLabel;
    private JCheckBox configOverrideCheckBox;
    private JLabel configOverrideLabel;
    private JPanel configOverridePanel;
    private TextFieldWithBrowseButton configOverrideFilesInChooser;
    private JCheckBox dbDriversDirOverrideCheckBox;
    private JPanel dbDriverDirOverridePanel;
    private JLabel dbDriversDirOverrideLabel;
    private TextFieldWithBrowseButton dbDriversDirOverrideFileChooser;
    private JLabel followSimlinkLabel;
    private JCheckBox followSimplinkCheckbox;
    private JLabel scanThroughExternalModuleLabel;
    private JCheckBox scanThroughExternalModuleCheckbox;
    private JCheckBox excludeTestSourcesCheckBox;
    private JLabel excludeTestSourcesLabel;
    private String hybrisVersion;
    public HybrisWorkspaceRootStep(final WizardContext context) {
        super(context);

        this.storeModuleFilesInChooser.addBrowseFolderListener(
            HybrisI18NBundleUtils.message(
                "hybris.project.import.select.directory.where.new.idea.module.files.will.be.stored"),
            "",
            null,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        );

        this.storeModuleFilesInCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                storeModuleFilesInChooser.setEnabled(((JCheckBox) e.getSource()).isSelected());
            }
        });

        this.storeModuleFilesInLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                storeModuleFilesInCheckBox.doClick();
            }
        });

        this.sourceCodeFilesInChooser.setVisible(false);

        this.sourceCodeCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                sourceCodeFilesInChooser.setVisible(((JCheckBox) e.getSource()).isSelected());
            }
        });

        this.sourceCodeLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                sourceCodeCheckBox.doClick();
            }
        });

        this.importOotbModulesInReadOnlyModeLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                importOotbModulesInReadOnlyModeCheckBox.doClick();
            }
        });

        this.directoryOverridePanel.setVisible(false);

        this.directoryOverrideCheckBox.addActionListener(
            e -> directoryOverridePanel.setVisible(((JCheckBox) e.getSource()).isSelected())
        );

        this.directoryOverrideLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                directoryOverrideCheckBox.doClick();
            }
        });

        this.configOverridePanel.setVisible(false);

        this.configOverrideCheckBox.addActionListener(
            e -> configOverridePanel.setVisible(((JCheckBox) e.getSource()).isSelected())
        );

        this.configOverrideLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                configOverrideCheckBox.doClick();
            }
        });

        this.dbDriverDirOverridePanel.setVisible(false);

        this.dbDriversDirOverrideCheckBox.addActionListener(
            e -> dbDriverDirOverridePanel.setVisible(((JCheckBox) e.getSource()).isSelected())
        );

        this.dbDriversDirOverrideLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                dbDriversDirOverrideCheckBox.doClick();
            }
        });

        this.circularDependencyIsNeededTextPane.setVisible(false);
        this.circularDependencyIsNeededTextPane.setDisabledTextColor(JBColor.RED);
        this.circularDependencyCheckBox.addActionListener(e -> circularDependencyIsNeededTextPane.setVisible(((JCheckBox) e
            .getSource()).isVisible()));
        this.circularDependencyIsNeededLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                circularDependencyCheckBox.doClick();
            }
        });

        sourceCodeFilesInChooser.addActionListener(new MySourceCodeChooserActionListener(
            FileChooserDescriptorFactory.createSingleLocalFileDescriptor()));

        this.hybrisDistributionDirectoryFilesInChooser.addBrowseFolderListener(
            HybrisI18NBundleUtils.message("hybris.import.label.select.hybris.distribution.directory"),
            "",
            null,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        );

        this.externalExtensionsDirectoryFilesInChooser.addBrowseFolderListener(
            HybrisI18NBundleUtils.message("hybris.import.label.select.custom.extensions.directory"),
            "",
            null,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        );

        this.configOverrideFilesInChooser.addBrowseFolderListener(
            HybrisI18NBundleUtils.message("hybris.import.label.select.config.extensions.directory"),
            "",
            null,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        );

        this.dbDriversDirOverrideFileChooser.addBrowseFolderListener(
            HybrisI18NBundleUtils.message("hybris.import.label.select.dbdriver.extensions.directory"),
            "",
            null,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        );
    }

    @Override
    public JComponent getComponent() {
        return this.rootPanel;
    }

    @Override
    public void updateDataModel() {

        this.getContext().cleanup();

        if (this.storeModuleFilesInCheckBox.isSelected()) {
            this.getContext().getHybrisProjectDescriptor().setModulesFilesDirectory(
                toFile(this.storeModuleFilesInChooser.getText())
            );
        }

        this.getContext().getHybrisProjectDescriptor().setImportOotbModulesInReadOnlyMode(
            this.importOotbModulesInReadOnlyModeCheckBox.isSelected()
        );

        this.getContext().getHybrisProjectDescriptor().setFollowSymlink(
            this.followSimplinkCheckbox.isSelected()
        );

        this.getContext().getHybrisProjectDescriptor().setExcludeTestSources(
            this.excludeTestSourcesCheckBox.isSelected()
        );

        this.getContext().getHybrisProjectDescriptor().setScanThroughExternalModule(
            this.scanThroughExternalModuleCheckbox.isSelected()
        );

        this.getContext().getHybrisProjectDescriptor().setSourceCodeFile(
            getValidSourceCode()
        );

        this.getWizardContext().setProjectName(this.projectNameTextField.getText());

        this.getContext().getHybrisProjectDescriptor().setHybrisDistributionDirectory(
            toFile(this.hybrisDistributionDirectoryFilesInChooser.getText())
        );

        final String externalExtensionsDirPath = directoryOverrideCheckBox.isSelected()
            ? externalExtensionsDirectoryFilesInChooser.getText()
            : "";
        this.getContext().getHybrisProjectDescriptor().setExternalExtensionsDirectory(
            externalExtensionsDirPath.isEmpty() ? null : toFile(externalExtensionsDirPath)
        );

        this.getContext().getHybrisProjectDescriptor().setExternalConfigDirectory(
            configOverrideCheckBox.isSelected()
                ? toFile(this.configOverrideFilesInChooser.getText())
                : null
        );

        this.getContext().getHybrisProjectDescriptor().setExternalDbDriversDirectory(
            dbDriversDirOverrideCheckBox.isSelected()
                ? toFile(this.dbDriversDirOverrideFileChooser.getText())
                : null
        );

        this.getContext().getHybrisProjectDescriptor().setCreateBackwardCyclicDependenciesForAddOns(
            this.circularDependencyCheckBox.isSelected()
        );

        this.getContext().getHybrisProjectDescriptor().setJavadocUrl(
            this.javadocUrlTextField.getText()
        );

        this.getContext().getHybrisProjectDescriptor().setHybrisVersion(hybrisVersion);

        LOG.info("importing a project with the following settings: "+this.getContext().getHybrisProjectDescriptor().toString());

        this.getContext().setRootProjectDirectory(toFile(this.getContext().getFileToImport()));
    }

    private File getValidSourceCode() {
        if (!sourceCodeCheckBox.isSelected()) {
            return null;
        }
        File sourceCodeFile = new File(this.sourceCodeFilesInChooser.getText());
        if (!sourceCodeFile.exists()) {
            return null;
        }
        if (sourceCodeFile.isDirectory()) {
            final File hybrisFile = new File(this.hybrisDistributionDirectoryFilesInChooser.getText());
            if (sourceCodeFile.getAbsolutePath().equals(hybrisFile.getAbsolutePath())) {
                return null;
            }
        }
        return sourceCodeFile;
    }

    @Override
    public void updateStep() {
        final HybrisApplicationSettings appSettings = HybrisApplicationSettingsComponent.getInstance().getState();
        this.storeModuleFilesInChooser.setText(
            new File(
                this.getBuilder().getFileToImport(), HybrisConstants.DEFAULT_DIRECTORY_NAME_FOR_IDEA_MODULE_FILES
            ).getAbsolutePath()
        );

        this.projectNameTextField.setText(getWizardContext().getProjectName());

        final HybrisProjectDescriptor hybrisProjectDescriptor = this.getContext().getHybrisProjectDescriptor();

        hybrisProjectDescriptor.setImportOotbModulesInReadOnlyMode(
            appSettings.isDefaultPlatformInReadOnly()
        );
        hybrisProjectDescriptor.setFollowSymlink(
            appSettings.isFollowSymlink()
        );
        hybrisProjectDescriptor.setScanThroughExternalModule(
            appSettings.isScanThroughExternalModule()
        );
        hybrisProjectDescriptor.setExcludeTestSources(
            appSettings.isExcludeTestSources()
        );
        this.importOotbModulesInReadOnlyModeCheckBox.setSelected(
            hybrisProjectDescriptor.isImportOotbModulesInReadOnlyMode()
        );
        this.scanThroughExternalModuleCheckbox.setSelected(
            hybrisProjectDescriptor.isScanThroughExternalModule()
        );
        this.followSimplinkCheckbox.setSelected(
            hybrisProjectDescriptor.isFollowSymlink()
        );
        this.excludeTestSourcesCheckBox.setSelected(
            hybrisProjectDescriptor.isExcludeTestSources()
        );

        if (StringUtils.isBlank(this.hybrisDistributionDirectoryFilesInChooser.getText())) {

            ProgressManager.getInstance().run(new SearchHybrisDistributionDirectoryTaskModalWindow(
                new File(this.getBuilder().getFileToImport()), parameter -> {
                hybrisDistributionDirectoryFilesInChooser.setText(parameter);

                if (StringUtils.isBlank(sourceCodeFilesInChooser.getText())) {
                    sourceCodeFilesInChooser.setText(hybrisDistributionDirectoryFilesInChooser.getText());
                }
            }
            ));
        }

        if (StringUtils.isNotBlank(this.hybrisDistributionDirectoryFilesInChooser.getText())) {

            if (StringUtils.isBlank(this.externalExtensionsDirectoryFilesInChooser.getText())) {

                this.externalExtensionsDirectoryFilesInChooser.setText(
                    new File(
                        this.hybrisDistributionDirectoryFilesInChooser.getText(),
                        HybrisConstants.CUSTOM_MODULES_DIRECTORY_RELATIVE_PATH
                    ).getAbsolutePath()
                );
            }

            if (StringUtils.isBlank(this.configOverrideFilesInChooser.getText())) {
                this.configOverrideFilesInChooser.setText(
                    new File(
                        this.hybrisDistributionDirectoryFilesInChooser.getText(),
                        HybrisConstants.CONFIG_EXTENSION_NAME
                    ).getAbsolutePath()
                );
            }

            if (StringUtils.isBlank(this.dbDriversDirOverrideFileChooser.getText())) {
                String dbDriversDirAbsPath = new File(
                    this.hybrisDistributionDirectoryFilesInChooser.getText(),
                    HybrisConstants.PLATFORM_MODULE_PREFIX + HybrisConstants.PLATFORM_DB_DRIVER
                ).getAbsolutePath();

                if (StringUtils.isNotEmpty(appSettings.getExternalDbDriversDirectory())) {
                    final File dbDriversDir = new File(appSettings.getExternalDbDriversDirectory());
                    if (dbDriversDir.isDirectory()) {
                        dbDriversDirAbsPath = dbDriversDir.getAbsolutePath();
                        if (!dbDriversDirOverrideCheckBox.isSelected()) {
                            dbDriversDirOverrideCheckBox.doClick();
                        }
                    }
                }
                this.dbDriversDirOverrideFileChooser.setText(dbDriversDirAbsPath);
            }

            hybrisVersion = getHybrisVersion(this.hybrisDistributionDirectoryFilesInChooser.getText(), false);
            final String sourceCodeDirectory = appSettings.getSourceCodeDirectory();
            final File sourceFile = appSettings.isSourceZipUsed()
                ? findSourceZip(sourceCodeDirectory, hybrisVersion)
                : new File(sourceCodeDirectory);

            if (sourceFile != null) {
                if (!sourceCodeCheckBox.isSelected()) {
                    sourceCodeCheckBox.doClick();
                }
                sourceCodeFilesInChooser.setText(sourceFile.getPath());
            }

            final String defaultJavadocUrl = getDefaultJavadocUrl(getHybrisVersion(this.hybrisDistributionDirectoryFilesInChooser.getText(), true));
            if (StringUtils.isNotBlank(defaultJavadocUrl)) {
                this.javadocUrlTextField.setText(defaultJavadocUrl);
            }
        }

        if (StringUtils.isBlank(this.sourceCodeFilesInChooser.getText())) {
            sourceCodeFilesInChooser.setText(this.hybrisDistributionDirectoryFilesInChooser.getText());
        }

        circularDependencyCheckBox.setSelected(false);

    }

    private File findSourceZip(final String sourceCodeDir, final String hybrisApiVersion) {
        if (StringUtils.isBlank(sourceCodeDir) || StringUtils.isBlank(hybrisApiVersion)) {
            return null;
        }
        File sourceCodeDirectory = new File(sourceCodeDir);
        if (!sourceCodeDirectory.isDirectory()) {
            return null;
        }
        final List<File> sourceZipList =
            Arrays.stream(sourceCodeDirectory.listFiles())
                  .filter(e -> e.getName().endsWith(".zip"))
                  .filter(e -> e.getName().contains(hybrisApiVersion))
                  .collect(Collectors.toList());
        if (sourceZipList.isEmpty()) {
            return null;
        }
        if (sourceZipList.size() > 1) {
            sort(sourceZipList, new Comparator<File>() {

                @Override
                public int compare(final File zip1, final File zip2) {
                    return getPatch(zip1, hybrisApiVersion).compareTo(getPatch(zip2, hybrisApiVersion));
                }
            });
            reverse(sourceZipList);
        }
        return sourceZipList.get(0);
    }

    private Integer getPatch(@NotNull final File sourceZip, @NotNull final String hybrisApiVersion) {
        final String name = sourceZip.getName();
        final int index = name.indexOf(hybrisApiVersion);
        final char firstDigit = name.charAt(index + hybrisApiVersion.length() + 1);
        final char secondDigit = name.charAt(index + hybrisApiVersion.length() + 2);
        if (Character.isDigit(secondDigit)) {
            return getNumericValue(firstDigit) * 10 + getNumericValue(secondDigit);
        }
        return getNumericValue(firstDigit);
    }

    @Override
    public boolean validate() throws ConfigurationException {
        if (!super.validate()) {
            return false;
        }

        if (this.storeModuleFilesInCheckBox.isSelected()) {
            final File moduleFilesDirectory = new File(this.storeModuleFilesInChooser.getText());
            if (!moduleFilesDirectory.exists()) {
                return moduleFilesDirectory.mkdirs();
            }
        }

        if (directoryOverrideCheckBox.isSelected()) {
            if (!new File(this.externalExtensionsDirectoryFilesInChooser.getText()).isDirectory()) {
                throw new ConfigurationException(
                    HybrisI18NBundleUtils.message(
                        "hybris.import.wizard.validation.custom.extensions.directory.does.not.exist"));
            }
        }
        if (configOverrideCheckBox.isSelected()) {
            if (!new File(this.configOverrideFilesInChooser.getText()).isDirectory()) {
                throw new ConfigurationException(
                    HybrisI18NBundleUtils.message(
                        "hybris.import.wizard.validation.config.directory.does.not.exist"));
            }
        }

        if (dbDriversDirOverrideCheckBox.isSelected()) {
            if (!new File(this.dbDriversDirOverrideFileChooser.getText()).isDirectory()) {
                throw new ConfigurationException(
                    HybrisI18NBundleUtils.message(
                        "hybris.import.wizard.validation.dbdriver.directory.does.not.exist"));
            }
        }

        if (StringUtils.isBlank(this.hybrisDistributionDirectoryFilesInChooser.getText())) {
            throw new ConfigurationException(
                HybrisI18NBundleUtils.message("hybris.import.wizard.validation.hybris.distribution.directory.empty")
            );
        }

        if (!new File(this.hybrisDistributionDirectoryFilesInChooser.getText()).isDirectory()) {
            throw new ConfigurationException(
                HybrisI18NBundleUtils.message(
                    "hybris.import.wizard.validation.hybris.distribution.directory.does.not.exist"));
        }

        return true;
    }

    private AbstractHybrisProjectImportBuilder getContext() {
        return (AbstractHybrisProjectImportBuilder) this.getBuilder();
    }

    @Nullable
    private String getHybrisVersion(@NotNull final String hybrisRootDir, final boolean apiOnly) {
        Validate.notNull(hybrisRootDir);

        final File buildInfoFile = new File(hybrisRootDir + HybrisConstants.BUILD_NUMBER_FILE_PATH);
        final Properties buildProperties = new Properties();

        try (FileInputStream fis = new FileInputStream(buildInfoFile)) {
            buildProperties.load(fis);
        } catch (IOException e) {
            return null;
        }

        if (!apiOnly) {
            String version = buildProperties.getProperty(HybrisConstants.HYBRIS_VERSION_KEY);
            if (version != null) {
                return version;
            }
        }

        return buildProperties.getProperty(HybrisConstants.HYBRIS_API_VERSION_KEY);
    }

    @Nullable
    private String getDefaultJavadocUrl(@Nullable final String hybrisApiVersion) {
        if (StringUtils.isNotEmpty(hybrisApiVersion)) {
            if (hybrisApiVersion.charAt(0) >= '6') {
                return String.format(HybrisConstants.HYBRIS_6_0_PLUS_JAVADOC_ROOT_URL, hybrisApiVersion);
            }
            return String.format(HybrisConstants.DEFAULT_JAVADOC_ROOT_URL, hybrisApiVersion);
        }

        return null;
    }

    @Override
    public void nonGuiModeImport(final HybrisProjectSettings settings) throws ConfigurationException {

        this.getContext().cleanup();

        final HybrisProjectDescriptor hybrisProjectDescriptor = this.getContext().getHybrisProjectDescriptor();

        hybrisProjectDescriptor.setSourceCodeFile(toFile(settings.getSourceCodeFile(), true));
        hybrisProjectDescriptor.setExternalExtensionsDirectory(toFile(settings.getExternalExtensionsDirectory(), true));
        hybrisProjectDescriptor.setExternalConfigDirectory(toFile(settings.getExternalConfigDirectory(), true));
        hybrisProjectDescriptor.setExternalDbDriversDirectory(toFile(settings.getExternalDbDriversDirectory(), true));
        hybrisProjectDescriptor.setCreateBackwardCyclicDependenciesForAddOns(settings.isCreateBackwardCyclicDependenciesForAddOns());
        hybrisProjectDescriptor.setImportOotbModulesInReadOnlyMode(settings.getImportOotbModulesInReadOnlyMode());
        hybrisProjectDescriptor.setFollowSymlink(settings.isFollowSymlink());
        hybrisProjectDescriptor.setExcludeTestSources(settings.isExcludeTestSources());
        hybrisProjectDescriptor.setScanThroughExternalModule(settings.isScanThroughExternalModule());

        final String ideModulesFilesDirectory = settings.getIdeModulesFilesDirectory();
        if (ideModulesFilesDirectory != null) {
            hybrisProjectDescriptor.setModulesFilesDirectory(
                new File(ideModulesFilesDirectory)
            );
        } else {
            hybrisProjectDescriptor.setModulesFilesDirectory(
                new File(
                    this.getBuilder().getFileToImport(),
                    HybrisConstants.DEFAULT_DIRECTORY_NAME_FOR_IDEA_MODULE_FILES
                )
            );
        }

        final String hybrisDirectory = settings.getHybrisDirectory();
        if (hybrisDirectory != null) {
            hybrisProjectDescriptor.setHybrisDistributionDirectory(
                toFile(
                    this.getBuilder().getFileToImport(),
                    settings.getHybrisDirectory()
                )
            );
        }

        hybrisProjectDescriptor.setJavadocUrl(settings.getJavadocUrl());
        hybrisProjectDescriptor.setHybrisVersion(settings.getHybrisVersion());

        this.getContext().setRootProjectDirectory(toFile(this.getBuilder().getFileToImport()));

        if (hybrisDirectory == null) {
            // refreshing a project which was never imported by this plugin
            ProgressManager.getInstance().run(new SearchHybrisDistributionDirectoryTaskModalWindow(
                toFile(this.getBuilder().getFileToImport()), parameter -> {
                hybrisProjectDescriptor.setHybrisDistributionDirectory(toFile(parameter));
            }
            ));
        }

        LOG.info("refreshing a project with the following settings: "+this.getContext().getHybrisProjectDescriptor().toString());
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    private class MySourceCodeChooserActionListener
        extends ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> {

        MySourceCodeChooserActionListener(final FileChooserDescriptor fileChooserDescriptor) {
            super(
                HybrisI18NBundleUtils.message("hybris.import.label.select.hybris.src.file"),
                "",
                HybrisWorkspaceRootStep.this.sourceCodeFilesInChooser,
                (Project) null,
                fileChooserDescriptor,
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
            );
        }

        @NotNull
        @Override
        protected String chosenFileToResultingText(@NotNull final VirtualFile chosenFile) {
            if (chosenFile.isDirectory()) {
                final String hybrisApiVersion = getHybrisVersion(hybrisDistributionDirectoryFilesInChooser.getText(), false);
                final File sourceZip = findSourceZip(chosenFile.getPath(), hybrisApiVersion);

                if (sourceZip != null) {
                    return sourceZip.getAbsolutePath();
                }
            }
            return super.chosenFileToResultingText(chosenFile);
        }
    }

}
