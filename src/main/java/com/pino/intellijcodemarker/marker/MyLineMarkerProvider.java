package com.pino.intellijcodemarker.marker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.lang.LanguageDocumentation;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.pino.intellijcodemarker.settings.CodeMarkerSettingsState;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

public class MyLineMarkerProvider implements LineMarkerProvider {

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        try {
            if (!(element instanceof PsiMethodCallExpression methodCall)) {
                return null;
            }

            var method = methodCall.resolveMethod();
            var project = element.getProject();
            PsiClass containingClass = null;

            if (method != null) {
                containingClass = method.getContainingClass();
            } else {
                // Handle interface method calls where resolveMethod() returns null
                var qualifierExpression = methodCall.getMethodExpression().getQualifierExpression();
                if (qualifierExpression != null) {
                    var qualifierType = qualifierExpression.getType();
                    if (qualifierType instanceof PsiClassType psiClassType) {
                        containingClass = psiClassType.resolve();
                    }
                }
            }

            if (containingClass == null) {
                return null;
            }

            var facade = JavaPsiFacade.getInstance(project);
            var iconName = getTargetClassIcon(project, facade, containingClass);
            if (iconName != null) {
                var icon = IconLoader.getIcon("/icons/" + iconName, MyLineMarkerProvider.class);
                var provider = LanguageDocumentation.INSTANCE.forLanguage(JavaLanguage.INSTANCE);
                String htmlText;
                if (method != null) {
                    htmlText = provider.generateDoc(method, method);
                } else {
                    htmlText = "";
                }
                return new LineMarkerInfo<>(
                        element,
                        element.getTextRange(),
                        icon,
                        elt -> htmlText,
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

    private void showPopup(MouseEvent e, PsiElement elt) {
        // do nothing
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements,
                                       @NotNull Collection<? super LineMarkerInfo<?>> result) {
        // do nothing
    }

    private String getTargetClassIcon(Project project, JavaPsiFacade facade, PsiClass psiClass) {
        if (psiClass == null) {
            return null;
        }

        CodeMarkerSettingsState settings = CodeMarkerSettingsState.getInstance();

        for (CodeMarkerSettingsState.ClassIconMapping mapping : settings.classIconMappings) {
            if (mapping.className == null || mapping.className.isEmpty()) {
                continue;
            }

            PsiClass targetClass = facade.findClass(mapping.className.trim(), GlobalSearchScope.allScope(project));
            if (targetClass != null) {
                // Direct match
                if (psiClass.equals(targetClass)) {
                    return mapping.iconName;
                }

                // Check if psiClass inherits from targetClass (works for both classes and interfaces)
                if (psiClass.isInheritor(targetClass, true)) {
                    return mapping.iconName;
                }
            }
        }
        return null;
    }
}
