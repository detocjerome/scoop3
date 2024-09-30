/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ifremer.scoop3.gui.jzy3dManager;

import java.awt.Dimension;

import javax.swing.JPanel;

import org.jzy3d.plot3d.rendering.canvas.CanvasAWT;

import fr.ifremer.scoop3.data.SuperposedModeEnum;
import fr.ifremer.scoop3.gui.data.DataFrame3D;
import fr.ifremer.scoop3.gui.utils.PlotGraph3DException;
import fr.ifremer.scoop3.gui.utils.PlotGraphs3D;
import fr.ifremer.scoop3.model.Dataset;

public class Jzy3dManager {
    public enum Type3d {
	DELAUNEY, SCATTER, NONE, BLANK
    }

    public enum DataFrameType {
	NORMAL, FIRST_ONLY
    }

    private final JPanel jzy3dPanel;
    private final String getXJComboBox;
    private final String getYJComboBox;
    private final String getZJComboBox;
    private final String colorMapComboBox;
    private PlotGraphs3D plotGraphs3D = null;
    private final DataFrame3D dataFrame;
    private final Type3d type3d;
    private final Dataset dataset;
    private boolean errorInJzy3dManager = false;

    /**
     * Constructeur de graph 3D Delauney ou Scatter
     *
     * @param dataset
     * @param type3d
     * @param getXJComboBox
     * @param getYJComboBox
     * @param getZJComboBox
     * @param colorMapComboBox
     * @throws PlotGraph3DException
     */
    public Jzy3dManager(final Dataset dataset, final Type3d type3d, final String getXJComboBox,
	    final String getYJComboBox, final String getZJComboBox, final String colorMapComboBox,
	    final DataFrameType pythonType, final Double panelHeight, final Dimension jzy3dSize,
	    final SuperposedModeEnum superposedModeEnum, final String currentObservationIndex)
	    throws PlotGraph3DException {

	// LECTURE D'UN DATASET SCOOP
	this.getXJComboBox = getXJComboBox;
	this.getYJComboBox = getYJComboBox;
	this.getZJComboBox = getZJComboBox;
	this.colorMapComboBox = colorMapComboBox;
	this.type3d = type3d;
	this.dataset = dataset;

	// REFORMATTAGE DU DATASET EN DATAFRAME
	dataFrame = new DataFrame3D(this.dataset, this.getXJComboBox, this.getYJComboBox, this.getZJComboBox,
		this.colorMapComboBox, pythonType, superposedModeEnum, currentObservationIndex);

	// INITIALISATION DU MANAGER ET TRACE DU GRAPHIQUE PAR DEFAUT
	jzy3dPanel = new javax.swing.JPanel();
	if (jzy3dSize != null) {
	    jzy3dPanel.setSize(jzy3dSize);
	}
	plotGraphs3D = new PlotGraphs3D(jzy3dPanel, this.colorMapComboBox, this.getZJComboBox, panelHeight);
	plotGraphs3D.loadDataFrame(dataFrame);
	if (type3d == Type3d.DELAUNEY) {
	    plotGraphs3D.plotDelaunayTessellator(this.getXJComboBox, this.getYJComboBox, this.getZJComboBox,
		    dataFrame.getUseCycleNumber());
	}
	if (type3d == Type3d.SCATTER) {
	    plotGraphs3D.plotScatter(this.getXJComboBox, this.getYJComboBox, this.getZJComboBox,
		    dataFrame.getUseCycleNumber());
	}
	// check if there is an error in the graph or not
	if ((((CanvasAWT) jzy3dPanel.getComponent(0)).getView().getScale().getMin() == 0f)
		&& (((CanvasAWT) jzy3dPanel.getComponent(0)).getView().getScale().getMax() == 0f)) {
	    errorInJzy3dManager = true;
	}
    }

    public JPanel getJzy3dPanel() {
	return this.jzy3dPanel;
    }

    public PlotGraphs3D getPlotGraphs3D() {
	return this.plotGraphs3D;
    }

    public String getXJComboBox() {
	return this.getXJComboBox;
    }

    public String getYJComboBox() {
	return this.getYJComboBox;
    }

    public String getZJComboBox() {
	return this.getZJComboBox;
    }

    public String getColorMapComboBox() {
	return this.colorMapComboBox;
    }

    public Dataset getDataset() {
	return this.dataset;
    }

    public Type3d getType() {
	return this.type3d;
    }

    public DataFrame3D getDataframe() {
	return this.dataFrame;
    }

    public boolean getErrorInJzy3dManager() {
	return this.errorInJzy3dManager;
    }
}
