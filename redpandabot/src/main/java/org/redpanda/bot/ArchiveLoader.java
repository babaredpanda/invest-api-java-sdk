package org.redpanda.bot;

import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.InvestApi;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ArchiveLoader {

  public static final String TOKEN = "t.z9b8fpyOwZ7EIYHdZt9yGGkRloOUTxvkCzKm02OepcF_glJBCEROOeRog0sPlFRJfY-kv51pSIcfYGhD5rqXaA";
  public static final String[] TICKERS = {
    "GAZP", "SBER", "RUAL", "ROSN", "NVTK", "MTSS", "AFLT", "NLMK", "MTLR",
    "ALRS", "CHMF", "TATN", "AFKS", "SNGS", "MOEX", "RNFT", "SIBN", "BANEP", "VKCO"
  };
  public static final int[] YEARS = {2023, 2024};
  public static final File FOLDER = new File("C:/home/elisey/projects/tinkoff/history");

  public static void main(String[] args) throws IOException {
    var sandboxApi = InvestApi.createSandbox(TOKEN);
    for (String ticker : TICKERS) {
      Share share = sandboxApi.getInstrumentsService().getShareByTickerSync(ticker, "TQBR");
      for ( int year : YEARS) {
        downloadArchive(share, year);
      }
    }

    for (String ticker : TICKERS) {
      System.out.println("Process Ticker: " + ticker);
      List<HistoryDay> days = new ArrayList<>();
      for (int year : YEARS) {
        File file = new File(FOLDER, year + "-" + ticker + ".zip");
        ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
          System.out.println("\t" + zipEntry.getName());
          HistoryDay.Builder dayBuilder = HistoryDay.builder();
          byte[] bytes = zis.readAllBytes();
          BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
          String s = reader.readLine();
          while (s != null) {
            dayBuilder.addCandle(s);
            s = reader.readLine();
          }
          HistoryDay day = dayBuilder.build();
          if (!day.isHoliday()) {
            days.add(day);
          }
          zipEntry = zis.getNextEntry();
        }
      }
      Collections.sort(days);
      DataFormat.saveTickerData(ticker, days);
    }
  }

  private static void downloadArchive(Share share, int year) throws IOException {
    System.out.format("Download file: ticker = %s, year = %d\n", share.getTicker(), year);
    URLConnection conn = new URL(
      "https://invest-public-api.tinkoff.ru/history-data?figi=" + share.getFigi() +
        "&year=2024").openConnection();
    conn.setRequestProperty("Authorization", "Bearer " + TOKEN);
    conn.connect();
    try (BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
         FileOutputStream out = new FileOutputStream(new File(ArchiveLoader.FOLDER, year + "-" + share.getTicker() + ".zip"))) {
      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = in.read(buffer, 0, 1024)) != -1) {
        out.write(buffer, 0, bytesRead);
      }
    }
  }
}
