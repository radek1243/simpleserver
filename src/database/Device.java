package database;

public class Device {

	private int type;
	private int model;
	private String sn;
	private String sn2;
	private String stan;
	private String desc;
	
	public Device() {
		this.reset();
	}
	
	public void setType(int type){
		this.type = type;
	}
	
	public void setModel(int model){
		this.model = model;
	}
	
	public void setSN(String sn){
		this.sn = sn;
	}
	
	public void setSN2(String sn2) {
		this.sn2=sn2;
	}
	
	public void setStan(String stan){
		this.stan = stan;
	}
	
	public void setDesc(String desc){
		this.desc = desc;
	}
	
	public int getType(){
		return this.type;
	}
	
	public int getModel(){
		return this.model;
	}
	
	public String getSN(){
		return this.sn;
	}
	
	public String getSN2() {
		return this.sn2;
	}
	
	public String getStan(){
		return this.stan;
	}
	
	public String getDesc(){
		return this.desc;
	}
	
	public void reset(){
		this.type = -1;
		this.model = -1;
		this.sn = null;
		this.sn2 = null;
		this.stan = null;
		this.desc = null;
	}
}
