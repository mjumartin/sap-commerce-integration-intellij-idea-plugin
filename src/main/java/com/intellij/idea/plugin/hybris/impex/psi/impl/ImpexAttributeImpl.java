// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.impex.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyAttributeName;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyAttributeValue;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAttribute;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ImpexAttributeImpl extends ASTWrapperPsiElement implements ImpexAttribute {

  public ImpexAttributeImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpexVisitor visitor) {
    visitor.visitAttribute(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpexVisitor) accept((ImpexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public ImpexAnyAttributeName getAnyAttributeName() {
    return findNotNullChildByClass(ImpexAnyAttributeName.class);
  }

  @Override
  @Nullable
  public ImpexAnyAttributeValue getAnyAttributeValue() {
    return findChildByClass(ImpexAnyAttributeValue.class);
  }

}
