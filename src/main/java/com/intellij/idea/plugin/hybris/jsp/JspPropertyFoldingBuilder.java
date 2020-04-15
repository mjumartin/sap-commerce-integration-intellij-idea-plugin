package com.intellij.idea.plugin.hybris.jsp;

import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.jsp.JspFile;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.ObjectUtils;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class JspPropertyFoldingBuilder extends FoldingBuilderEx {

    private static List<Locale.LanguageRange> ourPriorityList = Locale.LanguageRange.parse(
        "en-US;q=1.0,en;q=0.5,de;q=0.1"
    );

    private static final Key<CachedValue<IProperty>> KEY = Key.create(JspPropertyFoldingBuilder.class + ":resolvedProperty");

    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(
        @NotNull final PsiElement root, @NotNull final Document document, final boolean quick
    ) {
        if (quick || !isFoldingEnabled()) {
            return FoldingDescriptor.EMPTY;
        }
        if (!(root instanceof XmlFile)) {
            return FoldingDescriptor.EMPTY;
        }
        if (root instanceof JspFile) {
            JspFile jspFile = (JspFile) root;
            List<FoldingDescriptor> results = new SmartList<>();
            jspFile.accept(new XmlRecursiveElementVisitor(true) {

                @Override
                public void visitXmlAttribute(@NotNull final XmlAttribute attribute) {
                    if (!mayResolveToProperty(attribute)) {
                        return;
                    }
                    XmlAttributeValue value = attribute.getValueElement();
                    if (value != null) {
                        Optional.ofNullable(JspPropertyFoldingBuilder.getResolvedProperty(value))
                                .map(property -> createFolding(value, property))
                                .ifPresent(results::add);
                        super.visitXmlAttribute(attribute);
                    }
                }

                private FoldingDescriptor createFolding(
                    final @NotNull XmlAttributeValue value,
                    final @NotNull IProperty property
                ) {
                    return new FoldingDescriptor(
                        value.getNode(),
                        value.getValueTextRange(),
                        null,
                        Collections.singleton(property)
                    );
                }
            });

            return results.toArray(new FoldingDescriptor[results.size()]);
        }

        return FoldingDescriptor.EMPTY;
    }

    protected boolean mayResolveToProperty(@NotNull final XmlAttribute xmlAttribute) {
        return "code".equals(xmlAttribute.getName());
    }

    @Override
    public String getPlaceholderText(@NotNull ASTNode node) {
        final PsiElement element = node.getPsi();
        if (element instanceof XmlAttributeValue) {
            IProperty property = getResolvedProperty((XmlAttributeValue) element);
            return property == null ? element.getText() : "{" + property.getValue() + "}";
        }
        return element.getText();
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull final ASTNode node) {
        return isFoldingEnabled();
    }

    private boolean isFoldingEnabled() {
        return HybrisApplicationSettingsComponent.getInstance().getState().isFoldingEnabled();
    }

    @Nullable
    private static IProperty getResolvedProperty(@NotNull final XmlAttributeValue codeValue) {
        return CachedValuesManager.getCachedValue(codeValue, KEY, () -> {
            List<IProperty> allProperties = new SmartList<>();
            for (PsiReference nextRef : codeValue.getReferences()) {
                if (nextRef instanceof PsiPolyVariantReference) {
                    Arrays.stream(((PsiPolyVariantReference) nextRef).multiResolve(false))
                          .filter(ResolveResult::isValidResult)
                          .map(ResolveResult::getElement)
                          .map(o -> ObjectUtils.tryCast(o, IProperty.class))
                          .filter(Objects::nonNull)
                          .forEach(allProperties::add);
                } else {
                    Optional.ofNullable(nextRef.resolve())
                            .map(o -> ObjectUtils.tryCast(o, IProperty.class))
                            .ifPresent(allProperties::add);
                }
            }

            IProperty theChosenOne = chooseForLocale(allProperties);
            return new CachedValueProvider.Result<>(theChosenOne, PsiModificationTracker.MODIFICATION_COUNT);
        });
    }

    private static IProperty chooseForLocale(
        final @NotNull List<IProperty> properties
    ) {
        return chooseForLocale(ourPriorityList, properties);
    }

    private static IProperty chooseForLocale(
        final @NotNull List<Locale.LanguageRange> priorityList,
        final @NotNull List<IProperty> properties
    ) {
        if (properties.isEmpty()) {
            return null;
        }
        IProperty first = properties.get(0);
        if (properties.size() == 1) {
            return first;
        }
        final Map<Locale, IProperty> map = new HashMap<>();
        final List<Locale> locales = new LinkedList<>();
        for (IProperty nextProperty : properties) {
            Locale nextLocale = safeGetLocale(nextProperty);
            if (nextLocale != null) {
                map.put(nextLocale, nextProperty);
                locales.add(nextLocale);
            }
        }

        Locale best = Locale.lookup(priorityList, locales);
        //System.err.println("found locales: " + locales + ", best: " + best + ", result: " + map.get(best));
        return Optional.ofNullable(best).map(map::get).orElse(first);
    }

    private static Locale safeGetLocale(final @NotNull IProperty property) {
        try {
            PropertiesFile file = property.getPropertiesFile();
            return file == null ? null : file.getLocale();
        } catch (PsiInvalidElementAccessException e) {
            return null;
        }
    }

}
