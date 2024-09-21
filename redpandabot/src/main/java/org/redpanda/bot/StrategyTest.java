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
      IStrategy strategy = new SimpleStrategy();

      Action action;
      List<HistoryDay> days = DataFormat.loadTickerData(ticker);
      System.out.println("\tData loaded");
      for (HistoryDay day: days) {
        action = strategy.startDay(result, day);

        List<HistoryCandle> candles = day.getCandles();
        int end = candles.size() - 30;
        for (int i = 0; i < candles.size(); i++) {
          HistoryCandle candle = candles.get(i);
          switch (action) {
            case BUY:
              result.buy(candle);
              break;
            case SELL_END_DAY:
              result.sellEndDay(candle);
              break;
            case SELL_STOP_LOSS:
              result.sellStopLoss(candle);
              break;
            case SELL_TAKE_PROFIT:
              result.sellTakeProfit(candle);
              break;
          }
          action = strategy.processCandle(candle, result);
          if (i == end) {
            action = strategy.endDay(result);
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
