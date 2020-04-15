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

package com.intellij.idea.plugin.hybris.impex.view;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyHeaderMode;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderType;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderLine;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 7/3/17.
 */
public class ImpexHeaderTreeElement extends PsiTreeElementBase<ImpexHeaderLine> implements ItemPresentation {

    protected ImpexHeaderTreeElement(final ImpexHeaderLine psiElement) {
        super(psiElement);
    }

    @NotNull
    @Override
    public Collection<StructureViewTreeElement> getChildrenBase() {
        return getElement().getFullHeaderParameterList()
                           .stream()
                           .map(e -> new ImpexHeaderParameterElement(e))
                           .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public String getPresentableText() {
        final ImpexFullHeaderType fullHeaderType = getElement().getFullHeaderType();
        if (fullHeaderType == null) {
            return null;
        }

        return fullHeaderType.getText();
    }

    @Nullable
    @Override
    public String getLocationString() {
        final ImpexAnyHeaderMode mode = getElement().getAnyHeaderMode();
        if (mode == null) {
            return null;
        }
        return mode.getText();
    }

    @Override
    @Nullable
    public Icon getIcon(boolean unused) {
        return null;
    }
}
