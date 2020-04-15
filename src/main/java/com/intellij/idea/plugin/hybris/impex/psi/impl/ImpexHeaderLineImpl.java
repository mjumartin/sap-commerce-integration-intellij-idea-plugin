// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.impex.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyHeaderMode;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderParameter;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderType;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderLine;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ImpexHeaderLineImpl extends ASTWrapperPsiElement implements ImpexHeaderLine {

  public ImpexHeaderLineImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpexVisitor visitor) {
    visitor.visitHeaderLine(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpexVisitor) accept((ImpexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public ImpexAnyHeaderMode getAnyHeaderMode() {
    return findNotNullChildByClass(ImpexAnyHeaderMode.class);
  }

  @Override
  @NotNull
  public List<ImpexFullHeaderParameter> getFullHeaderParameterList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpexFullHeaderParameter.class);
  }

  @Override
  @Nullable
  public ImpexFullHeaderType getFullHeaderType() {
    return findChildByClass(ImpexFullHeaderType.class);
  }

}
