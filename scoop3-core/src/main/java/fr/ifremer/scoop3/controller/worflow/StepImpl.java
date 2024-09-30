package fr.ifremer.scoop3.controller.worflow;


/**
 * 
 * @author altran
 *
 */
public class StepImpl implements Step {
	
	protected boolean enabled;
	protected StepCode stateCode;
	
	
	/**
	 * Constructor
	 * Default enable state : true
	 * @param stateCode
	 */
	public StepImpl(StepCode stateCode) {
		this.enabled = true;
		this.stateCode = stateCode;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String getCode() {
		return stateCode.toString();
	}
}
