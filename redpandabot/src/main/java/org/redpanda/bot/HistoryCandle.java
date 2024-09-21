package org.redpanda.bot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Iterator;

public class HistoryCandle {
  private final long ts;
  private final double open;
  private final double close;
  private final double high;
  private final double low;
  private final int volume;

  public static HistoryCandle createFromString(String s) {
    String[] strings = s.split(";");
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    try {
      long ts = format.parse(strings[1]).getTime();
      double open = Double.parseDouble(strings[2]);
      double close = Double.parseDouble(strings[3]);
      double high = Double.parseDouble(strings[4]);
      double low = Double.parseDouble(strings[5]);
      int volume = Integer.parseInt(strings[6]);
      return new HistoryCandle(ts, open, close, high, low, volume);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public HistoryCandle(long ts, double open, double close, double high, double low, int volume) {
    this.ts = ts;
    this.open = open;
    this.close = close;
    this.high = high;
    this.low = low;
    this.volume = volume;
  }

  public static void write(HistoryCandle candle, DataOutputStream out) throws IOException {
    out.writeLong(candle.ts);
    out.writeDouble(candle.open);
    out.writeDouble(candle.close);
    out.writeDouble(candle.high);
    out.writeDouble(candle.low);
    out.writeInt(candle.volume);
  }

  public static HistoryCandle read(DataInputStream in) throws IOException {
    return new HistoryCandle(
      in.readLong(),
      in.readDouble(),
      in.readDouble(),
      in.readDouble(),
      in.readDouble(),
      in.readInt()
    );
  }

  public long getTimestamp() {
    return ts;
  }

  public double getOpen() {
    return open;
  }

  public double getClose() {
    return close;
  }

  public double getHigh() {
    return high;
  }

  public double getLow() {
    return low;
  }

  public int getVolume() {
    return volume;
  }

  @Override
  public String toString() {
    return "HistoryCandle{" +
      "ts=" + ts +
      ", open=" + open +
      ", close=" + close +
      ", high=" + high +
      ", low=" + low +
      ", volume=" + volume +
      '}';
  }
}
