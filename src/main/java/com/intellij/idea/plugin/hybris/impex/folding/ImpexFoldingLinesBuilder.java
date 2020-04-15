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

import com.intellij.idea.plugin.hybris.impex.folding.smart.ImpexFoldingLinesFilter;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.getPrevNonWhitespaceElement;
import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.isHeaderLine;
import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.isImpexValueLine;
import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.isUserRightsMacros;
import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.nextElementIsHeaderLine;
import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.nextElementIsUserRightsMacros;

/**
 * @author
 */
public class ImpexFoldingLinesBuilder extends FoldingBuilderEx {

    private static final String LINE_GROUP_NAME = "impex_fold_line";

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

        FoldingGroup currentLineGroup = FoldingGroup.newGroup(LINE_GROUP_NAME);

        final List<PsiElement> foldingBlocks = foldingLines(root);

        PsiElement startGroupElement = foldingBlocks.isEmpty() ? null : foldingBlocks.get(0);

        final List<FoldingDescriptor> descriptors = new ArrayList<>();
         /* Avoid spawning a lot of unnecessary objects for each line break. */
        boolean groupIsNotFresh = false;
        final int size = foldingBlocks.size();
        int countLinesOnGroup = 0;
        for (int i = 0; i < size; i++) {
            final int nextIdx = Math.min(i + 1, size - 1);

            final PsiElement element = foldingBlocks.get(i);
            if (isHeaderLine(element) || isUserRightsMacros(element)) {
                startGroupElement = foldingBlocks.get(nextIdx);
                if (groupIsNotFresh) {
                    currentLineGroup = FoldingGroup.newGroup(LINE_GROUP_NAME);
                    countLinesOnGroup = 0;
                    groupIsNotFresh = false;
                }
            } else {
                if (nextElementIsHeaderLine(element)
                    || nextElementIsUserRightsMacros(element)
                    || nextIdx == size) {
                    if (countLinesOnGroup > 1) {
                        descriptors.add(new ImpexFoldingDescriptor(
                            startGroupElement,
                            startGroupElement.getStartOffsetInParent(),
                            element.getTextRange().getEndOffset(),
                            currentLineGroup,
                            (elm) -> {
                                final PsiElement prevSibling = getPrevNonWhitespaceElement(elm);
                                if (prevSibling != null && (isHeaderLine(prevSibling) || isUserRightsMacros(prevSibling))) {
                                    return ";....;....";
                                }
                                return "";
                            }
                        ));
                    }
                    groupIsNotFresh = true;
                }
            }
            if (isImpexValueLine(element)) {
                countLinesOnGroup++;
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
    protected List<PsiElement> foldingLines(@Nullable final PsiElement root) {
        if (root == null) {
            return Collections.emptyList();
        }

        final List<PsiElement> foldingBlocks = new ArrayList<>();
        PsiTreeUtil.processElements(root, new CollectFilteredElements<>(
            new ImpexFoldingLinesFilter(), foldingBlocks));

        return foldingBlocks;
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull final ASTNode node) {
        Validate.notNull(node);

        return ImpexFoldingPlaceholderBuilderFactory.getPlaceholderBuilder().getPlaceholder(node.getPsi());
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull final ASTNode node) {
        if (Objects.equals(node.getElementType(), ImpexTypes.VALUE_LINE)) {
            return false;
        }
        return true;
    }
}
