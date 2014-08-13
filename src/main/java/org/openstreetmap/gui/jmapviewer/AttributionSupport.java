package org.openstreetmap.gui.jmapviewer;

import org.openstreetmap.gui.jmapviewer.tilesources.TileSource;

import javax.inject.Inject;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AttributionSupport {

    private static final Font FONT = new Font("Arial", Font.PLAIN, 10);
    private Rectangle attrTextBounds = null;
    private Rectangle attrToUBounds = null;
    private TileSource source;

    static {
    }

    @Inject
    public AttributionSupport(TileSource source) {
        this.source = source;
    }

    public static void openLink(String url) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            System.err.printf("Opening link not supported on current platform ('%s')%n", url);
        }
    }

    public void paintAttribution(Graphics g, int width, int height) {
        if (source == null) {
            attrToUBounds = null;
            attrTextBounds = null;
            return;
        }
        Font font = g.getFont();
        g.setFont(FONT);
        attrToUBounds = source.getTermsOfUseText() == null ? null
                : drawText(g, width, height, source.getTermsOfUseText(), true);

        g.setFont(FONT);
        attrTextBounds = source.getAttributionText() == null ? null
                : drawText(g, width, height, source.getAttributionText(), false);

        g.setFont(font);
    }

    public Rectangle drawText(Graphics g, int width, int height, String text, boolean left) {
        Rectangle2D bounds = g.getFontMetrics().getStringBounds(text, g);
        int textHeight = (int) bounds.getHeight();
        int textWidth = (int) bounds.getWidth();
        int x = left ? 2 : width - textWidth - 2;
        int y = height - (textHeight - 5);
        g.setColor(Color.black);
        g.drawString(text, x + 1, y + 1);
        g.setColor(Color.white);
        g.drawString(text, x, y);
        return new Rectangle(x, y - (textHeight - 5), textWidth, textHeight);
    }

    public boolean handleAttribution(Point p, boolean click) {
        if (source == null)
            return false;

        if (click) {
            if (inAttrTextBounds(p)) {
                openLink(source.getAttributionLUrl());
                return true;
            }
            if (inAttrToUBounds(p)) {
                openLink(source.getTermsOfUseUrl());
                return true;
            }
            return false;
        } else {
            return inAttrTextBounds(p) || inAttrToUBounds(p);
        }
    }

    private boolean inAttrToUBounds(Point p) {
        return attrToUBounds != null && attrToUBounds.contains(p);
    }

    private boolean inAttrTextBounds(Point p) {
        return attrTextBounds != null && attrTextBounds.contains(p);
    }
}

