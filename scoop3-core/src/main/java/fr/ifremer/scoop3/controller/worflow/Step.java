package fr.ifremer.scoop3.controller.worflow;

/**
 * 
 * @author altran
 * 
 */
public interface Step {

	public boolean isEnabled();
	
	public void setEnabled(boolean enabled);
	
	public String getCode();
}
