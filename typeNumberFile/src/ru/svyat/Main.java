package ru.svyat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Main {

  private static final String fileName = "V:\\file\\file.txt";
  private static final String compFileName = "V:\\file\\gzipFile.txt.gzip";
  private static BufferedWriter writer;

  public static void main(String[] args) throws IOException {
//    writeToFiles();

    System.out.println("Compressing time (sec): " + compressGZipFiles());

    System.out.println("Decompressing time (sec): " + decompressGzipFile());

    fileSizeComparison();
  }

  public static void fileSizeComparison() throws IOException {
    System.out.println("Delimited file size in MB: " + Files.size(Path.of(fileName)) / 1024 * 1024);
    System.out.println("Compressed delimited file size in MB: " + Files.size(Path.of(compFileName)) / 1024 * 1024);
  }

  public static long compressGZipFiles() {
    Instant start = Instant.now();
    BufferedInputStream fis;
    BufferedOutputStream gzipOS;
    BufferedOutputStream fos;
    byte[] buffer = new byte[1024];
    int len;
    try {
      fis = new BufferedInputStream(new FileInputStream(fileName));
      fos = new BufferedOutputStream(new FileOutputStream(compFileName));
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
      fis = new BufferedInputStream(new FileInputStream(compFileName));
      gis = new BufferedInputStream(new GZIPInputStream(fis));
      fos = new BufferedOutputStream(new FileOutputStream(fileName));
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
    for (int i = 0; i < 3000; i++) {
      List<Integer> integerList = ThreadLocalRandom.current().ints(6720 * 3000, -32768, 32768).boxed().collect(Collectors.toList());
      try {
        writer = new BufferedWriter(new FileWriter(fileName, true));
        writer.flush();
        AtomicInteger j = new AtomicInteger();
        integerList.forEach(integer -> {
          try {
            if (j.get() > 0) {
              writer.write(',');
            }
            writer.write(Integer.toString(integer));
            j.getAndIncrement();
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
        if (i > 0) {
          writer.write(',');
        }
      } catch (IOException ex) {
        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
      }
      Logger.getLogger(Main.class.getName()).log(Level.INFO, String.valueOf(i));
    }
  }

  public static byte[] bigIntToByteArray(final int i) {
    BigInteger bigInt = BigInteger.valueOf(i);
    return bigInt.toByteArray();
  }
}
