package org.redpanda.bot;

public interface IStrategy {
  Action startDay(StrategyResult result, HistoryDay day);

  Action endDay(StrategyResult result);

  Action processCandle(HistoryCandle candle, StrategyResult result);
}
