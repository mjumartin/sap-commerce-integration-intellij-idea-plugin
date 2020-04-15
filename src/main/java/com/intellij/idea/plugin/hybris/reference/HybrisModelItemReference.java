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

package com.intellij.idea.plugin.hybris.reference;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Nosov Aleksandr
 */
public class HybrisModelItemReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {

    private static final String JAVA_MODEL_SUFFIX = "Model";
    private static final int QUOTE_LENGTH = 2;

    public HybrisModelItemReference(final PsiElement element, final boolean soft) {
        super(element, soft);
    }

    @Override
    public final TextRange getRangeInElement() {
        return TextRange.from(1, getElement().getTextLength() - QUOTE_LENGTH);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(final boolean incompleteCode) {
        final Project project = myElement.getProject();
        final String modelName = ((XmlAttributeValue) myElement).getValue();

        final String javaModelName = modelName + JAVA_MODEL_SUFFIX;
        final String jaloModelName = modelName;

        final PsiClass[] javaModelClasses = PsiShortNamesCache.getInstance(project).getClassesByName(
            javaModelName, GlobalSearchScope.allScope(project)
        );

        final PsiClass[] jaloModelClasses = PsiShortNamesCache.getInstance(project).getClassesByName(
            jaloModelName, GlobalSearchScope.projectScope(project)
        );

        final PsiClass[] psiClasses = ArrayUtil.mergeArrays(javaModelClasses, jaloModelClasses);
        return PsiElementResolveResult.createResults(psiClasses);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        final ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return EMPTY_ARRAY;
    }
}
