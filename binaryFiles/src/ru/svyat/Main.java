package ru.svyat;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class Main {

  private static final String typeFileName = "V:\\binary\\rawBytes.bin";
  private static final String compTypeFileName = "V:\\binary\\gzipTypeFile.bin.gzip";
  private static BufferedOutputStream byteWriter;

  public static void main(String[] args) throws IOException {
//    writeToFiles();
//    System.out.println("Compressing time (sec): " + compressGZipFiles());
//    System.out.println("Decompressing time (sec): " + decompressGzipFile());

//    fileSizeComparison();
//    BufferedImage img = map(672, 3000);
//    saveImage(img, "bmp", "image");

    compressImage("image.bmp", "image.png");
  }

  public static void fileSizeComparison() throws IOException {
    System.out.println("Type file size in MB: " + Files.size(Path.of(typeFileName)) / 1024 * 1024);
    System.out.println("Compressed type file size in MB: " + Files.size(Path.of(compTypeFileName)) / 1024 * 1024);
  }

  private static BufferedImage map(int sizeX, int sizeY) {
    List<Integer> integerList = ThreadLocalRandom.current().ints(672 * 3000, -32768, 32768).boxed().collect(Collectors.toList());
    final BufferedImage res = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_USHORT_565_RGB);
    for (int x = 0; x < sizeX; x++) {
      for (int y = 0; y < sizeY; y++) {
        res.setRGB(x, y, integerList.get(x + y));
      }
    }
    return res;
  }

  private static void saveImage(final BufferedImage bi, String format, final String path) {
    try {
      ImageIO.write(bi, format, new File(path + '.' + format));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void compressImage(String image, String path) throws IOException {
    File input = new File(image);
    BufferedImage bufferedImage = ImageIO.read(input);
    File compressedImageFile = new File(path);
    OutputStream os = new FileOutputStream(compressedImageFile);
    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
    ImageWriter writer = writers.next();
    ImageOutputStream ios = ImageIO.createImageOutputStream(os);
    writer.setOutput(ios);
    ImageWriteParam param = writer.getDefaultWriteParam();
    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    param.setCompressionQuality(0.90f);
    writer.write(null, new IIOImage(bufferedImage, null, null), param);
    os.close();
    ios.close();
    writer.dispose();
  }

  public static long compressGZipFiles() {
    Instant start = Instant.now();
    BufferedInputStream fis;
    BufferedOutputStream gzipOS;
    BufferedOutputStream fos;
    byte[] buffer = new byte[1024];
    int len;
    try {
      fis = new BufferedInputStream(new FileInputStream(typeFileName));
      fos = new BufferedOutputStream(new FileOutputStream(compTypeFileName));
      gzipOS = new BufferedOutputStream(new GZIPOutputStream(fos));
      while ((len = fis.read(buffer)) != -1) {
        gzipOS.write(buffer, 0, len);
      }
      gzipOS.close();
      fos.close();
      fis.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    Instant finish = Instant.now();
    return Duration.between(start, finish).toSeconds();
  }

  public static long decompressGzipFile() {
    Instant start = Instant.now();
    byte[] buffer = new byte[1024];
    int len;
    BufferedInputStream fis;
    BufferedInputStream gis;
    BufferedOutputStream fos;
    try {
      fis = new BufferedInputStream(new FileInputStream(compTypeFileName));
      gis = new BufferedInputStream(new GZIPInputStream(fis));
      fos = new BufferedOutputStream(new FileOutputStream(typeFileName));
      while ((len = gis.read(buffer)) != -1) {
        fos.write(buffer, 0, len);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    Instant finish = Instant.now();
    return Duration.between(start, finish).toSeconds();
  }

  public static void writeToFiles() {
    List<Integer> integerList = ThreadLocalRandom.current().ints(672 * 3000, -32768, 32768).boxed().collect(Collectors.toList());
    List<byte[]> bytes = integerList.stream().map(Main::bigIntToByteArray).collect(Collectors.toList());
    try {
      byteWriter = new BufferedOutputStream(new FileOutputStream(typeFileName, false));
      byteWriter.flush();
      AtomicInteger j = new AtomicInteger();
      bytes.forEach(bytes1 -> {
        try {
          byteWriter.write(bytes1);
          j.getAndIncrement();
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    } catch (IOException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public static byte[] bigIntToByteArray(final int i) {
    BigInteger bigInt = BigInteger.valueOf(i);
    return bigInt.toByteArray();
  }
}
