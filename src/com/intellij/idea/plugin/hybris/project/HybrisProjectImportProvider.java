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

package com.intellij.idea.plugin.hybris.project;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectWizardStepFactory;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.project.wizard.CheckRequiredPluginsStep;
import com.intellij.idea.plugin.hybris.project.wizard.HybrisWorkspaceRootStep;
import com.intellij.idea.plugin.hybris.project.wizard.InformationStep;
import com.intellij.idea.plugin.hybris.project.wizard.SelectHybrisModulesToImportStep;
import com.intellij.idea.plugin.hybris.project.wizard.SelectOtherModulesToImportStep;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.ProjectImportProvider;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created 12:31 AM 10 June 2015
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class HybrisProjectImportProvider extends ProjectImportProvider {

    private final HybrisProjectOpenProcessor myProcessor;

    public HybrisProjectImportProvider(@NotNull final DefaultHybrisProjectImportBuilder builder) {
        super(builder);

        Validate.notNull(builder);

        this.myProcessor = new HybrisProjectOpenProcessor(builder);
    }

    @Override
    protected boolean canImportFromFile(final VirtualFile file) {
        return this.myProcessor.canOpenProject(file);
    }

    @Override
    public ModuleWizardStep[] createSteps(final WizardContext context) {
        final ProjectWizardStepFactory stepFactory = ProjectWizardStepFactory.getInstance();

        return new ModuleWizardStep[]{
            new CheckRequiredPluginsStep(context),
            new InformationStep(context),
            new HybrisWorkspaceRootStep(context),
            new SelectHybrisModulesToImportStep(context),
            new SelectOtherModulesToImportStep(context),
            stepFactory.createProjectJdkStep(context)
        };
    }

    @Nullable
    @Override
    public String getFileSample() {
        return HybrisI18NBundleUtils.message("hybris.project.import.dialog.message");
    }
}
