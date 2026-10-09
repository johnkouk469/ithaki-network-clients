import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.lang.System;
import javax.imageio.*;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ithakimodem.Modem;

public class userApplication {
	
	public static Modem modem = new Modem();	
	public static String echo_request_code;
	public static String image_request_code;
	public static String image_request_code_with_errors;
	public static String GPS_request_code;
	public static String ACK_result_code;
	public static String NACK_result_code;
	public static File outputDir = new File(".");
	
	
	public static String packetRequest(String request) {
		String response = "";
		int k;
		modem.write(request.getBytes());

		for (;;) {

			try {

				k = modem.read();

				if (k == -1) {
					System.out.println("connection closed");
					break;
				}

				response += (char) k;

				if (response.indexOf("PSTOP") != -1) {
					System.out.println("packet is here");
					break;
				}

			} catch (Exception x) {
				System.out.println(x);
				break;
			}
		}
		return response;
	}
	
	public static ArrayList<echoPacket> echoPacketRequest(long runTime) {

		ArrayList<echoPacket> results = new ArrayList<echoPacket>();
		long packet_arrived = System.currentTimeMillis();
		long start_of_process = System.currentTimeMillis();

		do {

			long start = System.currentTimeMillis();
			String response = packetRequest(echo_request_code);
			packet_arrived = System.currentTimeMillis();

			long timeResponse = packet_arrived - start;
			echoPacket packet = new echoPacket(response, timeResponse);

			results.add(packet);
		} while ((packet_arrived - start_of_process) <= runTime);
		
		return results;
	}
	
	public static byte[] createByteArray(ArrayList<Integer> rawData) {
		int startOfImageDelimiter=0;
		for(int i=2; i<rawData.size(); i++) {
			if(rawData.get(i-1) == 255 && rawData.get(i) == 216) {
				startOfImageDelimiter = i-1;
				break;
			}
		}
		
		byte[] img = new byte[rawData.size() - startOfImageDelimiter];
		for (int i = startOfImageDelimiter; i < rawData.size(); i++) {
			img[i-startOfImageDelimiter] = rawData.get(i).byteValue();
		}		
		return img;
	}
	
	public static boolean createJPEG(byte[] img, String request) throws Exception {
		try {
			
			ByteArrayInputStream bis = new ByteArrayInputStream(img);
			BufferedImage bufferedImageFromConvert = ImageIO.read(bis);
		
			File outputFile = new File(outputDir, request.replace('\r', '.') + "jpg");

			ImageIO.write(bufferedImageFromConvert, "jpg", outputFile);
			System.out.println("Image saved at " + outputFile.getPath());
			return true;
		}catch (Exception x) {
			System.out.println("Exception occured: " + x.getMessage());
			return false;
		}
	}
	
	public static boolean ImageRequest(String request) throws Exception {
		
		int k;		
		ArrayList<Integer> rawData = new ArrayList<Integer>();		
		modem.write(request.getBytes());		
		for (;;) {
			k = modem.read();
			if (k == -1) {
				System.out.println("connection closed!");
				break;
			}
			
			rawData.add(k);
			
			if (rawData.size() >= 3) {
				if (rawData.get(rawData.size() - 2) == 255 && k == 217) {
					System.out.println("image packet is here!");
					break;
				}
			}
		}
		return createJPEG(createByteArray(rawData), request);
		
	}
	
	public static int iterateGPSresponse(int i, String response) {
		if(i==0)
			return response.indexOf("START ITHAKI GPS TRACKING\r\n")+27;
		else if(i+1 == response.indexOf("STOP ITHAKI GPS TRACKIN"))
			return -1;
		else
			return response.indexOf("\n$GP", i+1);
	}
	
	public static void GPSrequest() throws NumberFormatException {
		int k;
		String response = "";
		modem.write((GPS_request_code + "R=1003090\r").getBytes());
		for(;;) {
			k=modem.read();
			if(k == -1) {
				System.out.println("connection closed!");
				break;
			}
			response += (char) k;			
			if (response.indexOf("STOP ITHAKI GPS TRACKING") != -1) {
				System.out.println("packet is here");
				break;
			}
			
			System.out.print((char)k);
		}
		
		if (response.indexOf("START ITHAKI GPS TRACKING") == -1) {
			System.out.println("No GPS data received.");
			return;
		}
		
		ArrayList<GPSsentence> sentences = new ArrayList<GPSsentence>();
		k=0;
		int count =0;
		while(k>=0) {
			k = iterateGPSresponse(k, response);
			count++;
			if(count % 10 == 0) {
				String header = response.substring(k+1, k+7);
				int time = Integer.parseInt(response.substring(k+8, k+14));
				int northLatitude1 = Integer.parseInt(response.substring(k+19, k+23));
				int northLatitude2 = Integer.parseInt(response.substring(k+24, k+28));
				int eastLatitude1 = Integer.parseInt(response.substring(k+32, k+36));
				int eastLatitude2 = Integer.parseInt(response.substring(k+37, k+41));
				GPSsentence sentence = new GPSsentence(header, time, northLatitude1, northLatitude2, eastLatitude1, eastLatitude2);
				sentences.add(sentence);
			}
		}
		
		for(int i=0; i<sentences.size(); i++) {
			GPS_request_code += sentences.get(i).getT();
		}
		GPS_request_code += "\r";
		System.out.println(GPS_request_code);
		try {
			ImageRequest(GPS_request_code);
		}catch(Exception x) {
			System.out.println(x);
		}
		
	}
	
