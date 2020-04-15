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

package com.intellij.idea.plugin.hybris.type.system.meta;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
public interface TSMetaModelAccess {

    static TSMetaModelAccess getInstance(final Project project) {
        return ServiceManager.getService(project, TSMetaModelAccess.class);
    }

    static TSMetaModelAccess getInstance(final DomElement domElement) {
        assert domElement.getXmlElement() != null;
        return getInstance(domElement.getXmlElement().getProject());
    }

    /**
     * Please don't use this method to access TS model from components working on TS XML files.
     * It initiates full rebuilding of the model if any TS XML file was changed since last call.
     * Use getTypeSystemMeta(PsiFile) and getExternalTypeSystemMeta(PsiFile) instead.
     */
    TSMetaModel getTypeSystemMeta();

    /**
     * Optimized version of getTypeSystemMeta() for using from components working on TS XML files.
     * @param contextFile only affects if it is a TS XML file.
     */
    TSMetaModel getTypeSystemMeta(@Nullable PsiFile contextFile);

    /**
     * Returns TS model built from all TS XML files excluding the file passed
     * @param contextFile only affects if it is a TS XML file.
     */
    TSMetaModel getExternalTypeSystemMeta(@NotNull PsiFile contextFile);
}

