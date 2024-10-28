package org.redpanda.bot.indicator;

import org.redpanda.bot.ICandle;

public class ATR extends RMAIndicator {

  public ATR(int length) {
    super(length);
  }

  @Override
  protected Double getCurrentValue(ICandle candle) {
    return candle.getHigh() - candle.getLow();
  }
}
