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

package com.intellij.idea.plugin.hybris.impex.folding;

import com.intellij.idea.plugin.hybris.impex.folding.simple.DefaultFoldingBlocksFilter;
import com.intellij.idea.plugin.hybris.impex.folding.smart.SmartFoldingBlocksFilter;
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.util.PsiElementFilter;

/**
 * Created 22:32 29 March 2015
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public final class PsiElementFilterFactory {

    private PsiElementFilterFactory() throws IllegalAccessException {
        throw new IllegalAccessException("Should never be accessed.");
    }

    public static PsiElementFilter getPsiElementFilter() {
        return isUseSmartFolding()
            ? ServiceManager.getService(SmartFoldingBlocksFilter.class)
            : ServiceManager.getService(DefaultFoldingBlocksFilter.class);
    }

    private static boolean isUseSmartFolding() {
        return HybrisApplicationSettingsComponent.getInstance().getState().isUseSmartFolding();
    }

}
