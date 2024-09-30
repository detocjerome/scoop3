package fr.ifremer.scoop3.gui.utils;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

/**
 * Source : http://stackoverflow.com/questions/3679886/how-can-i-let-jtoolbars-wrap
 * -to-the-next-line-flowlayout-without-them-being-hi
 */
public class FlowLayoutForJScrollPane extends FlowLayout {

    /**
     * 
     */
    private static final long serialVersionUID = -5460728467111695696L;

    public FlowLayoutForJScrollPane() {
	super();
    }

    public FlowLayoutForJScrollPane(final int align) {
	super(align);
    }

    public FlowLayoutForJScrollPane(final int align, final int hgap, final int vgap) {
	super(align, hgap, vgap);
    }

    @Override
    public Dimension minimumLayoutSize(final Container target) {
	// Size of largest component, so we can resize it in
	// either direction with something like a split-pane.
	return computeMinSize(target);
    }

    @Override
    public Dimension preferredLayoutSize(final Container target) {
	return computeSize(target);
    }

    private Dimension computeSize(final Container target) {
	synchronized (target.getTreeLock()) {
	    final int hgap = getHgap();
	    final int vgap = getVgap();
	    int w = target.getWidth();

	    // Let this behave like a regular FlowLayout (single row)
	    // if the container hasn't been assigned any size yet
	    if (w == 0) {
		w = Integer.MAX_VALUE;
	    }

	    Insets insets = target.getInsets();
	    if (insets == null) {
		insets = new Insets(0, 0, 0, 0);
	    }
	    int reqdWidth = 0;

	    final int maxwidth = w - (insets.left + insets.right + (hgap * 2));
	    final int n = target.getComponentCount();
	    int x = 0;
	    int y = insets.top + vgap; // FlowLayout starts by adding vgap, so
				       // do that here too.
	    int rowHeight = 0;

	    for (int i = 0; i < n; i++) {
		final Component c = target.getComponent(i);
		if (c.isVisible()) {
		    final Dimension d = c.getPreferredSize();
		    if ((x == 0) || ((x + d.width) <= maxwidth)) {
			// fits in current row.
			if (x > 0) {
			    x += hgap;
			}
			x += d.width;
			rowHeight = Math.max(rowHeight, d.height);
		    } else {
			// Start of new row
			x = d.width;
			y += vgap + rowHeight;
			rowHeight = d.height;
		    }
		    reqdWidth = Math.max(reqdWidth, x);
		}
	    }
	    y += rowHeight;
	    y += insets.bottom;
	    return new Dimension(reqdWidth + insets.left + insets.right, y);
	}
    }

    private Dimension computeMinSize(final Container target) {
	synchronized (target.getTreeLock()) {
	    int minx = Integer.MAX_VALUE;
	    int miny = Integer.MIN_VALUE;
	    boolean foundOne = false;
	    final int n = target.getComponentCount();

	    for (int i = 0; i < n; i++) {
		final Component c = target.getComponent(i);
		if (c.isVisible()) {
		    foundOne = true;
		    final Dimension d = c.getPreferredSize();
		    minx = Math.min(minx, d.width);
		    miny = Math.min(miny, d.height);
		}
	    }
	    if (foundOne) {
		return new Dimension(minx, miny);
	    }
	    return new Dimension(0, 0);
	}
    }
}
