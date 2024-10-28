package org.redpanda.bot.indicator;

import org.redpanda.bot.ICandle;
import org.redpanda.bot.IIndicator;

public abstract class RMAIndicator implements IIndicator {
  private final int length;
  private Double value;
  private double[] data;
  private int pointer;
  private double sum;
  private int counter;

  public RMAIndicator(int length) {
    this.length = length;
    value = null;
    data = new double[length];
    pointer = 0;
    sum = 0;
    counter = 0;
  }

  @Override
  public Double getValue() {
    return value;
  }

  @Override
  public void update(ICandle candle) {
    Double v = getCurrentValue(candle);
    if (v != null) {
      sum += v - data[pointer];
      data[pointer] = v;
      pointer = (pointer + 1) % length;
      counter++;
      if (counter >= length) {
        value = sum / length;
      }
    }
  }

  protected abstract Double getCurrentValue(ICandle candle);
}
