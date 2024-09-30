package fr.ifremer.scoop3.gui.utils;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.jzy3d.chart.AWTChart;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.controllers.mouse.camera.AWTCameraMouseController;
import org.jzy3d.chart.controllers.mouse.selection.AWTAbstractMouseSelector;
import org.jzy3d.chart.controllers.thread.camera.CameraThreadController;
import org.jzy3d.plot3d.rendering.view.Renderer2d;

public class AWTScatterDualModeMouseSelector {

    public AWTScatterDualModeMouseSelector(final Chart chart, final AWTAbstractMouseSelector alternativeMouse) {
	build(chart, alternativeMouse);
    }

    private Chart build(final Chart chart, final AWTAbstractMouseSelector alternativeMouse) {
	this.chart = chart;
	this.mouseSelection = alternativeMouse;

	// Create and add controllers
	final CameraThreadController threadCamera = new CameraThreadController(chart);
	mouseCamera = new AWTCameraMouseController(chart);
	mouseCamera.addSlaveThreadController(threadCamera);
	chart.getCanvas().addKeyController(buildToggleKeyListener(chart));// ajoute le keyListener gérant la bascule
	// entre rotation et selection
	useCam(); // default mode is selection

	message = messageRotationMode;
	final Renderer2d messageRenderer = buildMessageRenderer();
	getAWTChart(chart).addRenderer(messageRenderer);
	return chart;
    }

    private AWTChart getAWTChart(final Chart chart) {
	return (AWTChart) chart;
    }

    private KeyListener buildToggleKeyListener(final Chart chart) {
	return new KeyListener() {
	    @Override
	    public void keyPressed(final KeyEvent e) {
		// empty method
	    }

	    @Override
	    public void keyReleased(final KeyEvent e) {
		if (e.getKeyChar() == 'c') {
		    useCam();
		    mouseSelection.clearLastSelection();
		    holding = true;
		    message = messageRotationMode;
		}
		chart.render(); // update message display
	    }

	    @Override
	    public void keyTyped(final KeyEvent e) {
		if (holding) {
		    if (e.getKeyChar() == 'c') {
			while (!chart.getControllers().isEmpty()) {
			    chart.removeController(mouseCamera);
			}
			releaseCam();
			holding = false;
			message = messageSelectionMode;
		    }
		    chart.render();
		}
	    }

	    protected boolean holding = true;
	};
    }

    private Renderer2d buildMessageRenderer() {
	return new Renderer2d() {
	    public void paint(final Graphics g) {
		if (DISPLAY_MESSAGE && (message != null)) {
		    g.setColor(java.awt.Color.RED);
		    g.drawString(message, 10, 30);
		}
	    }

	    @Override
	    public void paint(final Graphics g, final int canvasWidth, final int canvasHeight) {
		// empty method
	    }
	};
    }

    private void useCam() {
	mouseSelection.unregister();
	chart.addController(mouseCamera);
    }

    private void releaseCam() {
	chart.removeController(mouseCamera);
	mouseSelection.register(chart);
    }

    private Chart chart;

    private AWTCameraMouseController mouseCamera;
    private AWTAbstractMouseSelector mouseSelection;

    private static final boolean DISPLAY_MESSAGE = true;
    private String message;

    private static String messageSelectionMode = "";// "Current mouse mode : selection (release 'c' to switch to
						    // camera mode)";
    private static String messageRotationMode = "";// "Current mouse mode : camera (hold 'c' to switch to selection
						   // mode)";
}
