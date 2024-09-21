package org.redpanda.bot;

public class RandomStrategy implements IStrategy {
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
      if (canBuy && Math.random() < 0.001) {
        return Action.BUY;
      }
    } else {
      if (candle.getClose() / result.getDealPrice() <= 0.998) {
        return Action.SELL_STOP_LOSS;
      }

      if (candle.getHigh() / result.getDealPrice() >= 1.01) {
        return Action.SELL_TAKE_PROFIT;
      }
    }
    return Action.NOTHING;
  }
}
