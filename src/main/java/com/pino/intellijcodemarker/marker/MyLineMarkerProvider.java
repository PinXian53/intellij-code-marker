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
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MyLineMarkerProvider implements LineMarkerProvider {



    public MyLineMarkerProvider() {
        // 無參數建構子，確保 Plugin 可以正確實例化
    }

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
        final Icon MY_ICON = IconLoader.getIcon("/icons/call-api.svg", MyLineMarkerProvider.class);
        if (isTagrgetClass(project, facade, containingClass)) {
            var provider = LanguageDocumentation.INSTANCE.forLanguage(JavaLanguage.INSTANCE);
            var htmlText = provider.generateDoc(method, method);
            return new LineMarkerInfo<>(
                    element,
                    element.getTextRange(),
                    MY_ICON,
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

    private boolean isTagrgetClass(Project project, JavaPsiFacade facade, PsiClass psiClass) {
        var target = new ArrayList<PsiClass>();
        target.add(facade.findClass("com.cathay.middleware.repository.EmployeeRepository", GlobalSearchScope.allScope(project)));
        target.add(facade.findClass("org.springframework.data.jpa.repository.JpaRepository", GlobalSearchScope.allScope(project)));
        target.add(facade.findClass("com.cathay.fkba.aply.client.IhrMvpApiClient", GlobalSearchScope.allScope(project)));
        target.add(facade.findClass("com.cathay.fkba.aply.client.IhrCoreApiClient", GlobalSearchScope.allScope(project)));
        target.add(facade.findClass("com.cathay.fkbc.unitmang.client.IhrMvpApiClient", GlobalSearchScope.allScope(project)));
        target.add(facade.findClass("com.cathay.fkbc.unitmang.client.IhrCoreApiClient", GlobalSearchScope.allScope(project)));
        target.add(facade.findClass("com.cathay.fkbd.bumang.client.IhrMvpApiClient", GlobalSearchScope.allScope(project)));
        target.add(facade.findClass("com.cathay.fkbd.bumang.client.IhrCoreApiClient", GlobalSearchScope.allScope(project)));
        target.add(facade.findClass("com.cathay.fkby.config.client.IhrMvpApiClient", GlobalSearchScope.allScope(project)));
        target.add(facade.findClass("com.cathay.fkby.config.client.IhrCoreApiClient", GlobalSearchScope.allScope(project)));
        for (PsiClass baseClass : target) {
            if (baseClass != null && psiClass.isInheritor(baseClass, true)) {
                return true;
            }
        }
        return false;
    }
}
