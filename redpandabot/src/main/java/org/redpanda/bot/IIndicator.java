package org.redpanda.bot;

public interface IIndicator {
  public Double getValue();
  public void update(ICandle candle);
}
