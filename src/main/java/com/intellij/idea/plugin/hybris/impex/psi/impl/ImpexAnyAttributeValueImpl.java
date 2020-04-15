// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.impex.psi.impl;

import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyAttributeValue;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroUsageDec;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexString;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor;
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexAttributeValueMixin;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ImpexAnyAttributeValueImpl extends ImpexAttributeValueMixin implements ImpexAnyAttributeValue {

  public ImpexAnyAttributeValueImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpexVisitor visitor) {
    visitor.visitAnyAttributeValue(this);
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
  public List<ImpexString> getStringList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpexString.class);
  }

}
