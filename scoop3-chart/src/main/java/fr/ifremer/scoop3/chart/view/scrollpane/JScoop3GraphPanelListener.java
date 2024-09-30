package fr.ifremer.scoop3.chart.view.scrollpane;

import java.awt.Point;
import java.util.EventListener;

import javafx.geometry.Rectangle2D;

public interface JScoop3GraphPanelListener extends EventListener {

    public void changeCurrentStation(int[] newCurrentStation);

    public void selectionDoneWithOtherMouseMode(Rectangle2D displayChartTotalArea, Rectangle2D displayChartSelectArea,
	    Point displayChartSelectAreaNewStartPoint, Point displayChartSelectAreaNewEndPoint);

    public void selectionDoneWithRemovalMode(Rectangle2D displayChartTotalArea, Rectangle2D displayChartSelectArea,
	    Point displayChartSelectAreaNewStartPoint, Point displayChartSelectAreaNewEndPoint);

    public void zoomAll();

    public void zoomOnDisplayArea(Rectangle2D displayChartTotalArea, Rectangle2D displayChartSelectArea,
	    Point displayChartSelectAreaNewStartPoint, Point displayChartSelectAreaNewEndPoint, boolean zoomOnGraph,
	    String sourceClass);

}
