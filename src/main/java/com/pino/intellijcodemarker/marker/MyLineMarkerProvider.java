package com.pino.intellijcodemarker.marker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.lang.LanguageDocumentation;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.Key;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import com.pino.intellijcodemarker.resource.IconResource;
import com.pino.intellijcodemarker.settings.CodeMarkerSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MyLineMarkerProvider implements LineMarkerProvider {

    /** Marks a resolved target that no rule applies to, so the absence of an icon is cached too. */
    private static final String NO_ICON = "";

    private static final Key<com.intellij.psi.util.CachedValue<String>> ICON_NAME_KEY =
            Key.create("com.pino.intellijcodemarker.iconName");

    /**
     * Shared by every provider instance: it is derived from the settings alone. It must stay static
     * so that the cached lookup below never captures a rule list that is about to change.
     */
    private static volatile CompiledRules compiledRules = CompiledRules.EMPTY;

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        try {
            // Markers have to be registered on leaf elements, and this check discards almost
            // every element in the file before anything more expensive runs
            PsiMethodCallExpression methodCall = methodCallOfNameIdentifier(element);
            if (methodCall == null) {
                return null;
            }

            List<CompiledRule> rules = rules();
            if (rules.isEmpty()) {
                return null;
            }

            var method = methodCall.resolveMethod();
            PsiClass containingClass;

            if (method != null) {
                containingClass = method.getContainingClass();
            } else {
                // Handle interface method calls where resolveMethod() returns null
                containingClass = null;
                var qualifierExpression = methodCall.getMethodExpression().getQualifierExpression();
                if (qualifierExpression != null && qualifierExpression.getType() instanceof PsiClassType psiClassType) {
                    containingClass = psiClassType.resolve();
                }
            }

            if (containingClass == null) {
                return null;
            }

            var iconName = cachedIconName(method, containingClass);
            if (iconName != null) {
                return new LineMarkerInfo<>(
                        element,
                        element.getTextRange(),
                        IconResource.loadSvgIcon(iconName),
                        MyLineMarkerProvider::tooltip,
                        this::showPopup,
                        GutterIconRenderer.Alignment.LEFT,
                        () -> "Code Marker"
                );
            }
        } catch (Exception e) {
            // do nothing, just return null
        }
        return null;
    }

    /**
     * @return the call {@code element} is the method name of, or null when it is any other element
     */
    private static PsiMethodCallExpression methodCallOfNameIdentifier(@NotNull PsiElement element) {
        if (!(element instanceof PsiIdentifier)
                || !(element.getParent() instanceof PsiReferenceExpression methodExpression)
                || methodExpression.getReferenceNameElement() != element
                || !(methodExpression.getParent() instanceof PsiMethodCallExpression methodCall)
                || methodCall.getMethodExpression() != methodExpression) {
            return null;
        }
        return methodCall;
    }

    /** Rendering the documentation is expensive, so it only happens once the tooltip is shown. */
    private static String tooltip(PsiElement element) {
        PsiMethodCallExpression methodCall = PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class);
        PsiMethod method = methodCall != null ? methodCall.resolveMethod() : null;
        if (method == null) {
            return "";
        }
        var provider = LanguageDocumentation.INSTANCE.forLanguage(JavaLanguage.INSTANCE);
        return provider == null ? "" : provider.generateDoc(method, method);
    }

    private void showPopup(MouseEvent e, PsiElement elt) {
        // do nothing
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements,
                                       @NotNull Collection<? super LineMarkerInfo<?>> result) {
        // do nothing
    }

    /**
     * The same method is usually called from many places, so the rule lookup is cached per target
     * and thrown away as soon as the code or the rules change.
     */
    private static String cachedIconName(@Nullable PsiMethod method, @NotNull PsiClass containingClass) {
        PsiElement cacheHolder = method != null ? method : containingClass;
        CodeMarkerSettingsState settings = CodeMarkerSettingsState.getInstance();

        // The provider must only capture values that stay the same for this cache holder, which is
        // why it reads the rules itself instead of receiving the list of the current call
        String iconName = CachedValuesManager.getManager(cacheHolder.getProject()).getCachedValue(
                cacheHolder,
                ICON_NAME_KEY,
                () -> CachedValueProvider.Result.create(
                        matchIconName(method, containingClass),
                        PsiModificationTracker.MODIFICATION_COUNT,
                        settings),
                false);

        return NO_ICON.equals(iconName) ? null : iconName;
    }

    private static String matchIconName(@Nullable PsiMethod method, @NotNull PsiClass containingClass) {
        for (CompiledRule rule : rules()) {
            // Every field that is filled in has to match
            if (rule.methodName() != null && (method == null || !method.getName().equals(rule.methodName()))) {
                continue;
            }
            if (rule.className() != null && !RuleMatcher.matchesClassName(containingClass, rule.className())) {
                continue;
            }
            if (rule.annotationName() != null
                    && !RuleMatcher.matchesAnnotation(method, containingClass, rule.annotationName())) {
                continue;
            }
            return rule.iconName();
        }
        return NO_ICON;
    }

    /** Trims and validates the configured rules once per settings change instead of once per call. */
    private static List<CompiledRule> rules() {
        CodeMarkerSettingsState settings = CodeMarkerSettingsState.getInstance();
        long modificationCount = settings.getModificationCount();

        CompiledRules current = compiledRules;
        if (current.modificationCount() != modificationCount) {
            List<CompiledRule> compiled = new ArrayList<>(settings.classIconMappings.size());
            for (CodeMarkerSettingsState.ClassIconMapping mapping : settings.classIconMappings) {
                String className = trimToNull(mapping.getClassName());
                String annotationName = trimToNull(mapping.getAnnotationName());
                // A rule with neither a class name nor an annotation would match every call, so it is dropped
                if (className == null && annotationName == null) {
                    continue;
                }
                compiled.add(new CompiledRule(className, annotationName,
                        trimToNull(mapping.getMethodName()), mapping.getIconName()));
            }
            current = new CompiledRules(modificationCount, List.copyOf(compiled));
            compiledRules = current;
        }
        return current.rules();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record CompiledRule(@Nullable String className, @Nullable String annotationName,
                                @Nullable String methodName, String iconName) {
    }

    private record CompiledRules(long modificationCount, List<CompiledRule> rules) {
        static final CompiledRules EMPTY = new CompiledRules(-1, List.of());
    }
}
