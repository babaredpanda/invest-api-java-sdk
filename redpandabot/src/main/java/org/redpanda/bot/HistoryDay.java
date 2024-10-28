package org.redpanda.bot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryDay implements Comparable<HistoryDay>, ICandle {
  private final long timestamp;
  private final List<HistoryCandle> candles;

  private final double open;
  private final double close;
  private final double high;
  private final double low;
  private final int volume;


  public HistoryDay(long timestamp, List<HistoryCandle> candles) {
    this.timestamp = timestamp;
    this.candles = candles;

    open = candles.get(0).getOpen();
    close = candles.get(candles.size() - 1).getClose();
    double h = open;
    double l = open;
    int v = 0;
    for (HistoryCandle candle : candles) {
      h = Math.max(h, candle.getHigh());
      l = Math.min(l, candle.getLow());
      v += candle.getVolume();
    }
    high = h;
    low = l;
    volume = v;
  }

  public static void write(HistoryDay day, DataOutputStream out) throws IOException {
    out.writeLong(day.timestamp);
    out.writeInt(day.candles.size());
    for (HistoryCandle candle : day.candles) {
      HistoryCandle.write(candle, out);
    }
  }

  public static HistoryDay read(DataInputStream in) throws IOException {
    long timestamp = in.readLong();
    List<HistoryCandle> candles = new ArrayList<>();
    int count = in.readInt();
    for (int i = 0; i < count; i++) {
       candles.add(HistoryCandle.read(in));
    }
    return new HistoryDay(timestamp, candles);
  }

  @Override
  public int compareTo(HistoryDay o) {
    return Long.compare(timestamp, o.timestamp);
  }

  public boolean isHoliday() {
    return candles.size() < 500;
  }

  public List<HistoryCandle> getCandles() {
    return Collections.unmodifiableList(candles);
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public double getOpen() {
    return open;
  }

  @Override
  public double getClose() {
    return close;
  }

  @Override
  public double getHigh() {
    return high;
  }

  @Override
  public double getLow() {
    return low;
  }

  @Override
  public int getVolume() {
    return volume;
  }

  public static class Builder {
    private Long timestamp;
    private final List<HistoryCandle> candles;

    private Builder() {
      timestamp = null;
      candles = new ArrayList<>();
    }

    public Builder addCandle(String s) {
      HistoryCandle candle = HistoryCandle.createFromString(s);
      if (timestamp == null) {
        timestamp = candle.getTimestamp();
      }
      candles.add(candle);
      return this;
    }

    public HistoryDay build() {
      return new HistoryDay(timestamp, candles);
    }
  }
}
