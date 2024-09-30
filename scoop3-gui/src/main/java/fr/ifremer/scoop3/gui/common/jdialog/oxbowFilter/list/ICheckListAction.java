package fr.ifremer.scoop3.gui.common.jdialog.oxbowFilter.list;

import java.util.ArrayList;
import java.util.Collection;

import fr.ifremer.scoop3.infra.i18n.Messages;

public interface ICheckListAction<T> {

    void check(ICheckListModel<T> model, boolean value);

    public static class CheckAll<T> implements ICheckListAction<T> {

	@Override
	public String toString() {
	    return "(" + Messages.getMessage("gui.oxbowFilter.check-all") + ")";
	}

	@SuppressWarnings("unchecked")
	@Override
	public void check(final ICheckListModel<T> model, final boolean value) {
	    final Collection<T> items = new ArrayList<T>();
	    if (value) {
		for (int i = 0, s = model.getSize(); i < s; i++) {
		    items.add((T) model.getElementAt(i));
		}
	    }
	    model.setCheckedItems(items);

	}

    }

}