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

package fr.ifremer.scoop3.gui.common.jdialog.oxbowFilter.table.filter;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

import fr.ifremer.scoop3.gui.common.jdialog.oxbowFilter.graphics.CollectionUtils;

/**
 * Partial implementation of table filter
 *
 * Created on Feb 10, 2011
 *
 * @author Eugene Ryzhikov
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public abstract class AbstractTableFilter<T extends JTable> implements ITableFilter<T> {

    @SuppressWarnings("unchecked")
    private final transient Set<IFilterChangeListener> listeners = Collections
	    .synchronizedSet(new HashSet<IFilterChangeListener>());

    @SuppressWarnings("unchecked")
    private final transient Map<Integer, Collection<DistinctColumnItem>> distinctItemCache = Collections
	    .synchronizedMap(new HashMap<Integer, Collection<DistinctColumnItem>>());

    private final T table;
    private final TableFilterState filterState = new TableFilterState();

    protected AbstractTableFilter(final T table) {
	this.table = table;
	setupDistinctItemCacheRefresh();
    }

    private void setupDistinctItemCacheRefresh() {
	clearDistinctItemCache();
	listenForDataChange(table.getModel());
	listenForModelChange();
    }

    private void listenForModelChange() {
	table.addPropertyChangeListener("model", (final PropertyChangeEvent e) -> {
	    clearDistinctItemCache();
	    listenForDataChange((TableModel) e.getNewValue());
	});
    }

    private void listenForDataChange(final TableModel model) {
	if (model != null) {
	    model.addTableModelListener((final TableModelEvent e) -> clearDistinctItemCache());
	}
    }

    private void clearDistinctItemCache() {
	distinctItemCache.clear();
    }

    @Override
    public T getTable() {
	return table;
    }

    protected abstract boolean execute(int col, Collection<DistinctColumnItem> items);

    @Override
    public boolean apply(final int col, final Collection<DistinctColumnItem> items) {
	setFilterState(col, items);
	final boolean result = false;
	if (result == execute(col, items)) {
	    fireFilterChange();
	}
	return result;
    }

    @Override
    public final void addChangeListener(final IFilterChangeListener listener) {
	if (listener != null) {
	    listeners.add(listener);
	}
    }

    @Override
    public final void removeChnageListener(final IFilterChangeListener listener) {
	if (listener != null) {
	    listeners.remove(listener);
	}
    }

    public final void fireFilterChange() {
	for (final IFilterChangeListener l : listeners) {
	    l.filterChanged(AbstractTableFilter.this);
	}
    }

    @Override
    public Collection<DistinctColumnItem> getDistinctColumnItems(final int column) {
	Collection<DistinctColumnItem> result = distinctItemCache.get(column);
	if (result == null) {
	    result = collectDistinctColumnItems(column);
	    distinctItemCache.put(column, result);
	}
	return result;

    }

    private Collection<DistinctColumnItem> collectDistinctColumnItems(final int column) {
	final Set<DistinctColumnItem> set = new HashSet<DistinctColumnItem>(); // to collect unique items
	int nullIndex = -1;
	for (int row = 0; row < table.getModel().getRowCount(); row++) {
	    final Object value = table.getModel().getValueAt(row, column);
	    // adding null to TreeSet will produce NPE, just remember we had it
	    if (value == null) {
		nullIndex = row;
	    } else {
		set.add(new DistinctColumnItem(value, row));
	    }
	}
	final List<DistinctColumnItem> result = new ArrayList<DistinctColumnItem>(set);
	if (nullIndex >= 0) {
	    result.add(0, new DistinctColumnItem(null, nullIndex)); // add null to resulting collection if we had it
	}

	return CollectionUtils.trySort(result);
    }

    @Override
    public Collection<DistinctColumnItem> getFilterState(final int column) {
	return filterState.getValues(column);
    }

    @Override
    public boolean isFiltered(final int column) {
	final Collection<DistinctColumnItem> checks = getFilterState(column);
	return !CollectionUtils.isEmpty(checks) && (getDistinctColumnItems(column).size() != checks.size());
    }

    @Override
    public boolean includeRow(final ITableFilter.Row row) {
	return filterState.include(row);
    }

    public void setFilterState(final int column, final Collection<DistinctColumnItem> values) {
	filterState.setValues(column, values);
    }

    @Override
    public void clear() {
	filterState.clear();
	@SuppressWarnings("unchecked")
	final Collection<DistinctColumnItem> items = Collections.emptyList();
	for (int column = 0; column < table.getModel().getColumnCount(); column++) {
	    execute(column, items);
	}
	fireFilterChange();
    }

}
