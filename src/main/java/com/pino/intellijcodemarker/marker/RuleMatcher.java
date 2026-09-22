package com.pino.intellijcodemarker.marker;

import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiModifierListOwner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Matches a called method and its declaring class against a configured rule.
 * <p>
 * Class names and annotation names follow the same two conventions: a name containing a dot is
 * matched against the fully qualified name, any other name against the simple name. Both are
 * looked up on the declaring class itself and on everything it inherits from.
 */
public final class RuleMatcher {

    private RuleMatcher() {
    }

    /**
     * @param className fully qualified name (e.g. {@code com.example.OrderService})
     *                  or simple name (e.g. {@code OrderService})
     */
    public static boolean matchesClassName(@Nullable PsiClass psiClass, String className) {
        String name = normalize(className);
        if (name == null || psiClass == null) {
            return false;
        }
        if (nameMatches(psiClass, name)) {
            return true;
        }
        for (PsiClass superClass : superClassesOf(psiClass)) {
            if (nameMatches(superClass, name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param annotationName fully qualified name (e.g. {@code org.springframework.stereotype.Service})
     *                       or simple name (e.g. {@code Service}), with or without a leading {@code @}
     */
    public static boolean matchesAnnotation(@Nullable PsiMethod method, @Nullable PsiClass containingClass, String annotationName) {
        String name = normalize(annotationName);
        if (name == null) {
            return false;
        }

        if (method != null) {
            if (hasAnnotation(method, name)) {
                return true;
            }
            for (PsiMethod superMethod : superMethodsOf(method)) {
                if (hasAnnotation(superMethod, name)) {
                    return true;
                }
            }
        }

        if (containingClass != null) {
            if (hasAnnotation(containingClass, name)) {
                return true;
            }
            for (PsiClass superClass : superClassesOf(containingClass)) {
                if (hasAnnotation(superClass, name)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static String normalize(String name) {
        if (name == null) {
            return null;
        }
        String normalized = name.trim();
        while (normalized.startsWith("@")) {
            normalized = normalized.substring(1).trim();
        }
        return normalized.isEmpty() ? null : normalized;
    }

    private static boolean nameMatches(@NotNull PsiClass psiClass, @NotNull String name) {
        String qualifiedName = psiClass.getQualifiedName();
        if (isQualified(name)) {
            return name.equals(qualifiedName);
        }
        // Anonymous and local classes have no qualified name, so fall back to the declared name
        return name.equals(qualifiedName != null ? simpleName(qualifiedName) : psiClass.getName());
    }

    private static boolean hasAnnotation(@NotNull PsiModifierListOwner owner, @NotNull String name) {
        PsiModifierList modifierList = owner.getModifierList();
        if (modifierList == null) {
            return false;
        }
        boolean qualified = isQualified(name);
        for (PsiAnnotation annotation : modifierList.getAnnotations()) {
            String qualifiedName = annotation.getQualifiedName();
            if (qualifiedName == null) {
                continue;
            }
            if (qualified ? qualifiedName.equals(name) : simpleName(qualifiedName).equals(name)) {
                return true;
            }
        }
        return false;
    }

    /** A name without a dot is a simple name, so users are not forced to type the package. */
    private static boolean isQualified(@NotNull String name) {
        return name.indexOf('.') >= 0;
    }

    private static String simpleName(@NotNull String qualifiedName) {
        int lastDot = qualifiedName.lastIndexOf('.');
        return lastDot < 0 ? qualifiedName : qualifiedName.substring(lastDot + 1);
    }

    private static Set<PsiMethod> superMethodsOf(@NotNull PsiMethod method) {
        Set<PsiMethod> superMethods = new LinkedHashSet<>();
        Collections.addAll(superMethods, method.findSuperMethods());
        Collections.addAll(superMethods, method.findDeepestSuperMethods());
        superMethods.remove(method);
        return superMethods;
    }

    private static List<PsiClass> superClassesOf(@NotNull PsiClass psiClass) {
        Set<PsiClass> visited = new HashSet<>();
        List<PsiClass> result = new ArrayList<>();
        Deque<PsiClass> queue = new ArrayDeque<>();

        visited.add(psiClass);
        queue.add(psiClass);

        while (!queue.isEmpty()) {
            for (PsiClass superClass : queue.poll().getSupers()) {
                // getSupers() reports java.lang.Object for every class, and broken code can produce
                // cyclic hierarchies, so both are filtered out before walking further up
                if (CommonClassNames.JAVA_LANG_OBJECT.equals(superClass.getQualifiedName()) || !visited.add(superClass)) {
                    continue;
                }
                result.add(superClass);
                queue.add(superClass);
            }
        }
        return result;
    }
}
