package com.pino.intellijcodemarker.marker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.pino.intellijcodemarker.resource.IconResource;
import com.pino.intellijcodemarker.settings.CodeMarkerSettingsState;

import java.util.ArrayList;
import java.util.List;

public class MyLineMarkerProviderTest extends LightJavaCodeInsightFixtureTestCase {

    private static final String ICON = "database";

    private final MyLineMarkerProvider provider = new MyLineMarkerProvider();
    private List<CodeMarkerSettingsState.ClassIconMapping> savedMappings;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        savedMappings = new ArrayList<>(settings().classIconMappings);
        myFixture.addClass("package demo; public @interface Marked {}");
        myFixture.addClass("package demo; public interface Api { @Marked void marked(); void plain(); }");
        myFixture.addClass("package demo; public class Service implements Api { public void marked() {} public void plain() {} }");
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            settings().classIconMappings.clear();
            settings().classIconMappings.addAll(savedMappings);
            settings().ruleChanged();
        } finally {
            super.tearDown();
        }
    }

    public void testAnnotationOnlyRule() {
        rule(new CodeMarkerSettingsState.ClassIconMapping("", "demo.Marked", "", ICON));

        assertEquals(ICON, iconOfCallTo("marked"));
        assertNull(iconOfCallTo("plain"));
    }

    public void testClassAndAnnotationMustBothMatch() {
        rule(new CodeMarkerSettingsState.ClassIconMapping("demo.Service", "demo.Marked", "", ICON));
        assertEquals(ICON, iconOfCallTo("marked"));

        // Also covers that changing the rules invalidates the cached lookup of the same method
        rule(new CodeMarkerSettingsState.ClassIconMapping("demo.Api", "demo.Missing", "", ICON));
        assertNull(iconOfCallTo("marked"));
    }

    public void testAnnotationAndMethodNameMustBothMatch() {
        rule(new CodeMarkerSettingsState.ClassIconMapping("", "demo.Marked", "plain", ICON));
        assertNull(iconOfCallTo("plain"));
        assertNull(iconOfCallTo("marked"));
    }

    public void testSimpleClassNameRule() {
        rule(new CodeMarkerSettingsState.ClassIconMapping("Api", "", "", ICON));

        assertEquals(ICON, iconOfCallTo("plain"));
        assertEquals(ICON, iconOfCallTo("marked"));
    }

    public void testClassNameRuleStillWorks() {
        // The three argument constructor keeps its old meaning: class name, method name, icon
        rule(new CodeMarkerSettingsState.ClassIconMapping("demo.Api", "plain", ICON));
        assertEquals(ICON, iconOfCallTo("plain"));
        assertNull(iconOfCallTo("marked"));

        // An empty method name still marks every call into the class
        rule(new CodeMarkerSettingsState.ClassIconMapping("demo.Api", "", ICON));
        assertEquals(ICON, iconOfCallTo("plain"));
        assertEquals(ICON, iconOfCallTo("marked"));
    }

    public void testEmptyRuleIsIgnored() {
        rule(new CodeMarkerSettingsState.ClassIconMapping("", "  ", " ", ICON));
        assertNull(iconOfCallTo("marked"));
    }

    public void testMarkerIsRegisteredOnTheMethodNameIdentifier() {
        rule(new CodeMarkerSettingsState.ClassIconMapping("demo.Api", "", "", ICON));
        PsiElement identifier = methodNameIdentifier("marked");

        LineMarkerInfo<?> info = provider.getLineMarkerInfo(identifier);

        assertNotNull(info);
        assertEquals(identifier, info.getElement());
        // The call expression itself must not produce a marker, the platform only allows leaves
        assertNull(provider.getLineMarkerInfo(identifier.getParent().getParent()));
    }

    private void rule(CodeMarkerSettingsState.ClassIconMapping mapping) {
        settings().classIconMappings.clear();
        settings().classIconMappings.add(mapping);
        settings().ruleChanged();
    }

    /** @return the icon name the provider assigns to {@code service.<methodName>()}, or null when unmarked */
    private String iconOfCallTo(String methodName) {
        LineMarkerInfo<?> info = provider.getLineMarkerInfo(methodNameIdentifier(methodName));
        return info == null ? null : iconNameOf(info);
    }

    private PsiElement methodNameIdentifier(String methodName) {
        myFixture.configureByText("Caller.java", """
                import demo.Service;

                class Caller {
                    void call(Service service) {
                        service.%s();
                    }
                }
                """.formatted(methodName));

        PsiMethodCallExpression call = PsiTreeUtil.findChildOfType(myFixture.getFile(), PsiMethodCallExpression.class);
        assertNotNull("no method call in the configured file", call);

        PsiElement identifier = call.getMethodExpression().getReferenceNameElement();
        assertNotNull("no method name identifier", identifier);
        return identifier;
    }

    private String iconNameOf(LineMarkerInfo<?> info) {
        for (String iconName : IconResource.getAllIconName()) {
            if (IconResource.loadSvgIcon(iconName).equals(info.getIcon())) {
                return iconName;
            }
        }
        return null;
    }

    private static CodeMarkerSettingsState settings() {
        return CodeMarkerSettingsState.getInstance();
    }
}
