// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.impex.psi.impl;

import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyHeaderParameterName;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroUsageDec;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor;
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexAnyHeaderParameterNameMixin;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ImpexAnyHeaderParameterNameImpl extends ImpexAnyHeaderParameterNameMixin implements ImpexAnyHeaderParameterName {

  public ImpexAnyHeaderParameterNameImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpexVisitor visitor) {
    visitor.visitAnyHeaderParameterName(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpexVisitor) accept((ImpexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ImpexMacroUsageDec getMacroUsageDec() {
    return findChildByClass(ImpexMacroUsageDec.class);
  }

}
