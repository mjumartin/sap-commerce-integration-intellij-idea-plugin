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
package com.intellij.idea.plugin.hybris.impex.psi.references;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyHeaderParameterName;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
public abstract class ImpexAnyHeaderParameterNameMixin extends ASTWrapperPsiElement implements
                                                                                    ImpexAnyHeaderParameterName {

    private TypeSystemAttributeReference myReference;

    public ImpexAnyHeaderParameterNameMixin(@NotNull final ASTNode astNode) {
        super(astNode);
    }

    @NotNull
    @Override
    public final PsiReference[] getReferences() {
        final IElementType leafType = Optional.ofNullable(getFirstChild())
                                              .map(PsiElement::getNode)
                                              .map(ASTNode::getElementType)
                                              .orElse(null);

        if (ImpexTypes.MACRO_USAGE.equals(leafType)) {
            return new PsiReference[]{new ImpexMacrosReferenceBase(this)};
        }

        if (ImpexTypes.DOCUMENT_ID.equals(leafType)) {
            return new PsiReference[]{new ImpexDocumentIdReference(this)};
        }

        //optimisation: dont even try for macro's and documents
        if (!ImpexTypes.HEADER_PARAMETER_NAME.equals(leafType) && 
            !ImpexTypes.FUNCTION.equals(leafType)) {
            return PsiReference.EMPTY_ARRAY;
        }
        if (myReference == null) {
            myReference = new TypeSystemAttributeReference(this);
        }
        return new PsiReference[]{myReference};
    }

    @Override
    protected Object clone() {
        final ImpexAnyHeaderParameterNameMixin result = (ImpexAnyHeaderParameterNameMixin) super.clone();
        result.myReference = null;
        return result;
    }
}
