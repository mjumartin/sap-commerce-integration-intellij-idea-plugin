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

package com.intellij.idea.plugin.hybris.beans;

import com.intellij.idea.plugin.hybris.beans.model.AbstractPojos;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService;
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.openapi.module.Module;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomFileDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class BeansDomFileDescription extends DomFileDescription<AbstractPojos> {

    public BeansDomFileDescription() {
        super(AbstractPojos.class, "beans");
    }

    @Override
    public boolean isMyFile(
        @NotNull final XmlFile file, @Nullable final Module module
    ) {
        if (module != null && !CommonIdeaService.getInstance().isHybrisProject(module.getProject())) {
            return false;
        }

        final boolean isMyFile = super.isMyFile(file, module);
        final boolean isBeansFileName = isBeansXmlFile(file);
        return isMyFile && isBeansFileName;
    }

    @Override
    public boolean hasStubs() {
        return true;
    }

    @Override
    public int getStubVersion() {
        return 6;
    }

    @Nullable
    @Override
    public Icon getFileIcon(final int flags) {
        return HybrisIcons.BEAN_FILE;
    }

    public static boolean isBeansXmlFile(@NotNull final XmlFile file) {
        return file.getName().endsWith(HybrisConstants.HYBRIS_BEANS_XML_FILE_ENDING);
    }


}
