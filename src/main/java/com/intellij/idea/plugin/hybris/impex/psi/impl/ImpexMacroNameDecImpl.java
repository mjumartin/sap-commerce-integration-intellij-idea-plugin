// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.impex.psi.impl;

import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroNameDec;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexPsiNamedElementImpl;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

public class ImpexMacroNameDecImpl extends ImpexPsiNamedElementImpl implements ImpexMacroNameDec {

  public ImpexMacroNameDecImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpexVisitor visitor) {
    visitor.visitMacroNameDec(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpexVisitor) accept((ImpexVisitor)visitor);
    else super.accept(visitor);
  }

}
