package org.redpanda.bot.indicator;

import org.redpanda.bot.ICandle;
import org.redpanda.bot.IIndicator;

public class RSI implements IIndicator {
  private final RMAIndicator avgGain;
  private final RMAIndicator avgLoss;
  private Double value;

  public RSI(int length) {
    avgGain = new ChangeIndicator(length, true);
    avgLoss = new ChangeIndicator(length, false);
    value = null;
  }
  @Override
  public Double getValue() {
    return value;
  }

  @Override
  public void update(ICandle candle) {
    avgGain.update(candle);
    avgLoss.update(candle);
    if (avgGain.getValue() != null && avgLoss.getValue() != null) {
      if (avgLoss.getValue() != 0) {
        value = 100 - 100 / (1 + avgGain.getValue() / avgLoss.getValue());
      } else {
        value = 100d;
      }
    }
  }

  private static class ChangeIndicator extends RMAIndicator {

    private final boolean isPositive;
    private Double prevClose;

    public ChangeIndicator(int length, boolean isPositive) {
      super(length);
      this.isPositive = isPositive;
    }

    @Override
    protected Double getCurrentValue(ICandle candle) {
      if (prevClose == null) {
        prevClose = candle.getClose();
        return null;
      }
      double v = candle.getClose() - prevClose;
      prevClose = candle.getClose();
      return v >= 0 ^ isPositive ? 0 : Math.abs(v);
    }
  }
}
