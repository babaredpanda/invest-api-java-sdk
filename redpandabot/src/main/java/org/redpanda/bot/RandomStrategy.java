package org.redpanda.bot;

import java.util.Collections;
import java.util.List;

public class RandomStrategy implements IStrategy {
  private boolean canBuy = false;

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
    if (result.getLongLots() == 0) {
      if (canBuy && Math.random() < 0.001) {
        return Collections.singletonList(Action.LONG_OPEN);
      }
    } else {
      if (candle.getClose() / result.getLongDealPrice() <= 0.998) {
        return Collections.singletonList(Action.LONG_STOP_LOSS);
      }

      if (candle.getHigh() / result.getLongDealPrice() >= 1.01) {
        return Collections.singletonList(Action.LONG_TAKE_PROFIT);
      }
    }
    return Collections.emptyList();
  }
}
