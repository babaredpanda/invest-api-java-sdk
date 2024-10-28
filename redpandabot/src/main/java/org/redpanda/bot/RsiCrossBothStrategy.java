package org.redpanda.bot;

import org.redpanda.bot.indicator.ATR;
import org.redpanda.bot.indicator.RSI;

import java.util.ArrayList;
import java.util.List;

public class RsiCrossBothStrategy implements IStrategy {
  public static final double TAKE_PROFIT_FACTOR = 1.5;
  public static final double STOP_LOSS_FACTOR = TAKE_PROFIT_FACTOR / 3.;
  private double longTakeProfit = -1;
  private double longStopLoss = -1;
  private double shortTakeProfit = -1;
  private double shortStopLoss = -1;

  private Boolean rsiCompare;
  private final ATR atr;
  private final RSI rsiLong;
  private final RSI rsiShort;

  public RsiCrossBothStrategy() {
    atr = new ATR(5);
    rsiLong = new RSI(17);
    rsiShort = new RSI(5);
  }

  @Override
  public List<Action> startDay(StrategyResult result, HistoryDay day) {
    List<Action> res = new ArrayList<>(2);
    Double rsiLongValue = rsiLong.getValue();
    Double rsiShortValue = rsiShort.getValue();
    Double atrValue = atr.getValue();
    Boolean newRsiCompare = null;
    if (rsiLongValue != null && rsiShortValue != null) {
      newRsiCompare = rsiLongValue < rsiShortValue;
    }
    if (rsiCompare != null && !rsiCompare.equals(newRsiCompare) && atrValue != null) {
      double price = day.getCandles().get(0).getAverage();
      longTakeProfit = price + atrValue * TAKE_PROFIT_FACTOR;
      shortTakeProfit = price - atrValue * TAKE_PROFIT_FACTOR;
      longStopLoss = price - atrValue * STOP_LOSS_FACTOR;
      shortStopLoss = price + atrValue * STOP_LOSS_FACTOR;
      res.add(Action.LONG_OPEN);
      res.add(Action.SHORT_OPEN);
    }
    rsiCompare = newRsiCompare;
    return res;
  }

  @Override
  public List<Action> endDay(StrategyResult result, HistoryDay day) {
    atr.update(day);
    rsiLong.update(day);
    rsiShort.update(day);
    List<Action> res = new ArrayList<>(2);
    if (result.getLongLots() > 0) {
      res.add(Action.LONG_CLOSE_END_DAY);
    }
    if (result.getShortLots() > 0) {
      res.add(Action.SHORT_CLOSE_END_DAY);
    }
    return res;
  }

  @Override
  public List<Action> processCandle(HistoryCandle candle, StrategyResult result) {
    List<Action> res = new ArrayList<>(2);
    if (result.getLongLots() > 0) {
      if (candle.getHigh() >= longTakeProfit) {
        res.add(Action.LONG_TAKE_PROFIT);
      }
      if (candle.getLow() <= longStopLoss) {
        res.add(Action.LONG_STOP_LOSS);
      }
    }
    if (result.getShortLots() > 0) {
      if (candle.getHigh() >= shortStopLoss) {
        res.add(Action.SHORT_STOP_LOSS);
      }
      if (candle.getLow() <= shortTakeProfit) {
        res.add(Action.SHORT_TAKE_PROFIT);
      }
    }
    return res;
  }
}
