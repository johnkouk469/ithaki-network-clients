package ithakimodem;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayDeque;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * Stand-in for the course's virtual modem class ithakimodem.Modem, for running networks-1 without the lab.
 * Written in October 2026 to check the published code. It answers echo, image (M and G codes), GPS and ARQ
 * (Q and R codes) requests with made-up data and corrupts the checksum of about one ARQ packet in five.
 * System properties: -Dmock.dead=true makes open() fail, -Dmock.delayms=60 sets the answer delay in ms.
 */
public class Modem {
	private final ArrayDeque<Integer> queue = new ArrayDeque<Integer>();
	private final Random rnd = new Random(42);
	private final boolean dead = Boolean.getBoolean("mock.dead");
	private final int delay = Integer.getInteger("mock.delayms", 60);
	private String lastPacket = "";

	public Modem() {}
	public void setSpeed(int s) {}
	public void setTimeout(int t) {}
	public boolean open(String name) { System.out.println("[mock modem] open " + name + " dead=" + dead); return !dead; }
	public void close() {}

	public int read() {
		if (queue.isEmpty()) return -1;
		return queue.poll();
	}

	public void write(byte[] b) {
		if (dead) return;
		String req = new String(b);
		try { Thread.sleep(delay); } catch (InterruptedException e) {}
		queue.clear();
		char c = req.charAt(0);
		if (c == 'E') push("PSTART 20-05-2020 18:03:25 01 <" + seq() + "> PSTOP\r\n");
		else if (c == 'M' || c == 'G') pushBytes(jpeg(c == 'G'));
		else if (c == 'P' && req.contains("R=")) gps();
		else if (c == 'P') pushBytes(jpeg(false));
		else if (c == 'Q') { lastPacket = arq(rnd.nextInt(5) == 0); push(lastPacket); }
		else if (c == 'R') { lastPacket = arq(rnd.nextInt(5) == 0); push(lastPacket); }
		else push("UNKNOWN REQUEST\r\n");
	}

	private String seq() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 16; i++) sb.append((char) ('a' + rnd.nextInt(26)));
		return sb.toString();
	}

	private String arq(boolean corrupt) {
		String s = seq();
		int x = s.charAt(0) ^ s.charAt(1);
		for (int i = 2; i < s.length(); i++) x ^= s.charAt(i);
		if (corrupt) s = "Z" + s.substring(1);
		return "PSTART 20-05-2020 18:03:25 01 <" + s + "> " + String.format("%03d", x) + " PSTOP\r\n";
	}

	private void gps() {
		StringBuilder sb = new StringBuilder("START ITHAKI GPS TRACKING\r\n");
		for (int i = 0; i < 90; i++) {
			sb.append(String.format("$GPGGA,%06d.000,4037.%04d,N,02257.%04d,E,1,07,1.5,57.8,M,36.1,M,,0000*6D\r\n", 45208 + i, 6331 + i * 7, 5633 + i * 5));
		}
		sb.append("STOP ITHAKI GPS TRACKING\r\n");
		push(sb.toString());
	}

	private byte[] jpeg(boolean noisy) {
		try {
			BufferedImage im = new BufferedImage(176, 144, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = im.createGraphics();
			g.setColor(Color.DARK_GRAY); g.fillRect(0, 0, 176, 144);
			g.setColor(Color.ORANGE); g.fillOval(40, 30, 90, 80);
			g.dispose();
			ByteArrayOutputStream o = new ByteArrayOutputStream();
			ImageIO.write(im, "jpg", o);
			byte[] d = o.toByteArray();
			byte[] out = new byte[d.length + 5];
			out[0] = 'X'; out[1] = 'Y'; out[2] = 0; out[3] = 1; out[4] = 2;
			System.arraycopy(d, 0, out, 5, d.length);
			return out;
		} catch (Exception e) { throw new RuntimeException(e); }
	}

	private void push(String s) { for (byte x : s.getBytes()) queue.add(x & 0xFF); }
	private void pushBytes(byte[] d) { for (byte x : d) queue.add(x & 0xFF); }
}
