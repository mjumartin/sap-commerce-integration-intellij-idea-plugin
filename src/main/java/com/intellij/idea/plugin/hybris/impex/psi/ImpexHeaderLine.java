// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.impex.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ImpexHeaderLine extends PsiElement {

  @NotNull
  ImpexAnyHeaderMode getAnyHeaderMode();

  @NotNull
  List<ImpexFullHeaderParameter> getFullHeaderParameterList();

  @Nullable
  ImpexFullHeaderType getFullHeaderType();

}
