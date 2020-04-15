// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.impex.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroDeclaration;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroNameDec;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroUsageDec;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroValueDec;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ImpexMacroDeclarationImpl extends ASTWrapperPsiElement implements ImpexMacroDeclaration {

  public ImpexMacroDeclarationImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpexVisitor visitor) {
    visitor.visitMacroDeclaration(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpexVisitor) accept((ImpexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public ImpexMacroNameDec getMacroNameDec() {
    return findNotNullChildByClass(ImpexMacroNameDec.class);
  }

  @Override
  @NotNull
  public List<ImpexMacroUsageDec> getMacroUsageDecList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpexMacroUsageDec.class);
  }

  @Override
  @NotNull
  public List<ImpexMacroValueDec> getMacroValueDecList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpexMacroValueDec.class);
  }

}
