package me.redstoner2019.odmsg.misc;

import javax.swing.*;
import java.awt.*;

public class VerticalCellRenderer<E> extends JPanel implements ListCellRenderer<E> {
    private JLabel label;

    public VerticalCellRenderer() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        label = new JLabel();
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(label);
    }
    @Override
    public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
        label.setText(value.toString());

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            label.setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            label.setForeground(list.getForeground());
        }

        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setOpaque(true);

        return this;
    }
}
