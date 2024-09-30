/*
 * Copyright (c) 2009-2011, EzWare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.Redistributions
 * in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.Neither the name of the
 * EzWare nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package fr.ifremer.scoop3.gui.common.jdialog.oxbowFilter.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractListModel;

import fr.ifremer.scoop3.gui.common.jdialog.oxbowFilter.util.DefaultObjectToStringTranslator;
import fr.ifremer.scoop3.gui.common.jdialog.oxbowFilter.util.IObjectToStringTranslator;
import fr.ifremer.scoop3.gui.common.jdialog.oxbowFilter.util.IValueWrapper;

/**
 * Default model for check list. It is based on the list of items Implementation of checks is based on HashSet of
 * checked items
 *
 * @author Eugene Ryzhikov
 *
 * @param <T>
 *            list element type
 */
@SuppressWarnings("rawtypes")
public class DefaultCheckListModel<T> extends AbstractListModel implements ICheckListModel<T> {

    private static final long serialVersionUID = 1L;

    private final List<T> data = new ArrayList<T>();
    private final Set<T> checks = new HashSet<T>();

    public static IObjectToStringTranslator defaultTranslator = new DefaultObjectToStringTranslator();
    private transient List<T> filteredData = null;

    public DefaultCheckListModel(final Collection<? extends T> data) {

	if (data == null) {
	    return;
	}
	for (final T object : data) {
	    this.data.add(object);
	    checks.clear();
	}
    }

    @SuppressWarnings("unchecked")
    public DefaultCheckListModel(final T... data) {
	this(Arrays.asList(data));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.oxbow.swingbits.list.ICheckListModel#getSize()
     */
    @Override
    public int getSize() {
	return data().size();
    }

    private List<T> data() {
	return filteredData == null ? data : filteredData;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.oxbow.swingbits.list.ICheckListModel#getElementAt(int)
     */
    @Override
    public Object getElementAt(final int index) {
	return data().get(index);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.oxbow.swingbits.list.ICheckListModel#isChecked(int)
     */
    @Override
    public boolean isCheckedIndex(final int index) {
	return checks.contains(data().get(index));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.oxbow.swingbits.list.ICheckListModel#setChecked(int, boolean)
     */
    @Override
    public void setCheckedIndex(final int index, final boolean value) {
	final T o = data().get(index);
	if (value) {
	    checks.add(o);
	} else {
	    checks.remove(o);
	}
	fireContentsChanged(this, index, index);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.oxbow.swingbits.list.ICheckListModel#getChecked()
     */
    @Override
    public Collection<T> getCheckedItems() {
	final List<T> items = new ArrayList<T>(checks);
	items.retainAll(data);
	return Collections.unmodifiableList(items);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.oxbow.swingbits.list.ICheckListModel#setChecked(java.util.Collection)
     */
    @Override
    public void setCheckedItems(final Collection<T> items) {

	// if ( CollectionUtils.isEmpty(items)) return;

	final List<T> correctedItems = new ArrayList<T>(items);
	correctedItems.retainAll(data);

	checks.clear();
	checks.addAll(correctedItems);
	fireContentsChanged(this, 0, checks.size() - 1);

    }

    @Override
    public void filter(final String filter, final IObjectToStringTranslator translator,
	    final CheckListFilterType filterType) {

	if ((filter == null) || (filter.trim().length() == 0)) {
	    filteredData = null;
	} else {

	    final CheckListFilterType ft = filterType == null ? CheckListFilterType.CONTAINS : filterType;

	    final IObjectToStringTranslator t = translator == null ? defaultTranslator : translator;
	    final String f = filter.toLowerCase();

	    final List<T> fData = new ArrayList<T>();

	    Object value;
	    for (final T o : data) {
		// if ( t.translate(o).startsWith(f)) {
		value = o instanceof IValueWrapper ? ((IValueWrapper<?>) o).getValue() : o;
		if (ft.include(t.translate(value), f)) {
		    fData.add(o);
		}
	    }
	    filteredData = fData;
	}

	fireContentsChanged(this, 0, data.size() - 1);

    }

}
