package com.pino.intellijcodemarker.marker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.lang.LanguageDocumentation;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;
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
        System.out.println("[DEBUG_LOG] Method name: " + methodName);

        var method = methodCall.resolveMethod();
        if (method == null) {
            return null;
        }

        var project = element.getProject();
        var containingClass = method.getContainingClass();
        var facade = JavaPsiFacade.getInstance(project);

        String iconName = getIconForTargetClass(project, facade, containingClass);
        if (iconName != null) {
            Icon icon = loadIcon(iconName);
            if (icon != null) {
                var provider = LanguageDocumentation.INSTANCE.forLanguage(JavaLanguage.INSTANCE);
                var htmlText = provider.generateDoc(method, method);
                return new LineMarkerInfo<>(
                        element,
                        element.getTextRange(),
                        icon,
                        elt -> htmlText,
                        this::showPopup,
                        GutterIconRenderer.Alignment.LEFT,
                        () -> "Code marker icon"
                );
            }
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

    private String getIconForTargetClass(Project project, JavaPsiFacade facade, PsiClass psiClass) {
        if (psiClass == null) {
            return null;
        }

        CodeMarkerSettingsState settings = CodeMarkerSettingsState.getInstance();

        for (CodeMarkerSettingsState.ClassIconMapping mapping : settings.classIconMappings) {
            if (mapping.getClassName() != null && !mapping.getClassName().isEmpty()) {
                PsiClass targetClass = facade.findClass(mapping.getClassName(), GlobalSearchScope.allScope(project));
                if (targetClass != null && psiClass.isInheritor(targetClass, true)) {
                    return mapping.getIconName();
                }
            }
        }

        return null;
    }

    private Icon loadIcon(String iconName) {
        if (iconName == null || iconName.isEmpty()) {
            return null;
        }

        try {
            // Try to load icon from resources/icons directory
            return IconLoader.getIcon("/icons/" + iconName + ".svg", MyLineMarkerProvider.class);
        } catch (Exception e) {
            // If loading fails, return null
            return null;
        }
    }
}
