/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.intellij.idea.plugin.hybris.impex.psi.references;

import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyHeaderParameterName;
import com.intellij.idea.plugin.hybris.impex.psi.references.result.EnumResolveResult;
import com.intellij.idea.plugin.hybris.psi.references.TypeSystemReferenceBase;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaClass;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaEnum;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModel;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaProperty;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaReference;
import com.intellij.idea.plugin.hybris.type.system.model.Attribute;
import com.intellij.idea.plugin.hybris.type.system.model.EnumType;
import com.intellij.idea.plugin.hybris.type.system.model.RelationElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.CODE_ATTRIBUTE_NAME;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.NAME_ATTRIBUTE_NAME;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.SOURCE_ATTRIBUTE_NAME;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.TARGET_ATTRIBUTE_NAME;
import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.findHeaderItemTypeName;
import static org.apache.commons.collections4.SetUtils.emptyIfNull;


/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
class TypeSystemAttributeReference extends TypeSystemReferenceBase<ImpexAnyHeaderParameterName> {

    public TypeSystemAttributeReference(@NotNull final ImpexAnyHeaderParameterNameMixin owner) {
        super(owner);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(final boolean incompleteCode) {
        final TSMetaModel meta = getTypeSystemMeta();
        final String featureName = getElement().getText().trim();

        List<ResolveResult> result = tryResolveForItemType(meta, featureName);

        if (result == null) {
            result = tryResolveForRelationType(meta, featureName);
        }

        if (result == null) {
            result = tryResolveForEnumType(meta, featureName);
        }

        if (result == null) {
            return ResolveResult.EMPTY_ARRAY;
        }

        return result.toArray(new ResolveResult[0]);
    }

    private List<ResolveResult> tryResolveForEnumType(final TSMetaModel meta, final String featureName) {
        final Optional<TSMetaEnum> metaEnum = findHeaderItemTypeName(getElement()).map(PsiElement::getText)
                                                                                  .map(meta::findMetaEnumByName);
        if (!metaEnum.isPresent()) {
            return null;
        }

        final EnumType enumType = metaEnum.get().retrieveDom();
        if (CODE_ATTRIBUTE_NAME.equals(featureName) || NAME_ATTRIBUTE_NAME.equals(featureName)) {
            final EnumResolveResult resolveResult = new EnumResolveResult(enumType);
            return Collections.singletonList(resolveResult);
        }

        return null;
    }

    private List<ResolveResult> tryResolveForItemType(final TSMetaModel meta, final String featureName) {
        final Optional<TSMetaClass> metaClass = findHeaderItemTypeName(getElement()).map(PsiElement::getText)
                                                                                    .map(meta::findMetaClassByName);
        if (!metaClass.isPresent()) {
            return null;
        }

        final List<ResolveResult> result = metaClass.get()
                                                    .findPropertiesByName(featureName, true)
                                                    .stream()
                                                    .map(TSMetaProperty::retrieveDom)
                                                    .filter(Objects::nonNull)
                                                    .map(AttributeResolveResult::new)
                                                    .collect(Collectors.toCollection(LinkedList::new));

        metaClass.get().findReferenceEndsByRole(featureName, true)
                 .stream()
                 .map(TSMetaReference.ReferenceEnd::retrieveDom)
                 .filter(Objects::nonNull)
                 .map(RelationElementResolveResult::new)
                 .collect(Collectors.toCollection(() -> result));

        return result;
    }

    private List<ResolveResult> tryResolveForRelationType(final TSMetaModel meta, final String featureName) {
        final Optional<List<TSMetaReference>> metaReferences = findHeaderItemTypeName(getElement())
            .map(PsiElement::getText)
            .map(meta::findRelationByName);

        if (!metaReferences.isPresent()) {
            return null;
        }
        final Set<TSMetaReference> references = new HashSet<>(metaReferences.get());
        if (SOURCE_ATTRIBUTE_NAME.equals(featureName)) {
            return emptyIfNull(references).stream()
                                          .map(TSMetaReference::getSource)
                                          .map(TSMetaReference.ReferenceEnd::retrieveDom)
                                          .filter(Objects::nonNull)
                                          .map(RelationElementResolveResult::new)
                                          .collect(Collectors.toList());
        }
        if (TARGET_ATTRIBUTE_NAME.equals(featureName)) {
            return emptyIfNull(references).stream()
                                          .map(TSMetaReference::getTarget)
                                          .map(TSMetaReference.ReferenceEnd::retrieveDom)
                                          .filter(Objects::nonNull)
                                          .map(RelationElementResolveResult::new)
                                          .collect(Collectors.toList());
        }

        return null;
    }

    private static class AttributeResolveResult implements TypeSystemResolveResult {

        private final Attribute myDomAttribute;

        public AttributeResolveResult(@NotNull final Attribute domAttribute) {
            myDomAttribute = domAttribute;
        }

        @Nullable
        @Override
        public PsiElement getElement() {
            return myDomAttribute.getQualifier().getXmlAttributeValue();
        }

        @Override
        public boolean isValidResult() {
            return getElement() != null;
        }

        @NotNull
        @Override
        public DomElement getSemanticDomElement() {
            return myDomAttribute;
        }
    }

    private static class RelationElementResolveResult implements TypeSystemResolveResult {

        @NotNull
        private final RelationElement myDomRelationEnd;

        public RelationElementResolveResult(@NotNull final RelationElement domRelationEnd) {
            myDomRelationEnd = domRelationEnd;
        }

        @Nullable
        @Override
        public PsiElement getElement() {
            return myDomRelationEnd.getQualifier().getXmlAttributeValue();
        }

        @Override
        public boolean isValidResult() {
            return getElement() != null;
        }

        @NotNull
        @Override
        public DomElement getSemanticDomElement() {
            return myDomRelationEnd;
        }
    }

}
