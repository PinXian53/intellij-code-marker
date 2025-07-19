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
        // Microsoft Corporation icons
        AVAILABLE_ICONS.put("document", "icons/microsoft-corporation/document.svg");
        AVAILABLE_ICONS.put("document-add", "icons/microsoft-corporation/document-add.svg");
        AVAILABLE_ICONS.put("document-edit", "icons/microsoft-corporation/document-edit.svg");
        AVAILABLE_ICONS.put("dismiss", "icons/microsoft-corporation/dismiss.svg");
        AVAILABLE_ICONS.put("alert", "icons/microsoft-corporation/alert.svg");
        AVAILABLE_ICONS.put("cloud", "icons/microsoft-corporation/cloud.svg");
        AVAILABLE_ICONS.put("database", "icons/microsoft-corporation/database.svg");
        AVAILABLE_ICONS.put("mail", "icons/microsoft-corporation/mail.svg");
        AVAILABLE_ICONS.put("message", "icons/microsoft-corporation/message.svg");
        AVAILABLE_ICONS.put("shield", "icons/microsoft-corporation/shield.svg");
        AVAILABLE_ICONS.put("agents", "icons/microsoft-corporation/agents.svg");
        AVAILABLE_ICONS.put("star", "icons/microsoft-corporation/star.svg");
        
        // Material Extensions icons
        AVAILABLE_ICONS.put("console", "icons/material-extensions/console.svg");
        AVAILABLE_ICONS.put("gemini-ai", "icons/material-extensions/gemini-ai.svg");
        AVAILABLE_ICONS.put("hurl", "icons/material-extensions/hurl.svg");
        AVAILABLE_ICONS.put("key", "icons/material-extensions/key.svg");
        AVAILABLE_ICONS.put("openapi", "icons/material-extensions/openapi.svg");
        AVAILABLE_ICONS.put("openapi-light", "icons/material-extensions/openapi-light.svg");
        AVAILABLE_ICONS.put("palette", "icons/material-extensions/palette.svg");
        AVAILABLE_ICONS.put("readme", "icons/material-extensions/readme.svg");
        AVAILABLE_ICONS.put("slug", "icons/material-extensions/slug.svg");
        AVAILABLE_ICONS.put("swagger", "icons/material-extensions/swagger.svg");
        AVAILABLE_ICONS.put("test-1", "icons/material-extensions/test-1.svg");
        AVAILABLE_ICONS.put("test-2", "icons/material-extensions/test-2.svg");
        AVAILABLE_ICONS.put("todo", "icons/material-extensions/todo.svg");
        AVAILABLE_ICONS.put("tree", "icons/material-extensions/tree.svg");
        
        // DevIcon icons
        AVAILABLE_ICONS.put("aws", "icons/devicon/aws.svg");
        AVAILABLE_ICONS.put("azure", "icons/devicon/azure.svg");
        AVAILABLE_ICONS.put("gcp", "icons/devicon/gcp.svg");
        AVAILABLE_ICONS.put("redis", "icons/devicon/redis.svg");
        AVAILABLE_ICONS.put("elasticsearch", "icons/devicon/elasticsearch.svg");
        AVAILABLE_ICONS.put("mongodb", "icons/devicon/mongodb.svg");
        AVAILABLE_ICONS.put("postgresql", "icons/devicon/postgresql.svg");
        AVAILABLE_ICONS.put("hadoop", "icons/devicon/hadoop.svg");
        AVAILABLE_ICONS.put("kafka", "icons/devicon/kafka.svg");
        AVAILABLE_ICONS.put("rabbitmq", "icons/devicon/rabbitmq.svg");
        AVAILABLE_ICONS.put("apache", "icons/devicon/apache.svg");
        AVAILABLE_ICONS.put("grpc", "icons/devicon/grpc.svg");
        AVAILABLE_ICONS.put("spring", "icons/devicon/spring.svg");
        AVAILABLE_ICONS.put("twilio", "icons/devicon/twilio.svg");
        
        // Stream Line icons
        AVAILABLE_ICONS.put("button-fast-forward", "icons/stream-line/button-fast-forward.svg");
        AVAILABLE_ICONS.put("button-play", "icons/stream-line/button-play.svg");
        AVAILABLE_ICONS.put("button-stop", "icons/stream-line/button-stop.svg");
        AVAILABLE_ICONS.put("cloud-data-transfer", "icons/stream-line/cloud-data-transfer.svg");
        AVAILABLE_ICONS.put("data-transfer", "icons/stream-line/data-transfer.svg");
        AVAILABLE_ICONS.put("database-stream", "icons/stream-line/database.svg");
        AVAILABLE_ICONS.put("filter", "icons/stream-line/filter.svg");
        AVAILABLE_ICONS.put("folder-add", "icons/stream-line/folder-add.svg");
        AVAILABLE_ICONS.put("folder-share", "icons/stream-line/folder-share.svg");
        AVAILABLE_ICONS.put("folder-upload", "icons/stream-line/folder-upload.svg");
        AVAILABLE_ICONS.put("signal", "icons/stream-line/signal.svg");
        AVAILABLE_ICONS.put("smiley-happy", "icons/stream-line/smiley-happy.svg");
        AVAILABLE_ICONS.put("smiley-mad", "icons/stream-line/smiley-mad.svg");
        AVAILABLE_ICONS.put("smiley-sad", "icons/stream-line/smiley-sad.svg");
        AVAILABLE_ICONS.put("smiley-unhappy", "icons/stream-line/smiley-unhappy.svg");
        AVAILABLE_ICONS.put("stool", "icons/stream-line/stool.svg");
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
