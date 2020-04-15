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

import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.PsiElementProcessor.CollectFilteredElements;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.isLineBreak;

/**
 * Created 14:28 01 January 2015
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class ImpexFoldingBuilder extends FoldingBuilderEx {

    private static final String GROUP_NAME = "impex";

    private static final FoldingDescriptor[] EMPTY_ARRAY = new FoldingDescriptor[0];

    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(
        @NotNull final PsiElement root,
        @NotNull final Document document,
        final boolean quick
    ) {
        if (this.isFoldingDisabled()) {
            return EMPTY_ARRAY;
        }

        Validate.notNull(root);
        Validate.notNull(document);

        final Collection<PsiElement> psiElements = this.findFoldingBlocksAndLineBreaks(root);

        FoldingGroup currentLineGroup = FoldingGroup.newGroup(GROUP_NAME);

        /* Avoid spawning a lot of unnecessary objects for each line break. */
        boolean groupIsNotFresh = false;

        final List<FoldingDescriptor> descriptors = new ArrayList<>();
        for (final PsiElement psiElement : psiElements) {

            if (isLineBreak(psiElement)) {
                if (groupIsNotFresh) {
                    currentLineGroup = FoldingGroup.newGroup(GROUP_NAME);
                    groupIsNotFresh = false;
                }
            } else {
                descriptors.add(new FoldingDescriptor(psiElement.getNode(), psiElement.getTextRange(), currentLineGroup));
                groupIsNotFresh = true;
            }
        }

        return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
    }


    @Contract(pure = true)
    protected boolean isFoldingDisabled() {
        return !HybrisApplicationSettingsComponent.getInstance().getState().isFoldingEnabled();
    }

    @NotNull
    @Contract(pure = true)
    protected Collection<PsiElement> findFoldingBlocksAndLineBreaks(@Nullable final PsiElement root) {
        if (root == null) {
            return Collections.emptyList();
        }

        final List<PsiElement> foldingBlocks = new ArrayList<>();
        PsiTreeUtil.processElements(root, new CollectFilteredElements<>(
            PsiElementFilterFactory.getPsiElementFilter(), foldingBlocks
        ));

        return foldingBlocks;
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull final ASTNode node) {
        Validate.notNull(node);

        String text = ImpexFoldingPlaceholderBuilderFactory.getPlaceholderBuilder().getPlaceholder(node.getPsi());
        String resolvedMacro = text;
        if (text.startsWith("$")) {
            Map<String, ImpexMacroDescriptor> cache = ImpexMacroUtils.getFileCache(node.getPsi().getContainingFile()).getValue();
            ImpexMacroDescriptor descriptor = cache.get(text);
            if (descriptor != null) {
                resolvedMacro = descriptor.getResolvedValue();
            }
        }
        if (resolvedMacro.length() <= text.length()) {
            return resolvedMacro;
        }
        return text;
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull final ASTNode node) {
        return true;
    }
}
