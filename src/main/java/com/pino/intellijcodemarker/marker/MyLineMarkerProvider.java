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

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

public class MyLineMarkerProvider implements LineMarkerProvider {

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!(element instanceof PsiMethodCallExpression methodCall)) {
            return null;
        }

        var methodName = methodCall.getMethodExpression().getReferenceName();
        System.out.println("Method Name: " + methodName);

        var method = methodCall.resolveMethod();
        if (method == null) {
            return null;
        }

        var project = element.getProject();
        var containingClass = method.getContainingClass();
        var facade = JavaPsiFacade.getInstance(project);

        String iconName = getTargetClassIcon(project, facade, containingClass);
        if (iconName != null) {
            final Icon icon = IconLoader.getIcon("/icons/" + iconName, MyLineMarkerProvider.class);
            var provider = LanguageDocumentation.INSTANCE.forLanguage(JavaLanguage.INSTANCE);
            var htmlText = provider.generateDoc(method, method);
            return new LineMarkerInfo<>(
                    element,
                    element.getTextRange(),
                    icon,
                    elt -> htmlText,
                    this::showPopup,
                    GutterIconRenderer.Alignment.LEFT,
                    () -> "My icon accessible name"
            );
        }
        return null;
    }

    private void showPopup(MouseEvent e, PsiElement elt){

    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements,
                                       @NotNull Collection<? super LineMarkerInfo<?>> result) {
        // optional
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

            PsiClass targetClass = facade.findClass(mapping.className, GlobalSearchScope.allScope(project));
            if (targetClass != null) {
                // Check if psiClass inherits from targetClass (works for both classes and interfaces)
                if (psiClass.isInheritor(targetClass, true)) {
                    return mapping.iconName;
                }

                // Direct match
                if (psiClass.equals(targetClass)) {
                    return mapping.iconName;
                }
            }
        }

        return null;
    }
}
