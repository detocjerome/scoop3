package fr.ifremer.scoop3.gui.common.jdialog.oxbowFilter.list;

public enum CheckListFilterType {

    STARTS_WITH {

	@Override
	public boolean include(final String element, final String filter) {

	    if ((element == null) || (filter == null)) {
		return false;
	    }
	    return element.startsWith(filter);

	}

    },

    CONTAINS {

	@Override
	public boolean include(final String element, final String filter) {

	    if ((element == null) || (filter == null)) {
		return false;
	    }
	    return element.contains(filter);

	}

    };

    public abstract boolean include(String element, String filter);

}
