package com.pino.intellijcodemarker.marker;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

public class RuleMatcherTest extends LightJavaCodeInsightFixtureTestCase {

    private static final String MARKED = "demo.Marked";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.addClass("package demo; public @interface Marked {}");
        myFixture.addClass("package demo; public class Plain { public void run() {} }");
        myFixture.addClass("package demo; public class Direct { @Marked public void run() {} }");
        myFixture.addClass("package demo; @Marked public class AnnotatedClass { public void run() {} }");
        myFixture.addClass("package demo; public interface Api { @Marked void run(); }");
        myFixture.addClass("package demo; public class Impl implements Api { public void run() {} }");
        myFixture.addClass("package demo; @Marked public class Base { public void run() {} }");
        myFixture.addClass("package demo; public class Child extends Base {}");
        myFixture.addClass("package other; public class Plain { public void run() {} }");
    }

    // --- annotations ---

    public void testAnnotationOnMethod() {
        assertTrue(matchesAnnotation("demo.Direct", "run", MARKED));
    }

    public void testAnnotationOnInterfaceMethodButNotOnImplementation() {
        assertTrue(matchesAnnotation("demo.Impl", "run", MARKED));
    }

    public void testAnnotationOnClass() {
        assertTrue(matchesAnnotation("demo.AnnotatedClass", "run", MARKED));
    }

    public void testAnnotationOnSuperClass() {
        assertTrue(matchesAnnotation("demo.Child", "run", MARKED));
    }

    public void testAnnotationAcceptsSimpleNameAndLeadingAtSign() {
        assertTrue(matchesAnnotation("demo.Direct", "run", "Marked"));
        assertTrue(matchesAnnotation("demo.Direct", "run", "@Marked"));
        assertTrue(matchesAnnotation("demo.Direct", "run", " @demo.Marked "));
    }

    public void testAnnotationNoMatch() {
        assertFalse(matchesAnnotation("demo.Plain", "run", MARKED));
        // A qualified name has to match in full, even when the simple name does
        assertFalse(matchesAnnotation("demo.Direct", "run", "other.Marked"));
        assertFalse(matchesAnnotation("demo.Direct", "run", ""));
    }

    public void testAnnotationOnClassMatchesWithoutAMethod() {
        assertTrue(RuleMatcher.matchesAnnotation(null, findClass("demo.AnnotatedClass"), MARKED));
        assertFalse(RuleMatcher.matchesAnnotation(null, findClass("demo.Plain"), MARKED));
    }

    // --- class names ---

    public void testQualifiedClassName() {
        assertTrue(RuleMatcher.matchesClassName(findClass("demo.Plain"), "demo.Plain"));
        assertFalse(RuleMatcher.matchesClassName(findClass("demo.Plain"), "other.Plain"));
    }

    public void testSimpleClassName() {
        assertTrue(RuleMatcher.matchesClassName(findClass("demo.Plain"), "Plain"));
        assertTrue(RuleMatcher.matchesClassName(findClass("demo.Plain"), " Plain "));
        // A simple name matches every package, which is the price of not having to type the package
        assertTrue(RuleMatcher.matchesClassName(findClass("other.Plain"), "Plain"));
        assertFalse(RuleMatcher.matchesClassName(findClass("demo.Plain"), "Missing"));
    }

    public void testClassNameMatchesSuperClassAndInterface() {
        assertTrue(RuleMatcher.matchesClassName(findClass("demo.Child"), "demo.Base"));
        assertTrue(RuleMatcher.matchesClassName(findClass("demo.Child"), "Base"));
        assertTrue(RuleMatcher.matchesClassName(findClass("demo.Impl"), "demo.Api"));
        assertTrue(RuleMatcher.matchesClassName(findClass("demo.Impl"), "Api"));
        // Only upwards: a super class does not match one of its implementations
        assertFalse(RuleMatcher.matchesClassName(findClass("demo.Base"), "demo.Child"));
    }

    public void testClassNameIgnoresJavaLangObject() {
        assertFalse(RuleMatcher.matchesClassName(findClass("demo.Plain"), "java.lang.Object"));
        assertFalse(RuleMatcher.matchesClassName(findClass("demo.Plain"), "Object"));
    }

    public void testClassNameNoMatch() {
        assertFalse(RuleMatcher.matchesClassName(findClass("demo.Plain"), ""));
        assertFalse(RuleMatcher.matchesClassName(null, "demo.Plain"));
    }

    private boolean matchesAnnotation(String className, String methodName, String annotationName) {
        PsiClass psiClass = findClass(className);
        PsiMethod[] methods = psiClass.findMethodsByName(methodName, true);
        assertTrue("no method " + methodName + " on " + className, methods.length > 0);
        return RuleMatcher.matchesAnnotation(methods[0], psiClass, annotationName);
    }

    private PsiClass findClass(String qualifiedName) {
        PsiClass psiClass = JavaPsiFacade.getInstance(getProject())
                .findClass(qualifiedName, GlobalSearchScope.allScope(getProject()));
        assertNotNull("class not found: " + qualifiedName, psiClass);
        return psiClass;
    }
}
