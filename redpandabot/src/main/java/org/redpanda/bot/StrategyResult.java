package org.redpanda.bot;

public class StrategyResult {
  public static final double COMMISSION = 0.0004;

  final private double startBalance;
  private double prevDayBalance;
  private double money;
  private int longLots;
  private int shortLots;

  private double commissionSum;

  private int dayCount;
  private double maxDayInc = 1;
  private double maxDayDec = 1;
  private double longDealPrice;
  private double shortDealPrice;
  private int dealCount;
  private double maxDealInc = 1;
  private double maxDealDec = 1;

  private int dayEndCloseCount;
  private int stopLossCloseCount;
  private int takeProfitCloseCount;

  public StrategyResult(double startBalance) {
    this.startBalance = startBalance;
    prevDayBalance = startBalance;
    money = startBalance;
    longLots = 0;
    shortLots = 0;
  }

  public int getLongLots() {
    return longLots;
  }
  public int getShortLots() {
    return shortLots;
  }

  public double getTotalChange() {
    return (prevDayBalance / startBalance - 1) * 100;
  }

  public double getLongDealPrice() {
    return longDealPrice;
  }
  public double getShortDealPrice() {
    return shortDealPrice;
  }

  public void longOpen(HistoryCandle candle) {
    longDealPrice = candle.getOpen();
    longLots = (int) (money / longDealPrice);
    money -= (longLots * longDealPrice) * (1 + COMMISSION);
    commissionSum += (longLots * longDealPrice) * COMMISSION;
  }

  public void longCloseEndDay(HistoryCandle candle) {
    dayEndCloseCount++;
    closeLong(candle);
  }

  public void longStopLoss(HistoryCandle candle) {
    stopLossCloseCount++;
    closeLong(candle);
  }

  public void longTakeProfit(HistoryCandle candle) {
    takeProfitCloseCount++;
    closeLong(candle);
  }

  public void shortOpen(HistoryCandle candle) {
    shortDealPrice = candle.getOpen();
    // TODO: 20.10.2024 calculate short lots
    shortLots = longLots;
    commissionSum += (shortLots * shortDealPrice) * COMMISSION;
  }

  public void shortCloseEndDay(HistoryCandle candle) {
    dayEndCloseCount++;
    closeShort(candle);
  }

  public void shortStopLoss(HistoryCandle candle) {
    stopLossCloseCount++;
    closeShort(candle);
  }

  public void shortTakeProfit(HistoryCandle candle) {
    takeProfitCloseCount++;
    closeShort(candle);
  }

  private void closeLong(HistoryCandle candle) {
    double price = candle.getOpen();
    money += (longLots * price) * (1 - COMMISSION);
    commissionSum += (longLots * price) * COMMISSION;
    longLots = 0;
    dealCount++;
    double change = price / longDealPrice;
    maxDealInc = Math.max(maxDealInc, change);
    maxDealDec = Math.min(maxDealDec, change);
  }

  private void closeShort(HistoryCandle candle) {
    double price = candle.getOpen();
    money += shortLots * (shortDealPrice - price) * (1 - COMMISSION);
    commissionSum += (shortLots * price) * COMMISSION;
    shortLots = 0;
    dealCount++;
    double change = shortDealPrice / price;
    maxDealInc = Math.max(maxDealInc, change);
    maxDealDec = Math.min(maxDealDec, change);
  }

  public void endDay(HistoryCandle historyCandle) {
//    if (lots != 0) {
//      throw new RuntimeException("lots != 0");
//    }
    double balance = money + historyCandle.getClose() * longLots;
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
      ", dayEndSellCount=" + dayEndCloseCount + "\n" +
      ", stopLossSellCount=" + stopLossCloseCount + "\n" +
      ", takeProfitSellCount=" + takeProfitCloseCount + "\n" +
      '}';
  }

  private String formatShare(double v) {
    double p = (v - 1.) * 100;
    return String.format("%,.2f%%", p);
  }
}
