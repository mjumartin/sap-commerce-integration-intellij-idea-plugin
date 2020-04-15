// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.impex.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexParameter;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexParameters;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ImpexParametersImpl extends ASTWrapperPsiElement implements ImpexParameters {

  public ImpexParametersImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpexVisitor visitor) {
    visitor.visitParameters(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpexVisitor) accept((ImpexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ImpexParameter> getParameterList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpexParameter.class);
  }

}
