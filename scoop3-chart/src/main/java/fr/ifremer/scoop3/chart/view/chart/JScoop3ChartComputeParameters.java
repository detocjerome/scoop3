package fr.ifremer.scoop3.chart.view.chart;

import java.awt.Point;
import java.util.List;

import fr.ifremer.scoop3.chart.view.additionalGraphs.AdditionalGraph;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;

public class JScoop3ChartComputeParameters {

    Dimension2D imageDimension;
    Point imagePosition;
    Rectangle2D dataAreaForZoomLevelOne;
    Rectangle2D dataAreaForZoomLevelCurrent;
    int horizontalScrollBarValue;
    int verticalScrollBarValue;
    List<AdditionalGraph> additionalGraphs;
    boolean isSuperposedMode;
    int currentLevel;
    protected int firstObservationIndex;
    protected int lastObservationIndex;
    boolean displayPoints = true;
}
