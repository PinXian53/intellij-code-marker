package com.pino.intellijcodemarker.settings;

import com.intellij.openapi.util.JDOMUtil;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CodeMarkerSettingsStateTest {

    /** CodeMarkerPlugin.xml as written by 1.0.2, before the annotation column existed. */
    private static final String LEGACY_STATE = """
            <CodeMarkerSettingsState>
              <option name="classIconMappings">
                <list>
                  <ClassIconMapping>
                    <option name="className" value="com.example.OrderService" />
                    <option name="iconName" value="database" />
                    <option name="methodName" value="save" />
                  </ClassIconMapping>
                  <ClassIconMapping>
                    <option name="className" value="com.example.MailSender" />
                    <option name="iconName" value="mail" />
                  </ClassIconMapping>
                </list>
              </option>
            </CodeMarkerSettingsState>
            """;

    @Test
    public void legacyStateIsStillReadable() throws Exception {
        CodeMarkerSettingsState state = deserialize(LEGACY_STATE);
        List<CodeMarkerSettingsState.ClassIconMapping> mappings = state.classIconMappings;

        assertEquals(2, mappings.size());

        assertEquals("com.example.OrderService", mappings.get(0).getClassName());
        assertEquals("save", mappings.get(0).getMethodName());
        assertEquals("database", mappings.get(0).getIconName());
        // The new field has to default to an empty string, never null
        assertEquals("", mappings.get(0).getAnnotationName());

        assertEquals("com.example.MailSender", mappings.get(1).getClassName());
        assertEquals("", mappings.get(1).getMethodName());
        assertEquals("mail", mappings.get(1).getIconName());
        assertEquals("", mappings.get(1).getAnnotationName());
    }

    @Test
    public void unknownOptionsAreIgnored() throws Exception {
        // What an older plugin build sees after a downgrade: a field it does not know about.
        // The serializer has to skip it instead of failing to read the rest of the rule.
        CodeMarkerSettingsState state = deserialize("""
                <CodeMarkerSettingsState>
                  <option name="classIconMappings">
                    <list>
                      <ClassIconMapping>
                        <option name="className" value="com.example.OrderService" />
                        <option name="annotationName" value="demo.Marked" />
                        <option name="methodName" value="save" />
                        <option name="iconName" value="database" />
                        <option name="fieldFromTheFuture" value="whatever" />
                      </ClassIconMapping>
                    </list>
                  </option>
                </CodeMarkerSettingsState>
                """);

        assertEquals(1, state.classIconMappings.size());
        assertEquals("com.example.OrderService", state.classIconMappings.get(0).getClassName());
        assertEquals("database", state.classIconMappings.get(0).getIconName());
    }

    @Test
    public void exportedStateOnlyContainsTheRules() {
        CodeMarkerSettingsState state = new CodeMarkerSettingsState();
        state.classIconMappings.add(
                new CodeMarkerSettingsState.ClassIconMapping("com.example.OrderService", "save", "database"));
        state.ruleChanged();

        String xml = JDOMUtil.write(XmlSerializer.serialize(state));

        // The tracker that drives cache invalidation must not leak into the settings or export file
        assertFalse(xml, xml.contains("modificationCount"));
        assertFalse(xml, xml.contains("modificationTracker"));
    }

    @Test
    public void annotationIsPersisted() throws Exception {
        CodeMarkerSettingsState state = new CodeMarkerSettingsState();
        state.classIconMappings.add(
                new CodeMarkerSettingsState.ClassIconMapping("", "demo.Marked", "", "database"));

        CodeMarkerSettingsState reloaded = deserialize(JDOMUtil.write(XmlSerializer.serialize(state)));

        assertEquals(1, reloaded.classIconMappings.size());
        assertEquals("demo.Marked", reloaded.classIconMappings.get(0).getAnnotationName());
    }

    private static CodeMarkerSettingsState deserialize(String xml) throws Exception {
        Element element = JDOMUtil.load(xml);
        return XmlSerializer.deserialize(element, CodeMarkerSettingsState.class);
    }
}
