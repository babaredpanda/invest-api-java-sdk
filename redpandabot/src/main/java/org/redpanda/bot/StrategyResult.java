package org.redpanda.bot;

public class StrategyResult {
  public static final double COMMISSION = 0.0004;

  final private double startBalance;
  private double prevDayBalance;
  private double money;
  private int lots;

  private double commissionSum;

  private int dayCount;
  private double maxDayInc = 1;
  private double maxDayDec = 1;
  private double dealPrice;
  private int dealCount;
  private double maxDealInc = 1;
  private double maxDealDec = 1;

  private int dayEndSellCount;
  private int stopLossSellCount;
  private int takeProfitSellCount;

  public StrategyResult(double startBalance) {
    this.startBalance = startBalance;
    prevDayBalance = startBalance;
    money = startBalance;
    lots = 0;
  }

  public int getLots() {
    return lots;
  }

  public double getTotalChange() {
    return (prevDayBalance / startBalance - 1) * 100;
  }

  public double getDealPrice() {
    return dealPrice;
  }

  public void buy(HistoryCandle candle) {
    dealPrice = candle.getOpen();
    lots = (int) (money / dealPrice);
    money -= (lots * dealPrice) * (1 + COMMISSION);
    commissionSum += (lots * dealPrice) * COMMISSION;
  }

  public void sellEndDay(HistoryCandle candle) {
    dayEndSellCount++;
    sell(candle);
  }

  public void sellStopLoss(HistoryCandle candle) {
    stopLossSellCount++;
    sell(candle);
  }

  public void sellTakeProfit(HistoryCandle candle) {
    takeProfitSellCount++;
    sell(candle);
  }

  private void sell(HistoryCandle candle) {
    double price = candle.getOpen();
    money += (lots * price) * (1 - COMMISSION);
    commissionSum += (lots * price) * COMMISSION;
    lots = 0;
    dealCount++;
    double change = price / dealPrice;
    maxDealInc = Math.max(maxDealInc, change);
    maxDealDec = Math.min(maxDealDec, change);
  }

  public void endDay(HistoryCandle historyCandle) {
//    if (lots != 0) {
//      throw new RuntimeException("lots != 0");
//    }
    double balance = money + historyCandle.getClose() * lots;
    double dayChange = balance / prevDayBalance;
    prevDayBalance = balance;
    maxDayInc = Math.max(maxDayInc, dayChange);
    maxDayDec = Math.min(maxDayDec, dayChange);
    dayCount++;
  }

  @Override
  public String toString() {
    return "StrategyResult{" + "\n" +
      "StartBalance=" + startBalance + "\n" +
      ", EndBalance=" + String.format("%,.2f", prevDayBalance) + "\n" +
      ", TotalIncrement=" + formatShare(prevDayBalance / startBalance) + "\n" +
      ", DayCount=" + dayCount + "\n" +
      ", MaxDayIncrement=" + formatShare(maxDayInc) + "\n" +
      ", MaxDayDecrement=" + formatShare(maxDayDec) + "\n" +
      ", MidDayChange=" + String.format("%,.2f", (prevDayBalance / startBalance - 1) * 100 / dayCount) + "%\n" +
      ", DealCount=" + dealCount + "\n" +
      ", MaxDealIncrement=" + formatShare(maxDealInc) + "\n" +
      ", MaxDealDecrement=" + formatShare(maxDealDec) + "\n" +
      ", commission=" + formatShare(1 + commissionSum / startBalance) + "\n" +
      ", dayEndSellCount=" + dayEndSellCount + "\n" +
      ", stopLossSellCount=" + stopLossSellCount + "\n" +
      ", takeProfitSellCount=" + takeProfitSellCount + "\n" +
      '}';
  }

  private String formatShare(double v) {
    double p = (v - 1.) * 100;
    return String.format("%,.2f%%", p);
  }
}
