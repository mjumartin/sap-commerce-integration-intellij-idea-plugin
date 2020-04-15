// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.impex.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ImpexParameter extends PsiElement {

  @NotNull
  List<ImpexMacroUsageDec> getMacroUsageDecList();

  @NotNull
  List<ImpexModifiers> getModifiersList();

  @Nullable
  ImpexSubParameters getSubParameters();

}
