import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.io.*;
import javax.sound.sampled.*;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class userApplication {

	private static int CLIENT_PORT;
	private static byte[] SERVER_IP = { (byte) 155, (byte) 207, (byte) 18, (byte) 208 };
	private static int SERVER_PORT;
	private static String ECHO_REQUEST_CODE;
	private static String IMAGE_REQUEST_CODE;
	private static String AUDIO_REQUEST_CODE;
	private static String ITHAKICOPTER_CODE;
	private static String OBD_II_CODE;
	private static int COPTER_CLIENT_PORT = 48078;
	private static int COPTER_SERVER_PORT = 48078;
	private static File outputDir = new File(".");
	private static String workbookFile = "session.xlsx";

	public static XSSFWorkbook workbook = new XSSFWorkbook();

	public static ArrayList<echoPacket> echo(String request_code, long runTime) throws UnknownHostException {

		byte[] txbuffer = request_code.getBytes();

		InetAddress serverAddress = InetAddress.getByAddress(SERVER_IP);

		DatagramPacket request = new DatagramPacket(txbuffer, txbuffer.length, serverAddress, SERVER_PORT);

		byte[] rxbuffer = new byte[128];

		DatagramPacket response = new DatagramPacket(rxbuffer, rxbuffer.length);

		ArrayList<echoPacket> responses = new ArrayList<echoPacket>();
		long packetArrived = System.currentTimeMillis();
		long startOfProcess = System.currentTimeMillis();

		try (DatagramSocket socket = new DatagramSocket(CLIENT_PORT)) {

			do {

				long start = System.currentTimeMillis();

				socket.send(request);
				socket.setSoTimeout(3000);

				for (;;) {

					try {

						socket.receive(response);

						packetArrived = System.currentTimeMillis();
						String message = new String(response.getData(), 0, response.getLength());
						long timeResponse = packetArrived - start;
						echoPacket packet = new echoPacket(message, timeResponse);
						responses.add(packet);

					} catch (IOException y) {
						System.out.println(y);
					}

					break;
				}

			} while ((System.currentTimeMillis() - startOfProcess) <= runTime);

			socket.close();

		} catch (IOException x) {
			System.out.println(x);
		}

		return responses;

	}

	public static void temperature() throws UnknownHostException {

		byte[] txbuffer = (ECHO_REQUEST_CODE.replace("\r", "T00\r")).getBytes();

		InetAddress serverAddress = InetAddress.getByAddress(SERVER_IP);

		DatagramPacket request = new DatagramPacket(txbuffer, txbuffer.length, serverAddress, SERVER_PORT);

		byte[] rxbuffer = new byte[128];

		DatagramPacket response = new DatagramPacket(rxbuffer, rxbuffer.length);

		try (DatagramSocket socket = new DatagramSocket(CLIENT_PORT)) {

			socket.send(request);
			socket.setSoTimeout(3000);

			boolean received = false;

			for (;;) {

				try {

					socket.receive(response);
					received = true;

				} catch (IOException y) {
					System.out.println(y);
				}

				break;
			}

			if (received) {
				File file = new File(outputDir, ECHO_REQUEST_CODE.trim() + "_TEMP.txt");
				try {

					file.createNewFile();
					FileWriter writer = new FileWriter(file, false);
					writer.write(new String(response.getData(), 0, response.getLength()) + "\n");
					writer.close();

				} catch (IOException z) {
					System.out.println(z);
				}
			}

			socket.close();

		} catch (IOException x) {
			System.out.println(x);
		}
	}

	public static void exportExcel(ArrayList<echoPacket> packet, Boolean noDelay) throws IOException {

		XSSFSheet sheet = workbook.createSheet(noDelay ? "E000" : ECHO_REQUEST_CODE.trim());
		XSSFRow row = sheet.createRow(0);
		XSSFCell cell = row.createCell(0);
		cell.setCellValue("Message");
		cell = row.createCell(1);
		cell.setCellValue("Time Response");

		for (int i = 0; i < packet.size(); i++) {
			row = sheet.createRow(i + 1);
			cell = row.createCell(0);
			cell.setCellValue(packet.get(i).getMessage());
			cell = row.createCell(1);
			cell.setCellValue(packet.get(i).getTimeResponse());

		}

	}

	public static void exportExcel(ArrayList<ithakicopterPacket> packet, int count) throws IOException {

		XSSFSheet sheet = workbook.createSheet(ITHAKICOPTER_CODE + "#" + count);
		XSSFRow row = sheet.createRow(0);
		XSSFCell cell = row.createCell(0);
		cell.setCellValue("Packet String");
		cell = row.createCell(1);
		cell.setCellValue("Date time");
		cell = row.createCell(2);
		cell.setCellValue("lmotor");
		cell = row.createCell(3);
		cell.setCellValue("rmotor");
		cell = row.createCell(4);
		cell.setCellValue("Altitude");
		cell = row.createCell(5);
		cell.setCellValue("Temperature");
		cell = row.createCell(6);
		cell.setCellValue("Pressure");
		cell = row.createCell(7);
		cell.setCellValue("Response time");

		for (int i = 0; i < packet.size(); i++) {
			row = sheet.createRow(i + 1);
			cell = row.createCell(0);
			cell.setCellValue(packet.get(i).getPacketString());
			cell = row.createCell(1);
			cell.setCellValue(packet.get(i).getDatetime());
			cell = row.createCell(2);
			cell.setCellValue(packet.get(i).getLmotor());
			cell = row.createCell(3);
			cell.setCellValue(packet.get(i).getRmotor());
			cell = row.createCell(4);
			cell.setCellValue(packet.get(i).getAltitude());
			cell = row.createCell(5);
			cell.setCellValue(packet.get(i).getTemperature());
			cell = row.createCell(6);
			cell.setCellValue(packet.get(i).getPressure());
			cell = row.createCell(7);
			cell.setCellValue(packet.get(i).getResponseTime());
		}

	}

	public static void exportExcel(ArrayList<vehiclePacket> packet, String request) throws IOException {

		XSSFSheet sheet = workbook.createSheet(request);
		XSSFRow row = sheet.createRow(0);
		XSSFCell cell = row.createCell(0);
		cell.setCellValue("Engine run time");
		cell = row.createCell(1);
		cell.setCellValue("Intake air temperature");
		cell = row.createCell(2);
		cell.setCellValue("Throttle position");
		cell = row.createCell(3);
		cell.setCellValue("Engine RPM");
		cell = row.createCell(4);
		cell.setCellValue("Vehicle speed");
		cell = row.createCell(5);
		cell.setCellValue("Coolant temperature");

		for (int i = 0; i < packet.size(); i++) {

			row = sheet.createRow(i + 1);
			cell = row.createCell(0);
			cell.setCellValue(packet.get(i).getEngineRunTime());
			cell = row.createCell(1);
			cell.setCellValue(packet.get(i).getIntakeAirTemperature());
			cell = row.createCell(2);
			cell.setCellValue(packet.get(i).getThrottlePosition());
			cell = row.createCell(3);
			cell.setCellValue(packet.get(i).getEngineRPM());
			cell = row.createCell(4);
			cell.setCellValue(packet.get(i).getVehicleSpeed());
			cell = row.createCell(5);
			cell.setCellValue(packet.get(i).getCoolantTemperature());

		}

	}

	public static void exportExcel(String sheetname, String type, byte[] array) throws IOException {

		XSSFSheet sheet = workbook.createSheet(sheetname + "_" + type);
		XSSFRow row;
		XSSFCell cell;

		for (int i = 0; i < array.length; i++) {
			row = sheet.createRow(i);
			cell = row.createCell(0);
			cell.setCellValue(Integer.valueOf(array[i]));
		}
	}

	public static void exportExcel(String sheetname, String type, byte[][] array, int numAudioPackets)
			throws IOException {

		XSSFSheet sheet = workbook.createSheet(sheetname + "_" + type);
		XSSFRow row;
		XSSFCell cell;

		for (int i = 0; i < numAudioPackets; i++) {
			for (int j = 0; j < array[i].length; j++) {
				row = sheet.createRow((i * array[i].length) + j);
				cell = row.createCell(0);
				cell.setCellValue(Integer.valueOf(array[i][j]));
			}
		}
	}

	public static void image(String request_code) throws UnknownHostException {

		byte[] txbuffer = request_code.getBytes();

		InetAddress serverAddress = InetAddress.getByAddress(SERVER_IP);

		DatagramPacket request = new DatagramPacket(txbuffer, txbuffer.length, serverAddress, SERVER_PORT);

		byte[] rxbuffer = new byte[128];

		DatagramPacket response = new DatagramPacket(rxbuffer, rxbuffer.length);

		File imagePath = new File(outputDir, request_code + ".jpg");
		try (FileOutputStream imageFile = new FileOutputStream(imagePath)) {

			try (DatagramSocket socket = new DatagramSocket(CLIENT_PORT)) {

				socket.send(request);
				socket.setSoTimeout(1400);

				boolean startFound = false;
				int startOfImageDelimiter = 0;

				for (;;) {

					try {

						socket.receive(response);

					} catch (IOException x) {
						System.out.println(x);
						break;
					}

					byte[] data = response.getData();
					int length = response.getLength();

					if (startFound)

						imageFile.write(data, 0, length);

					else {

						for (int i = 1; i < length; i++) {
							if ((data[i - 1] == (byte) 0xFF) && (data[i] == (byte) 0xD8)) {
								startOfImageDelimiter = i - 1;
								startFound = true;
								System.out.println("Found Start!!!" + startOfImageDelimiter);
								imageFile.write(data, startOfImageDelimiter, length - startOfImageDelimiter);
								break;
							}
						}
					}

					if (length >= 2 && data[length - 2] == (byte) 0xFF && data[length - 1] == (byte) 0xD9)
						break;

				}

				socket.close();

			} catch (IOException ioe) {
				System.out.println(ioe);
			}

			imageFile.flush();
			imageFile.close();

		} catch (Exception y) {
			System.out.println(y);
		}
	}

	public static void audio(String request_code, boolean isAdaptive, int numAudioPackets) throws UnknownHostException {

		byte[] txbuffer = request_code.getBytes();

		InetAddress serverAddress = InetAddress.getByAddress(SERVER_IP);

		DatagramPacket request = new DatagramPacket(txbuffer, txbuffer.length, serverAddress, SERVER_PORT);

		byte[] rxbuffer = new byte[132];

		DatagramPacket response = new DatagramPacket(rxbuffer, rxbuffer.length);

		int packetsArrived = 0;
		byte[][] elasticBuffer = new byte[numAudioPackets][128 * 2 * (isAdaptive ? 2 : 1)];
		byte[][] rawBytes = new byte[numAudioPackets][128 * 2];
		byte[] mBytes = new byte[numAudioPackets];
		byte[] bBytes = new byte[numAudioPackets];

		try (DatagramSocket socket = new DatagramSocket(CLIENT_PORT)) {

			socket.send(request);
			socket.setSoTimeout(1400);

			while (packetsArrived != numAudioPackets) {

				try {

					socket.receive(response);

				} catch (IOException x) {
					System.out.println(x);
					break;
				}

				int m = 0;
				int b = 2;
				int leftCompressedByte = 0;
				int rightCompressedByte = 0;
				int leftByte = 0;
				int rightByte = 0;

				if (isAdaptive) {

					byte[] mm = new byte[4];
					byte sign = (byte) ((response.getData()[1] & 0x80) != 0 ? 0xff : 0x00);
					mm[3] = sign;
					mm[2] = sign;
					mm[1] = response.getData()[1];
					mm[0] = response.getData()[0];
					m = ByteBuffer.wrap(mm).order(ByteOrder.LITTLE_ENDIAN).getInt();
					mBytes[packetsArrived] = (byte) m;

					sign = (byte) ((response.getData()[3] & 0x80) != 0 ? 0xff : 0x00);
					byte[] bb = new byte[4];
					bb[3] = sign;
					bb[2] = sign;
					bb[1] = response.getData()[3];
					bb[0] = response.getData()[2];
					b = ByteBuffer.wrap(bb).order(ByteOrder.LITTLE_ENDIAN).getInt();
					bBytes[packetsArrived] = (byte) b;

					int ll;
					int rr;
					int pos;
					int prev = 0;
					for (int i = 0; i < 128; i++) {
						int pair = (int) response.getData()[i + 4];
						pos = 4 * i;

						leftCompressedByte = (pair >>> 4) & 15;
						rightCompressedByte = pair & 15;
						leftByte = (leftCompressedByte - 8);
						rightByte = (rightCompressedByte - 8);
						rawBytes[packetsArrived][pos / 2] = (byte) leftByte;
						rawBytes[packetsArrived][(pos / 2) + 1] = (byte) rightByte;
						leftByte *= b;
						rightByte *= b;

						ll = prev + leftByte + m;
						prev = rightByte;
						rr = leftByte + rightByte + m;

						elasticBuffer[packetsArrived][pos + 0] = (byte) (ll & 0x000000FF);
						elasticBuffer[packetsArrived][pos + 1] = (byte) ((ll & 0x0000FF00) >> 8);
						elasticBuffer[packetsArrived][pos + 2] = (byte) (rr & 0x000000FF);
						elasticBuffer[packetsArrived][pos + 3] = (byte) ((rr & 0x0000FF00) >> 8);
					}

				} else {

					for (int i = 0; i < 128; i++) {
						int pair = (int) response.getData()[i];
						leftCompressedByte = (pair >>> 4) & 15;
						rightCompressedByte = pair & 15;
						leftByte = (leftCompressedByte - 8);
						rightByte = (rightCompressedByte - 8);
						int pos = i * 2;
						rawBytes[packetsArrived][pos] = (byte) leftByte;
						rawBytes[packetsArrived][pos + 1] = (byte) rightByte;
						leftByte *= b;
						rightByte *= b;
						if (i == 0)
							elasticBuffer[packetsArrived][i] = (byte) leftByte;
						else
							elasticBuffer[packetsArrived][pos] = (byte) (leftByte
									+ (int) elasticBuffer[packetsArrived][pos - 1]);
						elasticBuffer[packetsArrived][pos
								+ 1] = (byte) (rightByte + (int) elasticBuffer[packetsArrived][pos]);
					}

				}

				packetsArrived++;

			}

			socket.close();

		} catch (IOException x) {
			System.out.println(x);
		}

		AudioFormat linearPCM = new AudioFormat(8000, (isAdaptive ? 16 : 8), 1, true, false);
		try (SourceDataLine lineOut = AudioSystem.getSourceDataLine(linearPCM)) {

			lineOut.open(linearPCM, 32000);
			lineOut.start();
			for (int i = 0; i < packetsArrived; i++) {
				lineOut.write(elasticBuffer[i], 0, elasticBuffer[i].length);
			}
			lineOut.stop();
			lineOut.close();

		} catch (LineUnavailableException lue) {
			System.out.println(lue);
		}

		try {
			exportExcel(request_code, "Samples", elasticBuffer, numAudioPackets);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			exportExcel(request_code, "Subs", rawBytes, numAudioPackets);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (isAdaptive) {
			try {
				exportExcel(request_code, "M", mBytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				exportExcel(request_code, "B", bBytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static ArrayList<ithakicopterPacket> ithakiCopter(long runTime) throws UnknownHostException {

		byte[] txbuffer = ITHAKICOPTER_CODE.getBytes();

		InetAddress serverAddress = InetAddress.getByAddress(SERVER_IP);

		DatagramPacket request = new DatagramPacket(txbuffer, txbuffer.length, serverAddress, COPTER_SERVER_PORT);

		byte[] rxbuffer = new byte[128];

		DatagramPacket response = new DatagramPacket(rxbuffer, rxbuffer.length);

		ArrayList<ithakicopterPacket> responses = new ArrayList<ithakicopterPacket>();
		long packetArrived = System.currentTimeMillis();
		long startOfProcess = System.currentTimeMillis();

		try (DatagramSocket socket = new DatagramSocket(COPTER_CLIENT_PORT)) {

			do {

				long start = System.currentTimeMillis();

				socket.send(request);
				socket.setSoTimeout(3000);

				for (;;) {

					try {

						socket.receive(response);
						packetArrived = System.currentTimeMillis();

						String message = new String(response.getData(), 0, response.getLength());
						long timeResponse = packetArrived - start;
						ithakicopterPacket packet = new ithakicopterPacket(message, timeResponse);
						responses.add(packet);

					} catch (IOException y) {
						System.out.println(y);
					}

					break;
				}

			} while ((System.currentTimeMillis() - startOfProcess) <= runTime);

			socket.close();

		} catch (IOException x) {
			System.out.println(x);
		}

		return responses;

	}

	public static ArrayList<vehiclePacket> vehicle(long runTime) throws UnknownHostException {

		InetAddress serverAddress = InetAddress.getByAddress(SERVER_IP);

		byte[] rxbuffer = new byte[2048];

		DatagramPacket response = new DatagramPacket(rxbuffer, rxbuffer.length);

		String[] pid = { "1F", "0F", "11", "0C", "0D", "05" };

		ArrayList<vehiclePacket> responses = new ArrayList<vehiclePacket>();
		long packetArrived = System.currentTimeMillis();
		long startOfProcess = System.currentTimeMillis();

		try (DatagramSocket socket = new DatagramSocket(CLIENT_PORT)) {

			do {

				vehiclePacket packet = new vehiclePacket();
				boolean complete = true;

				for (int i = 0; i < pid.length; i++) {

					byte[] txbuffer = (OBD_II_CODE + "OBD=01 " + pid[i] + "\r").getBytes();
					DatagramPacket request = new DatagramPacket(txbuffer, txbuffer.length, serverAddress, SERVER_PORT);

					socket.send(request);
					socket.setSoTimeout(8000);

					for (;;) {

						try {

							socket.receive(response);
							packetArrived = System.currentTimeMillis();

						} catch (IOException y) {
							System.out.println(y);
							complete = false;
							break;
						}

						if (response.getLength() < 6) {
							complete = false;
							break;
						}

						String message = new String(response.getData(), 0, response.getLength()).substring(6);

						switch (pid[i]) {
						case "1F":
							packet.setEngineRunTime(message);
							break;
						case "0F":
							packet.setIntakeAirTemperature(message);
							break;
						case "11":
							packet.setThrottlePosition(message);
							break;
						case "0C":
							packet.setEngineRPM(message);
							break;
						case "0D":
							packet.setVehicleSpeed(message);
							break;
						case "05":
							packet.setCoolantTemperature(message);
							break;
						default:
							System.out.println("Error\n");
							break;
						}
						break;
					}

				}

				if (complete)
					responses.add(packet);

			} while ((System.currentTimeMillis() - startOfProcess) <= runTime);

			socket.close();

		} catch (IOException x) {
			System.out.println(x);
		}

		return responses;

	}

	public static void main(String[] args) throws Exception {

		Config config = new Config(args.length > 0 ? args[0] : "ithaki.properties");
		CLIENT_PORT = Integer.parseInt(config.get("client_port"));
		SERVER_PORT = Integer.parseInt(config.get("server_port"));
		ECHO_REQUEST_CODE = config.get("echo_request_code") + "\r";
		IMAGE_REQUEST_CODE = config.get("image_request_code");
		AUDIO_REQUEST_CODE = config.get("audio_request_code");
		ITHAKICOPTER_CODE = config.get("ithakicopter_code");
		OBD_II_CODE = config.get("obd_ii_code");
		String[] address = config.optional("server_ip", "155.207.18.208").split("\\.");
		SERVER_IP = new byte[] { (byte) Integer.parseInt(address[0]), (byte) Integer.parseInt(address[1]),
				(byte) Integer.parseInt(address[2]), (byte) Integer.parseInt(address[3]) };
		COPTER_CLIENT_PORT = Integer.parseInt(config.optional("copter_client_port", "48078"));
		COPTER_SERVER_PORT = Integer.parseInt(config.optional("copter_server_port", "48078"));
		outputDir = new File(config.optional("output_dir", "."));
		outputDir.mkdirs();
		workbookFile = config.optional("workbook_file", "session.xlsx");

		temperature();

		exportExcel(ithakiCopter(90000), 1);

		exportExcel(ithakiCopter(90000), 2);

		image(IMAGE_REQUEST_CODE);

		image(IMAGE_REQUEST_CODE + "CAM=PTZ");

		audio(AUDIO_REQUEST_CODE + "T300", false, 300);

		audio(AUDIO_REQUEST_CODE + "F300", false, 300);

		audio(AUDIO_REQUEST_CODE + "L01F300", false, 300);

		audio(AUDIO_REQUEST_CODE + "AQL01F300", true, 300);

		audio(AUDIO_REQUEST_CODE + "AQL02F300", true, 300);

		exportExcel(vehicle(240000), OBD_II_CODE);

		exportExcel(echo(ECHO_REQUEST_CODE, 300000), false);

		exportExcel(echo("E0000\r", 300000), true);

		File path = new File(outputDir, workbookFile);
		workbook.write(new FileOutputStream(path));
		workbook.close();

	}

}
