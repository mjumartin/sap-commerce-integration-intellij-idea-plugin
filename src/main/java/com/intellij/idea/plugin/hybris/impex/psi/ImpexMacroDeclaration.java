// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.impex.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ImpexMacroDeclaration extends PsiElement {

  @NotNull
  ImpexMacroNameDec getMacroNameDec();

  @NotNull
  List<ImpexMacroUsageDec> getMacroUsageDecList();

  @NotNull
  List<ImpexMacroValueDec> getMacroValueDecList();

}
