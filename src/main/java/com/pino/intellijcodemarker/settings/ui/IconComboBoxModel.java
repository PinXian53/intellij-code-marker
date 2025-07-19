package com.pino.intellijcodemarker.settings.ui;

import com.pino.intellijcodemarker.resource.IconResource;

import javax.swing.*;

public class IconComboBoxModel extends DefaultComboBoxModel<String> {
    public IconComboBoxModel() {
        super(IconResource.getAllIconName());
    }
}
