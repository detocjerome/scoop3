package fr.ifremer.scoop3.bathyClimato.climato;

public class ClimatoRequest {
	
	protected String gf3 ;
	protected Float latitude ;
	protected Float longitude ;
	protected Integer month ;
	protected String climatoCode ;
	
//	public ClimatoRequest (String gf3, 
//				           Float latitude, 
//				           Float longitude, 
//				           Integer month, 
//				           String climatoCode){
//		
//		this.gf3= gf3 ;
//		this.latitude= latitude ;
//		this.longitude = longitude ;
//		this.month= month ;
//		this.climatoCode = climatoCode ;
//		
//	}
	
	public String getGf3 (){
		return this.gf3 ;
	}
	
	public Float getLatitude (){
		return this.latitude ;
	}
	
	public Float getLongitude (){
		return this.longitude ;
	}
	
	public Integer getMonth (){
		return this.month ;
	}
	
	public String getClimatoCode (){
		return this.climatoCode ;
	}

	public void setClimatoCode(String climatoCode) {
		this.climatoCode = climatoCode;
	}

	public void setGf3(String gf3) {
		this.gf3 = gf3;
	}

	public void setLatitude(Float latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(Float longitude) {
		this.longitude = longitude;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}
	
}