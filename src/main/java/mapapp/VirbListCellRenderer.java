package mapapp;

import sun.swing.DefaultLookup;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;

import static java.awt.GridBagConstraints.*;
import static java.lang.Math.ceil;

/**
 * @author tbaum
 * @since 05.08.2014
 */
class VirbListCellRenderer extends JPanel implements ListCellRenderer<VirbSource> {
    private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    private final JLabel icon = new JLabel();
    private final JLabel date = new JLabel();
    private final JLabel length = new JLabel();
    private final JLabel name = new JLabel();

    VirbListCellRenderer() {
        super(new GridBagLayout());

        setOpaque(true);
        setBorder(getNoFocusBorder());
        setName("List.cellRenderer");

        add(icon, new GridBagConstraints(0, 0, 1, 3, 1.0, 1.0, NORTHWEST, NONE, new Insets(2, 2, 2, 2), 0, 0));
        add(date, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, NORTHWEST, HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        add(length, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, NORTHWEST, HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        add(name, new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0, NORTHWEST, HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
    }

    private Border getNoFocusBorder() {
        Border border = DefaultLookup.getBorder(this, ui, "List.cellNoFocusBorder");
        return border != null
                ? border
                : System.getSecurityManager() != null ? SAFE_NO_FOCUS_BORDER : DEFAULT_NO_FOCUS_BORDER;
    }

    public Component getListCellRendererComponent(JList<? extends VirbSource> list, VirbSource value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        setComponentOrientation(list.getComponentOrientation());
        Color bg = null, fg = null;

        JList.DropLocation dropLocation = list.getDropLocation();
        if (dropLocation != null && !dropLocation.isInsert() && dropLocation.getIndex() == index) {
            bg = DefaultLookup.getColor(this, ui, "List.dropCellBackground");
            fg = DefaultLookup.getColor(this, ui, "List.dropCellForeground");
            isSelected = true;
        }

        if (isSelected) {
            setBackground(bg == null ? list.getSelectionBackground() : bg);
            setForeground(fg == null ? list.getSelectionForeground() : fg);
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        setEnabled(list.isEnabled());
        setFont(list.getFont());

        Border border = null;
        if (cellHasFocus) {
            if (isSelected) {
                border = DefaultLookup.getBorder(this, ui, "List.focusSelectedCellHighlightBorder");
            }
            if (border == null) {
                border = DefaultLookup.getBorder(this, ui, "List.focusCellHighlightBorder");
            }
        } else {
            border = getNoFocusBorder();
        }
        setBorder(border);

        icon.setIcon(value.preview != null ? new ImageIcon(value.preview) : null);
        date.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(value.getCreationDate()));
        name.setText(String.valueOf(value.getMovie()));
        length.setText(String.format("Duration: %.0f:%02.0f", ceil(value.getLength() / 60), value.getLength() % 60));

        return this;
    }
}
