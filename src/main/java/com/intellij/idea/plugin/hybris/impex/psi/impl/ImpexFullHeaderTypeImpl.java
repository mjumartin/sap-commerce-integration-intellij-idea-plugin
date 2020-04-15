// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.impex.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderType;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderTypeName;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexModifiers;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ImpexFullHeaderTypeImpl extends ASTWrapperPsiElement implements ImpexFullHeaderType {

  public ImpexFullHeaderTypeImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpexVisitor visitor) {
    visitor.visitFullHeaderType(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpexVisitor) accept((ImpexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public ImpexHeaderTypeName getHeaderTypeName() {
    return findNotNullChildByClass(ImpexHeaderTypeName.class);
  }

  @Override
  @Nullable
  public ImpexModifiers getModifiers() {
    return findChildByClass(ImpexModifiers.class);
  }

}
