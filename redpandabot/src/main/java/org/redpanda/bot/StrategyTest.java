package org.redpanda.bot;

import java.io.*;
import java.util.*;

import static org.redpanda.bot.ArchiveLoader.*;

public class StrategyTest {
  private static final double START_BALANCE = 10000.0;

  public static void main(String[] args) throws IOException {
    long start = System.currentTimeMillis();
    double totalChangeSum = 0;
    for (String ticker : TICKERS) {
      System.out.println("\t" + ticker);
      StrategyResult result = new StrategyResult(START_BALANCE);
//      IStrategy strategy = new RandomStrategy();
//      IStrategy strategy = new TrendStrategy();
//      IStrategy strategy = new SimpleStrategy();
      IStrategy strategy = new RsiCrossBothStrategy();

      List<Action> actions;
      List<HistoryDay> days = DataFormat.loadTickerData(ticker);
      System.out.println("\tData loaded");
      for (HistoryDay day : days) {
        actions = strategy.startDay(result, day);

        List<HistoryCandle> candles = day.getCandles();
        int end = candles.size() - 30;
        for (int i = 0; i < candles.size(); i++) {
          HistoryCandle candle = candles.get(i);
          for (Action action : actions) {
            switch (action) {
              case LONG_OPEN:
                result.longOpen(candle);
                break;
              case LONG_CLOSE_END_DAY:
                result.longCloseEndDay(candle);
                break;
              case LONG_STOP_LOSS:
                result.longStopLoss(candle);
                break;
              case LONG_TAKE_PROFIT:
                result.longTakeProfit(candle);
                break;
              case SHORT_OPEN:
                result.shortOpen(candle);
                break;
              case SHORT_CLOSE_END_DAY:
                result.shortCloseEndDay(candle);
                break;
              case SHORT_STOP_LOSS:
                result.shortStopLoss(candle);
                break;
              case SHORT_TAKE_PROFIT:
                result.shortTakeProfit(candle);
                break;
            }
          }
          actions = strategy.processCandle(candle, result);
          if (i == end) {
            actions = strategy.endDay(result, day);
          }
        }
        result.endDay(candles.get(candles.size() - 1));
      }
      System.out.println(result);
      totalChangeSum += result.getTotalChange();
    }
    System.out.format("%,.2f\t %d\n", totalChangeSum / TICKERS.length, (System.currentTimeMillis() - start) / 1000);
  }
}
