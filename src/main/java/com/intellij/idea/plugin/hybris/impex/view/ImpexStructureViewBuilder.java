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

///**
// * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 6/3/17.
// */
//public class ImpexStructureViewBuilder extends TemplateLanguageStructureViewBuilder implements StructureViewBuilder {
//
//    private final ImpexFile psiFile;
//
//    public ImpexStructureViewBuilder(final ImpexFile psiFile) {
//        super(psiFile);
//        this.psiFile = psiFile;
//    }
//
//    @Override
//    protected @Nullable TreeBasedStructureViewBuilder createMainBuilder(
//        @NotNull final PsiFile psi
//    ) {
//        return this;
//    }
//
//    @Override
//    public @NotNull StructureView createStructureView(
//        final FileEditor fileEditor, @NotNull final Project project
//    ) {
//        final Editor editor = fileEditor instanceof TextEditor ? ((TextEditor) fileEditor).getEditor() : null;
//        StructureViewModel model = new ImpexStructureViewModel(editor, psiFile);
//        StructureView view = new ImpexStructureViewComponent(fileEditor, model, project);
////        StructureViewDescriptor descriptor = new StructureViewDescriptor(psiFile.getName(), view, null);
////        return descriptor;
//
//        return view;
//    }
//
//    @Override
//    public @NotNull StructureViewModel createStructureViewModel(@Nullable final Editor editor) {
//        StructureView view = new ImpexStructureViewComponent(editor, model, project);
//        return super.createStructureViewModel(editor);
//    }
//}
