package org.redpanda.bot;

import java.util.List;

public interface IStrategy {
  List<Action> startDay(StrategyResult result, HistoryDay day);

  List<Action> endDay(StrategyResult result, HistoryDay day);

  List<Action> processCandle(HistoryCandle candle, StrategyResult result);
}
