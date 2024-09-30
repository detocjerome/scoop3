package fr.ifremer.scoop3.gui.data.commandButton;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.text.MessageFormat;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandToggleButton;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;

import fr.ifremer.scoop3.gui.data.DataViewImpl;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.model.QCValues;

public class SelectAndChangeQCCommandButton {

    private JCheckBoxMenuItem displayOnlyQCCheckbox;
    private JCheckBoxMenuItem excludeOnlyQCCheckbox;
    private final int qc;
    private final AbstractCommandButton qcButton;

    public SelectAndChangeQCCommandButton(final DataViewImpl dataViewImpl, final JCommandToggleButton qcButton,
	    final int qc) {
	this.qcButton = qcButton;
	this.qc = qc;

	displayOnlyQCCheckbox = new JCheckBoxMenuItem(
		MessageFormat.format(Messages.getMessage("gui.select-and-change-qc-command-button.display-only-qc"),
			QCValues.getQCValues(qc).getCharQCValue()));
	displayOnlyQCCheckbox.addActionListener((final ActionEvent e) -> {
	    dataViewImpl.displayOnlyQCCheckboxStateChanged(SelectAndChangeQCCommandButton.this);
	    for (final SelectAndChangeQCCommandButton sacqccb : dataViewImpl.getSelectAndChangeQCs()) {
		sacqccb.getExcludeOnlyQCCheckbox().setSelected(false);
		sacqccb.updateButtonBackground();
	    }
	});

	excludeOnlyQCCheckbox = new JCheckBoxMenuItem(
		MessageFormat.format(Messages.getMessage("gui.select-and-change-qc-command-button.exclude-only-qc"),
			QCValues.getQCValues(qc).getCharQCValue()));
	excludeOnlyQCCheckbox.addActionListener((final ActionEvent e) -> {
	    dataViewImpl.excludeOnlyQCCheckboxStateChanged(SelectAndChangeQCCommandButton.this);
	    for (final SelectAndChangeQCCommandButton sacqccb : dataViewImpl.getSelectAndChangeQCs()) {
		sacqccb.getDisplayOnlyQCCheckbox().setSelected(false);
		sacqccb.updateButtonBackground();
	    }
	});

	qcButton.addMouseListener(new MouseAdapter() {
	    /*
	     * (non-Javadoc)
	     *
	     * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
	     */
	    @Override
	    public void mouseClicked(final MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
		    final JPopupMenu popup = new JPopupMenu();
		    popup.add(displayOnlyQCCheckbox);
		    popup.add(excludeOnlyQCCheckbox);
		    popup.show(e.getComponent(), e.getX(), e.getY());
		}
	    }
	});
    }

    public SelectAndChangeQCCommandButton(final DataViewImpl dataViewImpl, final JCommandButton qcButton,
	    final int qc) {
	this.qcButton = qcButton;
	this.qc = qc;

	displayOnlyQCCheckbox = null;
	if (qc != QCValues.QC_FILL_VALUE.getQCValue()) { // Add
	    displayOnlyQCCheckbox = new JCheckBoxMenuItem(
		    MessageFormat.format(Messages.getMessage("gui.select-and-change-qc-command-button.display-only-qc"),
			    QCValues.getQCValues(qc).getCharQCValue()));
	    displayOnlyQCCheckbox.addActionListener((final ActionEvent e) -> {
		dataViewImpl.displayOnlyQCCheckboxStateChanged(SelectAndChangeQCCommandButton.this);
		for (final SelectAndChangeQCCommandButton sacqccb : dataViewImpl.getSelectAndChangeQCs()) {
		    sacqccb.getExcludeOnlyQCCheckbox().setSelected(false);
		    sacqccb.updateButtonBackground();
		}
	    });

	    excludeOnlyQCCheckbox = new JCheckBoxMenuItem(
		    MessageFormat.format(Messages.getMessage("gui.select-and-change-qc-command-button.exclude-only-qc"),
			    QCValues.getQCValues(qc).getCharQCValue()));
	    excludeOnlyQCCheckbox.addActionListener((final ActionEvent e) -> {
		dataViewImpl.excludeOnlyQCCheckboxStateChanged(SelectAndChangeQCCommandButton.this);
		for (final SelectAndChangeQCCommandButton sacqccb : dataViewImpl.getSelectAndChangeQCs()) {
		    sacqccb.getDisplayOnlyQCCheckbox().setSelected(false);
		    sacqccb.updateButtonBackground();
		}
	    });

	    qcButton.addMouseListener(new MouseAdapter() {
		/*
		 * (non-Javadoc)
		 *
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(final MouseEvent e) {
		    if (SwingUtilities.isRightMouseButton(e)) {
			final JPopupMenu popup = new JPopupMenu();
			popup.add(displayOnlyQCCheckbox);
			popup.add(excludeOnlyQCCheckbox);
			popup.show(e.getComponent(), e.getX(), e.getY());
		    }
		}
	    });
	} else if (qc == QCValues.QC_FILL_VALUE.getQCValue()) {
	    qcButton.setEnabled(false);
	    final URL resource = getClass().getClassLoader()
		    .getResource("icons/"
			    + (dataViewImpl.getScoop3Frame().getTitle()
				    .contains(Messages.getMessage("bpc-controller.application-title")) ? "bpc_" : "")
			    + "select_and_change_qc_" + qc + "_inactive.png");
	    qcButton.setDisabledIcon(ImageWrapperResizableIcon.getIcon(resource, new Dimension(32, 32)));
	}

    }

    /**
     * @return -1 if the checkbox is not selected, otherwise return the getQc()
     */
    public int getDisplayOnlyQCCheckboxValue() {
	if (displayOnlyQCCheckbox.isSelected()) {
	    return getQc();
	}
	return -1;
    }

    /**
     * @return -1 if the checkbox is not selected, otherwise return the getQc()
     */
    public int getExcludeOnlyQCCheckboxValue() {
	if (excludeOnlyQCCheckbox.isSelected()) {
	    return getQc();
	}
	return -1;
    }

    /**
     * @return the qc
     */
    public int getQc() {
	return qc;
    }

    /**
     * @return the selectAndChange
     */
    public AbstractCommandButton getSelectAndChange() {
	return qcButton;
    }

    /**
     * Unselect the checkbox : displayOnlyQCCheckbox
     */
    public void unselectDisplayOnlyQCCheckbox() {
	displayOnlyQCCheckbox.setSelected(false);
	updateButtonBackground();
    }

    /**
     * Unselect the checkbox : excludeOnlyQCCheckbox
     */
    public void unselectExcludeOnlyQCCheckbox() {
	excludeOnlyQCCheckbox.setSelected(false);
	updateButtonBackground();
    }

    public JCheckBoxMenuItem getDisplayOnlyQCCheckbox() {
	return this.displayOnlyQCCheckbox;
    }

    public JCheckBoxMenuItem getExcludeOnlyQCCheckbox() {
	return this.excludeOnlyQCCheckbox;
    }

    public void updateButtonBackground() {
	if (displayOnlyQCCheckbox.isSelected()) {
	    qcButton.setBackground(Color.GRAY);
	    qcButton.setOpaque(true);
	} else if (excludeOnlyQCCheckbox.isSelected()) {
	    qcButton.setBackground(Color.RED);
	    qcButton.setOpaque(true);
	} else {
	    qcButton.setOpaque(false);
	}
	qcButton.validate();
	qcButton.repaint();
    }
}
