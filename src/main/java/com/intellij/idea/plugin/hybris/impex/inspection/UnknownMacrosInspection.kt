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

package com.intellij.idea.plugin.hybris.impex.inspection

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.idea.plugin.hybris.impex.folding.ImpexMacroDescriptor
import com.intellij.idea.plugin.hybris.impex.folding.ImpexMacroUtils
import com.intellij.idea.plugin.hybris.impex.psi.*
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import org.apache.commons.lang3.StringUtils
import java.io.File

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
class UnknownMacrosInspection : LocalInspectionTool() {
    protected val cachedMacros = HashMap<String, Boolean>()
    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevel.ERROR
    }
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = UnknownMacrosVisitor(holder, cachedMacros)
    override fun inspectionStarted(session: LocalInspectionToolSession, isOnTheFly: Boolean) {
        cachedMacros.clear()
        val declarations = PsiTreeUtil.findChildrenOfAnyType(session.file, ImpexMacroDeclaration::class.java)
        declarations.forEach { declaration -> cachedMacros[declaration.firstChild.text] = true }
    }
}

private class UnknownMacrosVisitor(private val problemsHolder: ProblemsHolder, private val cachedMacros: HashMap<String, Boolean>) : ImpexVisitor() {

    override fun visitMacroUsageDec(usage: ImpexMacroUsageDec) {
        if (usage.text.startsWith("\$config-")) return
        val macroName = usage.text

        if (macroName.isNotEmpty()) {
            val isDeclarationExists = cachedMacros[macroName]
            if (isDeclarationExists == true) return
            if (isDeclarationExists != null && isDeclarationExists == false) {
                problemsHolder.registerProblem(usage, "Unknown macro $macroName", ProblemHighlightType.ERROR)
            } else {
                val declaration = findMacrosDeclaration(usage.containingFile, macroName)
                if (declaration == null) {
                    cachedMacros[macroName] = false
                    problemsHolder.registerProblem(usage, "Unknown macro $macroName", ProblemHighlightType.ERROR)
                } else {
                    cachedMacros[macroName] = true
                }
            }
        }

    }

    private fun findMacrosDeclaration(file: PsiFile, macroName: String): ImpexMacroDeclaration? {
        val declarations = PsiTreeUtil.findChildrenOfAnyType(file, ImpexMacroDeclaration::class.java)
        return declarations.find { macroName.startsWith(it.firstChild.text) }
    }

    override fun visitString(o: ImpexString) {
        super.visitString(o)
        resolveIncludeExternalData(o)
    }

    private fun resolveIncludeExternalData(impexString: ImpexString) {
        val text = impexString.text
        var index = text.indexOf("impex.includeExternalData")
        if (index == -1) {
            return
        }
        index = text.indexOf("getResourceAsStream")
        if (index == -1) {
            return
        }
        val startIndex = text.indexOf("(", index)
        if (startIndex == -1) {
            return
        }
        val endIndex = text.indexOf(")", startIndex)
        if (endIndex == -1) {
            return
        }
        var resource = text.substring(startIndex + 1, endIndex)
        resource = StringUtils.strip(resource, "\"' ")
        val directory = impexString.containingFile.containingDirectory.virtualFile
        val dirPath = directory.canonicalPath
        val referencedFile = LocalFileSystem.getInstance().findFileByIoFile(File(dirPath, resource))
        if (referencedFile == null || !referencedFile.exists()) {
            return
        }
        val referencedPsi = PsiManager.getInstance(impexString.project).findFile(referencedFile) as? ImpexFile ?: return
        val declarations = PsiTreeUtil.findChildrenOfAnyType(referencedPsi, ImpexMacroDeclaration::class.java)
        declarations.forEach { declaration -> cachedMacros[declaration.firstChild.text] = true }
        val impString = PsiTreeUtil.findChildrenOfAnyType(referencedPsi, ImpexString::class.java)
        impString.forEach { str -> resolveIncludeExternalData(str) }
    }

    private fun preventRecursion(impexString: ImpexString) {
        val cache = ImpexMacroUtils.getFileCache(impexString.containingFile).value
        if (cache.isEmpty()) {
            cache["!"] = ImpexMacroDescriptor("!", "!", impexString)
        }
    }


}