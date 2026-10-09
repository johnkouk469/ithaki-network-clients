public class ithakicopterPacket {

	private String packetString;
	private  String datetime;
	private  int lmotor;
	private  int rmotor;
	private  int altitude;
	private  float temperature;
	private  float pressure;
	private  long responseTime;

	public ithakicopterPacket(String packetString, long responseTime) {
	    String[] splitted = packetString.split(" ");
	    assert (splitted[0] == "ITHAKICOPTER");
	    this.packetString = packetString;
	    this.datetime = splitted[1] + splitted[2];
	    this.lmotor = Integer.valueOf(splitted[3].substring(7));
	    this.rmotor = Integer.valueOf(splitted[4].substring(7));
	    this.altitude = Integer.valueOf(splitted[5].substring(9));
	    this.temperature = Float.valueOf(splitted[6].substring(12));
	    this.pressure = Float.valueOf(splitted[7].substring(9));
	    this.responseTime = responseTime;
	}

	public String getPacketString() {
		return packetString;
	}

	public void setPacketString(String packetString) {
		this.packetString = packetString;
	}

	public String getDatetime() {
		return datetime;
	}

	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}

	public int getLmotor() {
		return lmotor;
	}

	public void setLmotor(int lmotor) {
		this.lmotor = lmotor;
	}

	public int getRmotor() {
		return rmotor;
	}

	public void setRmotor(int rmotor) {
		this.rmotor = rmotor;
	}

	public int getAltitude() {
		return altitude;
	}

	public void setAltitude(int altitude) {
		this.altitude = altitude;
	}

	public float getTemperature() {
		return temperature;
	}

	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}

	public float getPressure() {
		return pressure;
	}

	public void setPressure(float pressure) {
		this.pressure = pressure;
	}

	public long getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(long responseTime) {
		this.responseTime = responseTime;
	}
	
}
