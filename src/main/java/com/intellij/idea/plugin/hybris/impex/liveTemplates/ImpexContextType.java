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

package com.intellij.idea.plugin.hybris.impex.liveTemplates;

import com.intellij.codeInsight.template.FileTypeBasedContextType;
import com.intellij.idea.plugin.hybris.impex.file.ImpexFileType;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * @author Aleksandr Nosov <nosovae.dev@gmail.com>
 */
public class ImpexContextType extends FileTypeBasedContextType {

    protected ImpexContextType() {
        super("IMPEX", "Impex", ImpexFileType.getInstance());
    }

    @Override
    public boolean isInContext(@NotNull final PsiFile file, final int offset) {
        return file instanceof ImpexFile;
    }
}
