package fr.ifremer.scoop3.infra.event;

import java.beans.PropertyChangeEvent;

public class MapPropertyChangeEvent extends PropertyChangeEvent {

    public enum MAP_EVENT_ENUM {
	LINK_UNLINK_OBSERVATIONS, //
	MAP_SELECTION, //
	SELECT_OBSERVATION_BY_REFERENCE, //
	SELECT_OBSERVATION_NAV, //
	SHOW_HIDE_LABELS, //
	UPDATE_LOCATION_FOR_OBS, //
	UPDATE_QC_FOR_OBS, //
	ZOOM_IN, //
	ZOOM_INITIAL, //
	ZOOM_OUT, //
	ZOOM_RECTANGLE, //
	ZOOM_WORLD, //
    }

    private static final long serialVersionUID = 4650088162366717955L;

    private final MAP_EVENT_ENUM eventEnum;
    private int levelIndex;

    public MapPropertyChangeEvent(final Object source, final MAP_EVENT_ENUM eventEnum) {
	super(source, eventEnum.toString(), null, null);
	this.eventEnum = eventEnum;
    }

    public MapPropertyChangeEvent(final Object source, final MAP_EVENT_ENUM eventEnum, final Object oldValue,
	    final Object newValue) {
	super(source, eventEnum.toString(), oldValue, newValue);
	this.eventEnum = eventEnum;
    }

    /**
     * @return the eventEnum
     */
    public MAP_EVENT_ENUM getEventEnum() {
	return eventEnum;
    }

    /**
     * @param levelIndex
     *            the levelIndex to set
     * @return this
     */
    public MapPropertyChangeEvent setLevelIndex(final int levelIndex) {
	this.levelIndex = levelIndex;
	return this;
    }

    /**
     * @return the levelIndex
     */
    public int getLevelIndex() {
	return levelIndex;
    }
}
