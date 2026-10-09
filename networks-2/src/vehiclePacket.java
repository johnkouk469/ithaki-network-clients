public class vehiclePacket {
	
	private int engineRunTime;
	private int intakeAirTemperature;
	private float throttlePosition;
	private float engineRPM;
	private int vehicleSpeed;
	private int coolantTemperature;
	
	public vehiclePacket() {}
	
	public void setEngineRunTime(String string) {
		String[] s = string.split(" ");
	    int xx = Integer.parseInt(s[0], 16);
	    int yy = Integer.parseInt(s[1], 16);
	    this.engineRunTime = (256 * xx) + yy;
	}
	
	public void setIntakeAirTemperature(String string) {
		this.intakeAirTemperature = Integer.parseInt(string, 16) - 40;
	}
	
	public void setThrottlePosition(String string) {
		this.throttlePosition = Integer.parseInt(string, 16) * 100 / 255;
	}
	
	public void setEngineRPM(String string) {
		String[] s = string.split(" ");
		int xx = Integer.parseInt(s[0], 16);
	    int yy = Integer.parseInt(s[1], 16);
	    this.engineRPM = ((xx * 256) + yy) / 4;
	}
	
	public void setVehicleSpeed(String string) {
		this.vehicleSpeed = Integer.parseInt(string, 16);
	}
	
	public void setCoolantTemperature(String string) {
		this.coolantTemperature = Integer.parseInt(string, 16) - 40; 
	}

	public int getEngineRunTime() {
		return engineRunTime;
	}

	public int getIntakeAirTemperature() {
		return intakeAirTemperature;
	}

	public float getThrottlePosition() {
		return throttlePosition;
	}

	public float getEngineRPM() {
		return engineRPM;
	}

	public int getVehicleSpeed() {
		return vehicleSpeed;
	}

	public int getCoolantTemperature() {
		return coolantTemperature;
	}	
	
}
