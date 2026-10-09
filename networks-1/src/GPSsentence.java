public class GPSsentence {
	private String header;
	private int time;
	private int northLatitude1;
	private int northLatitude2;
	private int eastLatitude1;
	private int eastLatitude2;
	private String T;
	
	public GPSsentence(String header, int time, int northLatitude1, int northLatitude2, int eastLatitude1,
			int eastLatitude2) {
		super();
		this.header = header;
		this.time = time;
		this.northLatitude1 = northLatitude1;
		this.northLatitude2 = (int) (((double)northLatitude2)*0.006);
		this.eastLatitude1 = eastLatitude1;
		this.eastLatitude2 = (int) (((double)eastLatitude2)*0.006);
		this.T = "T=" + this.eastLatitude1 + this.eastLatitude2 + this.northLatitude1 + this.northLatitude2;
	}

	public GPSsentence() {
		super();
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getNorthLatitude1() {
		return northLatitude1;
	}

	public void setNorthLatitude1(int northLatitude1) {
		this.northLatitude1 = northLatitude1;
	}

	public int getNorthLatitude2() {
		return northLatitude2;
	}

	public void setNorthLatitude2(int northLatitude2) {
		this.northLatitude2 = northLatitude2;
	}

	public int getEastLatitude1() {
		return eastLatitude1;
	}

	public void setEastLatitude1(int eastLatitude1) {
		this.eastLatitude1 = eastLatitude1;
	}

	public int getEastLatitude2() {
		return eastLatitude2;
	}

	public void setEastLatitude2(int eastLatitude2) {
		this.eastLatitude2 = eastLatitude2;
	}

	public String getT() {
		return T;
	}

	public void setT(String t) {
		T = t;
	}
	
}
