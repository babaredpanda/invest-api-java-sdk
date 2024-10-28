package org.redpanda.bot;

import java.util.Collections;
import java.util.List;

public class TrendStrategy implements IStrategy {
  public static final int LAST_PRICES_MAX_SIZE = 8000;

  private boolean canBuy;
  private double bouncePrice;

  private final double[] lastPrices;
  private int lastPricesSize;
  private int lastPricesPointer;
  private double lastPricesSum;

  public TrendStrategy() {
    canBuy = false;
    bouncePrice = Double.POSITIVE_INFINITY;

    lastPrices = new double[LAST_PRICES_MAX_SIZE];
    lastPricesSize = 0;
    lastPricesPointer = 0;
    lastPricesSum = 0;
  }

  @Override
  public List<Action> startDay(StrategyResult result, HistoryDay day) {
    canBuy = true;
    return Collections.emptyList();
  }

  @Override
  public List<Action> endDay(StrategyResult result, HistoryDay day) {
    canBuy = false;
    return result.getLongLots() > 0
      ? Collections.singletonList(Action.LONG_CLOSE_END_DAY)
      : Collections.emptyList();
  }

  @Override
  public List<Action> processCandle(HistoryCandle candle, StrategyResult result) {
    double prevSma = lastPricesSum / LAST_PRICES_MAX_SIZE;
    lastPricesPointer = (lastPricesPointer + 1) % LAST_PRICES_MAX_SIZE;
    lastPricesSum += candle.getClose() - lastPrices[lastPricesPointer];
    lastPrices[lastPricesPointer] = candle.getClose();
    double sma = lastPricesSum / LAST_PRICES_MAX_SIZE;
    if (lastPricesSize < LAST_PRICES_MAX_SIZE) {
      lastPricesSize++;
    } else {
      if (prevSma / sma <= 0.999999) {
        if (canBuy && result.getLongLots() == 0) {
          bouncePrice = Math.min(bouncePrice, candle.getLow());
          if (candle.getLow() / bouncePrice >= 1.01) {
            return Collections.singletonList(Action.LONG_OPEN);
          }
        }
      }

      if (result.getLongLots() != 0) {
        if (candle.getClose() / result.getLongDealPrice() <= 0.998) {
          return Collections.singletonList(Action.LONG_STOP_LOSS);
        }

        bouncePrice = Math.max(bouncePrice, candle.getHigh());
        if (candle.getHigh() / bouncePrice <= 0.98) {
          return Collections.singletonList(Action.LONG_TAKE_PROFIT);
        }
      }
    }
    return Collections.emptyList();
  }
}
