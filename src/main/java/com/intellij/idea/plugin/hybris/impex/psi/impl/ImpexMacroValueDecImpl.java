// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.impex.psi.impl;

import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroValueDec;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor;
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexMacrosValueMixin;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

public class ImpexMacroValueDecImpl extends ImpexMacrosValueMixin implements ImpexMacroValueDec {

  public ImpexMacroValueDecImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpexVisitor visitor) {
    visitor.visitMacroValueDec(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpexVisitor) accept((ImpexVisitor)visitor);
    else super.accept(visitor);
  }

}
