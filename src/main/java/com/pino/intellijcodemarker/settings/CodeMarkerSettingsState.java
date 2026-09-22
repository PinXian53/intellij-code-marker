package com.pino.intellijcodemarker.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.SimpleModificationTracker;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Service
@State(
        name = "CodeMarkerSettingsState",
        storages = @Storage("CodeMarkerPlugin.xml")
)
public final class CodeMarkerSettingsState implements PersistentStateComponent<CodeMarkerSettingsState>, ModificationTracker {

    public List<ClassIconMapping> classIconMappings = new ArrayList<>();

    /** Lets cached marker lookups know that the rules changed. Not part of the persisted state. */
    private final SimpleModificationTracker modificationTracker = new SimpleModificationTracker();

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
        ruleChanged();
    }

    /** Must be called whenever {@link #classIconMappings} is modified. */
    public void ruleChanged() {
        modificationTracker.incModificationCount();
    }

    @Transient
    @Override
    public long getModificationCount() {
        return modificationTracker.getModificationCount();
    }

    public static class ClassIconMapping {
        public String className = "";
        public String annotationName = "";
        public String methodName = "";
        public String iconName = "";

        public ClassIconMapping() {
        }

        public ClassIconMapping(String className, String methodName, String iconName) {
            this(className, "", methodName, iconName);
        }

        public ClassIconMapping(String className, String annotationName, String methodName, String iconName) {
            this.className = className;
            this.annotationName = annotationName;
            this.methodName = methodName;
            this.iconName = iconName;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getAnnotationName() {
            return annotationName;
        }

        public void setAnnotationName(String annotationName) {
            this.annotationName = annotationName;
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
