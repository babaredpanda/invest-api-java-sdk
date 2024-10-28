package org.redpanda.bot;

public interface ICandle {
  public long getTimestamp();

  public double getOpen();

  public double getClose();

  public double getHigh();

  public double getLow();

  public int getVolume();

  public default double getAverage() {
    return (getHigh() + getLow()) / 2.;
  }
}
