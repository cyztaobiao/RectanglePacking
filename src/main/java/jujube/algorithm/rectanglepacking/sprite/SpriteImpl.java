package jujube.algorithm.rectanglepacking.sprite;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by tb on 2017/6/20.
 */

public class SpriteImpl {

	public static void main(String[] args) throws IOException {
		boolean classify = args[0].equalsIgnoreCase("-c");
		if (classify) {
			File folder = new File(args[1]);
			File target = new File(args[2]);
			target.mkdirs();
			packWithClassify(folder, target);
		}else {
			File folder = new File(args[0]);
			File target = new File(args[1]);
			target.mkdirs();
			pack(folder, target);
		}
	}

	public static void pack(File folder, File target) throws IOException {
		List<PngRect> pngRectList = new ArrayList<>();
		for (File file : folder.listFiles()) {
			BufferedImage image = ImageIO.read(file);
			pngRectList.add(new PngRect(image.getWidth(), image.getHeight(), file.getName()));
		}

		PackRect packRect = new PackRect(pngRectList);
		List<PngRect> result = packRect.getResult();
		int width = packRect.getWidth();
		int height = packRect.getHeight();
		BufferedImage sprite = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = sprite.getGraphics();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("[");
		for (PngRect p : result) {
			stringBuilder.append("{\"x\":");
			stringBuilder.append(p.getStartP().getX());
			stringBuilder.append(",\"y\":");
			stringBuilder.append(p.getStartP().getY());
			stringBuilder.append(",\"file\":");
			stringBuilder.append("\"");
			stringBuilder.append(p.fileName.substring(0, p.fileName.length() - 4));
			stringBuilder.append("\"}");
			stringBuilder.append(",");

			BufferedImage image = ImageIO.read(new File(folder, p.fileName));
			graphics.drawImage(image, p.getStartP().getX(), p.getStartP().getY(), null);
		}
		graphics.dispose();

		stringBuilder.deleteCharAt(stringBuilder.length() - 1);
		stringBuilder.append("]");
		writeFileFromString(new File(target, "sprite.json"), stringBuilder.toString());
		ImageIO.write(sprite, "png", new File(target, "sprite.png"));
	}


	/**
	 * 将字符串写入文件
	 *
	 * @param file    文件
	 * @param content 写入内容
	 * @return {@code true}: 写入成功<br>{@code false}: 写入失败
	 */
	public static boolean writeFileFromString(final File file, final String content) {
		return writeFileFromString(file, content, false);
	}

	/**
	 * 将字符串写入文件
	 *
	 * @param file    文件
	 * @param content 写入内容
	 * @param append  是否追加在文件末
	 * @return {@code true}: 写入成功<br>{@code false}: 写入失败
	 */
	public static boolean writeFileFromString(final File file, final String content, final boolean append) {
		if (file == null || content == null) return false;
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(file, append));
			bw.write(content);
			bw.flush();
			bw.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}


	public static List<String> packWithClassify(File folder, File target) throws IOException {
		List<Spec> specs = classify(folder);
		return collectPics(specs, folder, target);
	}

	private static List<Spec> classify(File folder) throws IOException {
		String[] files = folder.list();
		List<Spec> specs = new ArrayList<>();
		for (int i = 0; i < files.length; i ++) {
			for (int j = i + 1; j < files.length; j ++) {
				BufferedImage i1 = ImageIO.read(new File(folder, files[i]));
				BufferedImage i2 = ImageIO.read(new File(folder, files[j]));
				if (i1.getWidth() == i2.getWidth() && i1.getHeight() == i2.getHeight()) {
					j = ++i;
				}
			}
			BufferedImage image = ImageIO.read(new File(folder, files[i]));
			specs.add(new Spec(image.getWidth(), image.getHeight()));
		}

		return specs;

	}

	private static List<String> collectPics(List<Spec> specs, File folder, File target) throws IOException {
		List<String> paths = new ArrayList<>();
		for (Spec spec : specs) {
			paths.add(draw(spec, folder, target));
		}
		return paths;
	}


	private static String draw(Spec spec, File folder, File target) throws IOException {
		String format = "\"%1$s\": {\n\"height\": %2$d,\n\"pixelRatio\": 1,\n\"width\": %3$d,\n\"x\": %4$d,\n\"y\": %5$d\n}";

		Set<String> figs = new HashSet<>();
		for (String fileName : folder.list()) {
			BufferedImage image = ImageIO.read(new File(folder, fileName));
			if (image.getWidth() == spec.w && image.getHeight() == spec.h) {
				figs.add(fileName);
			}
		}

		int m = fullSquare(figs.size());
		BufferedImage output = new BufferedImage(m * spec.w, m * spec.h, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = output.getGraphics();
		int ow = 0, oh = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		for (String fileName : figs) {
			BufferedImage image = ImageIO.read(new File(folder, fileName));
			if (image.getWidth() == spec.w && image.getHeight() == spec.h) {
				String substring = fileName.substring(0, fileName.length() - 4);
				if (ow < m * spec.w) {
					graphics.drawImage(image, ow, oh, null);
					sb.append(String.format(Locale.getDefault(),format, substring, image.getHeight(), image.getWidth(), ow, oh));
					sb.append(",\n");
					ow = ow + spec.w;
				}else {
					graphics.drawImage(image, 0, oh + spec.h, null);
					sb.append(String.format(Locale.getDefault(),format, substring, image.getHeight(), image.getWidth(), 0, oh + spec.h));
					sb.append(",\n");
					ow = 0;
					oh = oh + spec.h;
				}
			}
		}
		graphics.dispose();
		if (sb.charAt(sb.length() - 2) == ',') {
			sb.deleteCharAt(sb.length() - 2);
		}
		sb.append("}");

		String pic = spec.w + "x" + spec.h  + "x" + figs.size() + ".png";
		String json = spec.w + "x" + spec.h  + "x" + figs.size() + ".json";
		writeFileFromString(new File(target, json), sb.toString());
		ImageIO.write(output, "png", new File("output", pic));
		return pic + "\n" + json + "\n";
	}

	private static int fullSquare(int sum) {
		double m = Math.sqrt(sum);
		int k = (int) m;
		if (m - k == 0) {
			return k;
		}else {
			return k + 1;
		}
	}

	private static class Spec {
		int w;
		int h;

		Spec(int w, int h) {
			this.w = w;
			this.h = h;
		}
	}
}
