public class echoPacket {
	
	private String message;
	private long timeResponse;
		
	public echoPacket(String message, long timeResponse) {
		super();
		this.message = message;
		this.timeResponse = timeResponse;
	}


	public echoPacket(String message) {
		super();
		this.message = message;		
		this.timeResponse = -1;
		
	}

	public echoPacket() {}

	
	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public long getTimeResponse() {
		return timeResponse;
	}


	public void setTimeResponse(long timeResponse) {
		this.timeResponse = timeResponse;
	}
	
}