	public static echoPacket ACK_NACK(String request) throws NumberFormatException {
		
		String response = packetRequest(request);
		
		if (response.isEmpty())
			return null;
		if (response.indexOf("<") == -1 || response.indexOf(">") == -1 || response.indexOf(" PSTOP") == -1)
			return new echoPacket(response, false);
		
		String sequence = response.substring(response.indexOf("<")+1, response.indexOf(">"));
		String fcs = response.substring(response.indexOf(">")+2, response.indexOf(" PSTOP"));
		
		int xor = sequence.charAt(0)^sequence.charAt(1);
		for(int i=2; i<sequence.length(); i++) {
			xor = xor ^ sequence.charAt(i);
		}
		echoPacket packet;
		int FCS = Integer.parseInt(fcs);
		if(xor==FCS)
			packet = new echoPacket(response, true);
		else
			packet = new echoPacket(response, false);
		return packet;		
		
	}
	
	public static ArrayList<echoPacket> ARQ(long runTime){
		ArrayList<echoPacket> results = new ArrayList<echoPacket>();
		long packet_arrived = System.currentTimeMillis();
		long start_of_process = System.currentTimeMillis();
		
		do {
			double ack = 1;
			double nack = 0;
			long start = System.currentTimeMillis();
			echoPacket packet = ACK_NACK(ACK_result_code);
			while(packet != null && packet.isCorrect() == false) {
				nack++;
				packet = ACK_NACK(NACK_result_code);
				
			}
			if (packet == null) {
				System.out.println("connection closed");
				break;
			}
			packet_arrived = System.currentTimeMillis();
			
			double p = (ack / (ack + nack));
			double ber = 1.0 - Math.pow(p, 1.0/464.0);

			packet.setBer(ber);
			packet.setNack(nack);
			packet.setTimeResponse(packet_arrived - start);
			results.add(packet);
		}while((packet_arrived - start_of_process) <= runTime);
		return results;
	}
	
 	public static void exportExcel(ArrayList<echoPacket> packet, String request) throws IOException {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet();
		XSSFRow row = sheet.createRow(0);
		XSSFCell cell = row.createCell(0);
		cell.setCellValue("Message");
		cell = row.createCell(1);
		cell.setCellValue("Time Response");
		cell = row.createCell(2);
		cell.setCellValue("BER");
		cell = row.createCell(3);
		cell.setCellValue("Nack");
		
		for(int i=0; i<packet.size(); i++) {
			row = sheet.createRow(i+1);
			cell = row.createCell(0);
			cell.setCellValue(packet.get(i).getMessage());
			cell = row.createCell(1);
			cell.setCellValue(packet.get(i).getTimeResponse());
			cell = row.createCell(2);
			cell.setCellValue(packet.get(i).getBer());
			cell = row.createCell(3);
			cell.setCellValue(packet.get(i).getNack());
		}
		File path = new File(outputDir, request.replace("\r", ".") + "xlsx");
		workbook.write(new FileOutputStream(path));
		workbook.close();
	}
	
	public static void main(String[] args) throws Exception {

		Config config = new Config(args.length > 0 ? args[0] : "ithaki.properties");
		echo_request_code = config.get("echo_request_code") + "\r";
		image_request_code = config.get("image_request_code") + "\r";
		image_request_code_with_errors = config.get("image_request_code_with_errors") + "\r";
		GPS_request_code = config.get("gps_request_code");
		ACK_result_code = config.get("ack_request_code") + "\r";
		NACK_result_code = config.get("nack_request_code") + "\r";
		outputDir = new File(config.optional("output_dir", "."));
		outputDir.mkdirs();

		modem.setSpeed(80000);
		modem.setTimeout(8000);
		
		modem.open("ithaki");
		
		if (!ImageRequest(image_request_code)) {
			System.out.println("No image received: check the modem connection and the request codes.");
			modem.close();
			return;
		}
		
		ImageRequest(image_request_code_with_errors);
		
		GPSrequest();	
				
		modem.setSpeed(1000);
		
		exportExcel(ARQ(300000), ACK_result_code);
		
		exportExcel(echoPacketRequest(300000), echo_request_code);
		
		modem.close();

	}

}
