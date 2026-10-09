public class echoPacket {
	
	private String message;
	private long timeResponse;
	private boolean correct;
	private double ber;
	private double nack;
	
	public echoPacket(String message, long timeResponse, boolean correct) {
		super();
		this.message = message;
		this.timeResponse = timeResponse;
		this.correct = correct;
		this.ber = 0;
		this.nack = 0;
	}


	public echoPacket(String message, boolean correct) {
		super();
		this.message = message;
		this.correct = correct;
		this.ber = 0;
		this.timeResponse = -1;
		this.nack = 0;
	}


	public boolean isCorrect() {
		return correct;
	}


	public void setCorrect(boolean correct) {
		this.correct = correct;
	}


	public echoPacket() {}


	public echoPacket(String message, long timeResponse) {
		this.message = message;
		this.timeResponse = timeResponse;
		this.ber = 0;
		this.nack = 0;
	}


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


	public double getBer() {
		return ber;
	}


	public void setBer(double ber) {
		this.ber = ber;
	}


	public double getNack() {
		return nack;
	}


	public void setNack(double nack) {
		this.nack = nack;
	}	
	
}
