package fr.ifremer.scoop3.gui.common.model;

import java.beans.PropertyChangeEvent;

public class SC3PropertyChangeEvent extends PropertyChangeEvent {

    public enum EVENT_ENUM {
	ALL_PROFILES, //
	VALIDATE, // The button "Validate" is pressed
	CHANGE_AXIS_MIN_MAX, //
	CHANGE_COMMENT, // The comment in the error message
	CHANGE_IS_CHECKED, // The checkbox in the error message
	CHANGE_QC, //
	CHANGE_METADATA, // Change a Metadata (or its QC)
	CHANGE_MULTIPLE, // Change on multiple error messages
	CHANGE_SUPERPOSED_MODE, //
	DISPLAYED_QC, //
	DISPLAY_CIRCLE_ON_GRAPH, // Display/Hide the circle visible on the graphs
	DISPLAY_DATA_TABLE, //
	DISPLAY_LINE_ON_GRAPH, // Display/Hide the line between points visible on the graphs
	DISPLAY_ONLY_QC, // Enable/Disable the "display only" for the selected QC on graphs
	DISPLAY_POINTS_ON_GRAPH, // // Display/Hide the points visible on the graphs
	DISPLAY_STATION_TYPE, // Display or Hide Nearest Platforms (for Profiles)
	DISPLAY_STATISTICS, // Display or Hide the Statistics (for Timeseries)
	DIVIDE_TS, // Divide TS
	EDIT_CLIMATO_ADDITIONAL_GRAPHS, // Validate the transformation of the climatology additional graphs
	EXCLUDE_ONLY_QC, // Enable/Disable the "exclude only" for the selected QC on graphs
	KEEP_BOUNDS, // Keep the bounds of the visible graphs when changing Station
	MOUSE_MODE_CHANGED, // The Mouse Mode changed (Zoom / Selection)
	REDO, //
	REMOVE_MEASURE, //
	RESET_NEAREST_PROFILES, //
	SHIFT, //
	TRANSCODE_PARAMETERS, //
	UNDO, //
	UNDO_ALL, //
	UPDATE_POSITION_GRAPHS, //
	ZOOM_IN, //
	ZOOM_INITIAL, //
	ZOOM_OUT,
    }

    /**
     *
     */
    private static final long serialVersionUID = -7409959263366363426L;

    private final long currentTime;

    private final EVENT_ENUM eventEnum;

    /**
     * Default constructor
     *
     * @param source
     * @param eventEnum
     */
    public SC3PropertyChangeEvent(final Object source, final EVENT_ENUM eventEnum) {
	super(source, eventEnum.toString(), null, null);
	this.eventEnum = eventEnum;
	currentTime = System.currentTimeMillis();
    }

    /**
     * @return the currentTime
     */
    public long getCurrentTime() {
	return currentTime;
    }

    /**
     * @return the eventEnum
     */
    public EVENT_ENUM getEventEnum() {
	return eventEnum;
    }
}
