package mapapp;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * @author tbaum
 * @since 12.08.2014
 */
abstract class MapMouseMoveListener implements MouseMotionListener, MouseListener {
    private Point start = null;

    @Override public void mouseDragged(MouseEvent e) {
        Point point = e.getPoint();
        moveMap(start.x - point.x, start.y - point.y);
        start = point;
    }

    public abstract void moveMap(int x, int y);

    @Override public void mouseMoved(MouseEvent e) {
    }

    @Override public void mouseClicked(MouseEvent e) {
    }

    @Override public void mousePressed(MouseEvent e) {
        start = e.getPoint();
    }

    @Override public void mouseReleased(MouseEvent e) {
        start = null;
    }

    @Override public void mouseEntered(MouseEvent e) {
    }

    @Override public void mouseExited(MouseEvent e) {
    }
}
