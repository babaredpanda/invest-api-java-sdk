package org.redpanda.bot;

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
  public Action startDay(StrategyResult result, HistoryDay day) {
    canBuy = true;
    return Action.NOTHING;
  }

  @Override
  public Action endDay(StrategyResult result) {
    canBuy = false;
    return result.getLots() > 0 ? Action.SELL_END_DAY : Action.NOTHING;
  }

  @Override
  public Action processCandle(HistoryCandle candle, StrategyResult result) {
    double prevSma = lastPricesSum / LAST_PRICES_MAX_SIZE;
    lastPricesPointer = (lastPricesPointer + 1) % LAST_PRICES_MAX_SIZE;
    lastPricesSum += candle.getClose() - lastPrices[lastPricesPointer];
    lastPrices[lastPricesPointer] = candle.getClose();
    double sma = lastPricesSum / LAST_PRICES_MAX_SIZE;
    if (lastPricesSize < LAST_PRICES_MAX_SIZE) {
      lastPricesSize++;
    } else {
      if (prevSma / sma <= 0.999999) {
        if (canBuy && result.getLots() == 0) {
          bouncePrice = Math.min(bouncePrice, candle.getLow());
          if (candle.getLow() / bouncePrice >= 1.01) {
            return Action.BUY;
          }
        }
      }

      if (result.getLots() != 0) {
        if (candle.getClose() / result.getDealPrice() <= 0.998) {
          return Action.SELL_STOP_LOSS;
        }

        bouncePrice = Math.max(bouncePrice, candle.getHigh());
        if (candle.getHigh() / bouncePrice <= 0.98) {
          return Action.SELL_TAKE_PROFIT;
        }
      }
    }
    return Action.NOTHING;
  }

//        if (result.getLots() == 0) {
//          bouncePrice = Math.min(bouncePrice, candle.getLow());
//          if (canBuy && candle.getLow() / bouncePrice >= 1.02) {
//            return Action.BUY;
//          }
//        }
//      } else {
//        bouncePrice = Math.ulp(1.0);
//        if (result.getLots() != 0) {
//          return Action.SELL_TAKE_PROFIT;
//        }
//      }

}
