package me.redstoner2019.odmsg.misc;

import javax.swing.*;
import java.awt.*;

public class AlternatingListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        // Alternate the background color
        if (!isSelected) {
            if (index % 2 == 0) {
                component.setBackground(new Color(240,240,240));
            } else {
                component.setBackground(Color.WHITE);
            }
        }
        return component;
    }
}