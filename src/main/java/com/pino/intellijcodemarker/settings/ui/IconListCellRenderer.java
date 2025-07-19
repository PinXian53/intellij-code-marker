package com.pino.intellijcodemarker.settings.ui;

import com.pino.intellijcodemarker.resource.IconResource;

import javax.swing.*;
import java.awt.*;

public class IconListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof String iconName) {
            var icon = IconResource.loadSvgIcon(iconName);
            setIcon(icon);
            setText(iconName);
        }

        return this;
    }
}
