package fr.ifremer.scoop3.bathyClimato.climato;

import java.util.Map;


public class Climatology {
	
	
	private String code = "";
	private String type = "";
	private String fileType = "";
	private String name = "";
	private String directory = "";
	private String stdDevDirectory = "";
	private float step = 0;
	// Clef : mois, valeur : saison
	private Map<Integer , Integer> monthes;
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDirectory() {
		return directory;
	}
	public void setDirectory(String directory) {
		this.directory = directory;
	}
	public float getStep() {
		return step;
	}
	public void setStep(float step) {
		this.step = step;
	}
	public Map<Integer, Integer> getMonthes() {
		return monthes;
	}
	public void setMonthes(Map<Integer, Integer> monthes) {
		this.monthes = monthes;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public String getStdDevDirectory() {
		return stdDevDirectory;
	}
	public void setStdDevDirectory(String stdDevDirectory) {
		this.stdDevDirectory = stdDevDirectory;
	}
	public String getAgregatedCode() {
		return name + "-" + type;
	}
	
	
	

}
