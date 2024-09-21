package org.redpanda.bot;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataFormat {
  public static void saveTickerData(String ticker, List<HistoryDay> days) throws IOException {
    try (DataOutputStream out = new DataOutputStream(new FileOutputStream(getFile(ticker)))) {
      out.writeInt(days.size());
      for (HistoryDay day : days) {
        HistoryDay.write(day, out);
      }
    }
  }

  public static List<HistoryDay> loadTickerData(String ticker) throws IOException {
    List<HistoryDay> result = new ArrayList<>();
    try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(getFile(ticker))))) {
      int count = in.readInt();
      for (int i = 0; i < count; i++) {
        result.add(HistoryDay.read(in));
      }
    }
    return result;
  }

  private static File getFile(String ticker) {
    return new File(ArchiveLoader.FOLDER, ticker + ".dat");
  }
}
