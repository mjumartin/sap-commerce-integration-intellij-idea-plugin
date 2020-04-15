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

package com.intellij.idea.plugin.hybris.impex.formatting;

import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * Created 12:55 01 January 2015
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class TableAlignmentStrategy extends ColumnsAlignmentStrategy {

    @Override
    @Contract(pure = true)
    protected boolean isNewLine(@Nullable final ASTNode currentNode) {
        return this.isHeaderLine(currentNode) || super.isNewLine(currentNode);
    }

    @Override
    @Contract(pure = true)
    protected boolean isNewColumn(@Nullable final ASTNode currentNode) {
        return null != currentNode && (
            ImpexTypes.PARAMETERS_SEPARATOR == currentNode.getElementType() ||
            super.isNewColumn(currentNode)
        );
    }
}
