// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.impex.psi.impl;

import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderTypeName;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor;
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexHeaderTypeNameMixin;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

public class ImpexHeaderTypeNameImpl extends ImpexHeaderTypeNameMixin implements ImpexHeaderTypeName {

  public ImpexHeaderTypeNameImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpexVisitor visitor) {
    visitor.visitHeaderTypeName(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpexVisitor) accept((ImpexVisitor)visitor);
    else super.accept(visitor);
  }

}
