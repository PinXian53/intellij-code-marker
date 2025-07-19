package com.pino.intellijcodemarker.resource;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class IconResource {

    private static final Map<String, String> AVAILABLE_ICONS = new LinkedHashMap<>();
    private static final Map<String, Icon> iconCache = new HashMap<>();
    private static final String DEFAULT_ICON_NAME = "document";

    static {
        AVAILABLE_ICONS.put("document", "icons/microsoft-corporation/document.svg");
        AVAILABLE_ICONS.put("document-add", "icons/microsoft-corporation/document-add.svg");
        AVAILABLE_ICONS.put("document-edit", "icons/microsoft-corporation/document-edit.svg");
        AVAILABLE_ICONS.put("mail", "icons/microsoft-corporation/mail.svg");
        AVAILABLE_ICONS.put("shield", "icons/microsoft-corporation/shield.svg");
        AVAILABLE_ICONS.put("star", "icons/microsoft-corporation/star.svg");
        AVAILABLE_ICONS.put("alert", "icons/microsoft-corporation/alert.svg");
        AVAILABLE_ICONS.put("message", "icons/microsoft-corporation/message.svg");
        AVAILABLE_ICONS.put("cloud", "icons/microsoft-corporation/cloud.svg");
        AVAILABLE_ICONS.put("database", "icons/microsoft-corporation/database.svg");
        AVAILABLE_ICONS.put("readme", "icons/material-extensions/readme.svg");
        AVAILABLE_ICONS.put("openapi", "icons/material-extensions/openapi.svg");
        AVAILABLE_ICONS.put("openapi-light", "icons/material-extensions/openapi-light.svg");
        AVAILABLE_ICONS.put("swagger", "icons/material-extensions/swagger.svg");
        AVAILABLE_ICONS.put("test-1", "icons/material-extensions/test-1.svg");
        AVAILABLE_ICONS.put("test-2", "icons/material-extensions/test-2.svg");
        AVAILABLE_ICONS.put("todo", "icons/material-extensions/todo.svg");
        AVAILABLE_ICONS.put("key", "icons/material-extensions/key.svg");
        AVAILABLE_ICONS.put("hurl", "icons/material-extensions/hurl.svg");
        AVAILABLE_ICONS.put("palette", "icons/material-extensions/palette.svg");
        AVAILABLE_ICONS.put("slug", "icons/material-extensions/slug.svg");
        AVAILABLE_ICONS.put("tree", "icons/material-extensions/tree.svg");
        AVAILABLE_ICONS.put("azuresql", "icons/devicon/azuresql.svg");
        AVAILABLE_ICONS.put("elasticsearch", "icons/devicon/elasticsearch.svg");
        AVAILABLE_ICONS.put("gcp", "icons/devicon/gcp.svg");
        AVAILABLE_ICONS.put("hadoop", "icons/devicon/hadoop.svg");
        AVAILABLE_ICONS.put("mongodb", "icons/devicon/mongodb.svg");
        AVAILABLE_ICONS.put("postgresql", "icons/devicon/postgresql.svg");
        AVAILABLE_ICONS.put("rabbitmq", "icons/devicon/rabbitmq.svg");
        AVAILABLE_ICONS.put("redis", "icons/devicon/redis.svg");
        AVAILABLE_ICONS.put("ftp", "icons/custom/ftp.svg");
        AVAILABLE_ICONS.put("ftp-2", "icons/custom/ftp-2.svg");
    }

    private IconResource() {
    }

    public static String getDefaultIconName() {
        return DEFAULT_ICON_NAME;
    }

    public static String getIconPath(String iconName) {
        return AVAILABLE_ICONS.get(iconName);
    }

    public static String[] getAllIconName() {
        return AVAILABLE_ICONS.keySet().toArray(new String[0]);
    }

    public static Icon loadSvgIcon(String iconName) {
        if (iconCache.containsKey(iconName)) {
            return iconCache.get(iconName);
        }
        var icon = IconLoader.getIcon(IconResource.getIconPath(iconName), IconResource.class);
        iconCache.put(iconName, icon);
        return icon;
    }
}
