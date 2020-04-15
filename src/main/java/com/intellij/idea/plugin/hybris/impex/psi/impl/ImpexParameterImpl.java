// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.impex.psi.impl;

import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroUsageDec;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexModifiers;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexParameter;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexSubParameters;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor;
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexParameterMixin;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ImpexParameterImpl extends ImpexParameterMixin implements ImpexParameter {

  public ImpexParameterImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpexVisitor visitor) {
    visitor.visitParameter(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpexVisitor) accept((ImpexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ImpexMacroUsageDec> getMacroUsageDecList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpexMacroUsageDec.class);
  }

  @Override
  @NotNull
  public List<ImpexModifiers> getModifiersList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpexModifiers.class);
  }

  @Override
  @Nullable
  public ImpexSubParameters getSubParameters() {
    return findChildByClass(ImpexSubParameters.class);
  }

}
