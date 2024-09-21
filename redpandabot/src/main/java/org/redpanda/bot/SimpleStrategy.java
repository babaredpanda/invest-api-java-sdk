package org.redpanda.bot;

public class SimpleStrategy implements IStrategy {
  private double bouncePrice = Double.POSITIVE_INFINITY;
  private boolean canBuy = false;

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
    if (result.getLots() == 0) {
      bouncePrice = Math.min(bouncePrice, candle.getLow());
      if (canBuy && candle.getLow() / bouncePrice >= 1.05) {
        return Action.BUY;
      }
    } else {
      bouncePrice = Math.max(bouncePrice, candle.getHigh());
      if (candle.getClose() / result.getDealPrice() <= 0.998) {
        return Action.SELL_STOP_LOSS;
      }
      if (candle.getHigh() / result.getDealPrice() >= 1.02) {
        return Action.SELL_TAKE_PROFIT;
      }
//      if (candle.getHigh() / bouncePrice <= 0.98) {
//        return Action.SELL_TAKE_PROFIT;
//      }
    }
    return Action.NOTHING;
  }
}
