package com.pino.intellijcodemarker.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Service
@State(
        name = "CodeMarkerSettingsState",
        storages = @Storage("CodeMarkerPlugin.xml")
)
public final class CodeMarkerSettingsState implements PersistentStateComponent<CodeMarkerSettingsState> {

    public List<ClassIconMapping> classIconMappings = new ArrayList<>();

    public static CodeMarkerSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(CodeMarkerSettingsState.class);
    }

    @Override
    public @Nullable CodeMarkerSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull CodeMarkerSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static class ClassIconMapping {
        public String className = "";
        public String methodName = "";
        public String iconName = "";

        public ClassIconMapping() {
        }

        public ClassIconMapping(String className, String methodName, String iconName) {
            this.className = className;
            this.methodName = methodName;
            this.iconName = iconName;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getIconName() {
            return iconName;
        }

        public void setIconName(String iconName) {
            this.iconName = iconName;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }
    }
}
