package ch.ibkrbot;

import com.ib.client.*;

import java.util.*;

public class ClientWrapper implements EWrapper {


    private Map<String, List<Observer>> observers;

    public ClientWrapper() {
        this.observers = new HashMap<>();
    }

    public void addObserver(String notificationName, Observer o) {
        if (!this.observers.containsKey(notificationName))
            this.observers.put(notificationName, new ArrayList<>());
        this.observers.get(notificationName).add(o);
    }


    public void removeObserver(String notificationName, Observer o) {
        if (this.observers.containsKey(notificationName))
            this.observers.get(notificationName).remove(o);
    }


    @Override
    public void tickPrice(int i, int i1, double v, TickAttrib tickAttrib) {

    }

    @Override
    public void tickSize(int i, int i1, Decimal decimal) {

    }

    @Override
    public void tickOptionComputation(int i, int i1, int i2, double v, double v1, double v2, double v3, double v4, double v5, double v6, double v7) {

    }

    @Override
    public void tickGeneric(int i, int i1, double v) {

    }

    @Override
    public void tickString(int i, int i1, String s) {

    }

    @Override
    public void tickEFP(int i, int i1, double v, String s, double v1, int i2, String s1, double v2, double v3) {

    }

    @Override
    public void orderStatus(int i, String s, Decimal decimal, Decimal decimal1, double v, int i1, int i2, double v1, int i3, String s1, double v2) {
        System.out.println("Stato Ordine ID " + i + ": " + s + " | Filled: " + decimal + " | Remaining: " + decimal1);

    }

    @Override
    public void openOrder(int i, Contract contract, Order order, OrderState orderState) {

    }

    @Override
    public void openOrderEnd() {

    }

    @Override
    public void updateAccountValue(String s, String s1, String s2, String s3) {

    }

    @Override
    public void updatePortfolio(Contract contract, Decimal position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
        Position pos = new Position(contract, accountName, position, marketPrice, marketValue, averageCost, unrealizedPNL, realizedPNL);
        for (Observer o : this.observers.get("updatePortfolio")) {
            o.notify("updatePortfolio", pos);
        }
    }

    @Override
    public void updateAccountTime(String s) {

    }

    @Override
    public void accountDownloadEnd(String s) {

    }

    @Override
    public void nextValidId(int i) {
        System.out.println("NEXT valid ID " + i);
        if (this.observers.containsKey("nextValidId")) {
            for (Observer o : this.observers.get("nextValidId")) {
                o.notify("nextValidId", i);
            }
        }
    }

    @Override
    public void contractDetails(int i, ContractDetails contractDetails) {

    }

    @Override
    public void bondContractDetails(int i, ContractDetails contractDetails) {

    }

    @Override
    public void contractDetailsEnd(int i) {

    }

    @Override
    public void execDetails(int i, Contract contract, Execution execution) {

    }

    @Override
    public void execDetailsEnd(int i) {

    }

    @Override
    public void updateMktDepth(int i, int i1, int i2, int i3, double v, Decimal decimal) {

    }

    @Override
    public void updateMktDepthL2(int i, int i1, String s, int i2, int i3, double v, Decimal decimal, boolean b) {

    }

    @Override
    public void updateNewsBulletin(int i, int i1, String s, String s1) {

    }

    @Override
    public void managedAccounts(String s) {

    }

    @Override
    public void receiveFA(int i, String s) {

    }

    @Override
    public void historicalData(int i, Bar bar) {

    }

    @Override
    public void scannerParameters(String s) {

    }

    @Override
    public void scannerData(int i, int i1, ContractDetails contractDetails, String s, String s1, String s2, String s3) {

    }

    @Override
    public void scannerDataEnd(int i) {

    }

    @Override
    public void realtimeBar(int i, long l, double v, double v1, double v2, double v3, Decimal decimal, Decimal decimal1, int i1) {

    }

    @Override
    public void currentTime(long l) {

    }

    @Override
    public void fundamentalData(int i, String s) {

    }

    @Override
    public void deltaNeutralValidation(int i, DeltaNeutralContract deltaNeutralContract) {

    }

    @Override
    public void tickSnapshotEnd(int i) {

    }

    @Override
    public void marketDataType(int i, int i1) {

    }

    @Override
    public void commissionReport(CommissionReport commissionReport) {

    }

    @Override
    public void position(String account, Contract contract, Decimal pos, double avgCost) {
    }

    @Override
    public void positionEnd() {
    }

    @Override
    public void accountSummary(int reqId, String account, String tag, String value, String currency) {
        if (Util.isDouble(value)) {
            Balance b = new Balance();
            b.balanceType = tag;
            b.currency = currency;
            b.balance = Double.parseDouble(value);
            for (Observer o : this.observers.get("accountSummary"))
                o.notify("accountSummary", b);
        }
    }

    @Override
    public void accountSummaryEnd(int i) {
        for (Observer o : this.observers.get("accountSummaryEnd"))
            o.notify("accountSummaryEnd", null);
    }

    @Override
    public void verifyMessageAPI(String s) {

    }

    @Override
    public void verifyCompleted(boolean b, String s) {

    }

    @Override
    public void verifyAndAuthMessageAPI(String s, String s1) {

    }

    @Override
    public void verifyAndAuthCompleted(boolean b, String s) {

    }

    @Override
    public void displayGroupList(int i, String s) {

    }

    @Override
    public void displayGroupUpdated(int i, String s) {

    }

    @Override
    public void error(Exception e) {

    }

    @Override
    public void error(String s) {

    }

    @Override
    public void error(int i, int i1, String s, String s1) {
        System.out.println("Errore [" + i1 + "]: " + s + " " + s1);
    }

    @Override
    public void connectionClosed() {

    }

    @Override
    public void connectAck() {

    }

    @Override
    public void positionMulti(int i, String s, String s1, Contract contract, Decimal decimal, double v) {

    }

    @Override
    public void positionMultiEnd(int i) {

    }

    @Override
    public void accountUpdateMulti(int i, String s, String s1, String s2, String s3, String s4) {

    }

    @Override
    public void accountUpdateMultiEnd(int i) {

    }

    @Override
    public void securityDefinitionOptionalParameter(int i, String s, int i1, String s1, String s2, Set<String> set, Set<Double> set1) {

    }

    @Override
    public void securityDefinitionOptionalParameterEnd(int i) {

    }

    @Override
    public void softDollarTiers(int i, SoftDollarTier[] softDollarTiers) {

    }

    @Override
    public void familyCodes(FamilyCode[] familyCodes) {

    }

    @Override
    public void symbolSamples(int i, ContractDescription[] contractDescriptions) {

    }

    @Override
    public void historicalDataEnd(int i, String s, String s1) {

    }

    @Override
    public void mktDepthExchanges(DepthMktDataDescription[] depthMktDataDescriptions) {

    }

    @Override
    public void tickNews(int i, long l, String s, String s1, String s2, String s3) {

    }

    @Override
    public void smartComponents(int i, Map<Integer, Map.Entry<String, Character>> map) {

    }

    @Override
    public void tickReqParams(int i, double v, String s, int i1) {

    }

    @Override
    public void newsProviders(NewsProvider[] newsProviders) {

    }

    @Override
    public void newsArticle(int i, int i1, String s) {

    }

    @Override
    public void historicalNews(int i, String s, String s1, String s2, String s3) {

    }

    @Override
    public void historicalNewsEnd(int i, boolean b) {

    }

    @Override
    public void headTimestamp(int i, String s) {

    }

    @Override
    public void histogramData(int i, List<HistogramEntry> list) {

    }

    @Override
    public void historicalDataUpdate(int i, Bar bar) {

    }

    @Override
    public void rerouteMktDataReq(int i, int i1, String s) {

    }

    @Override
    public void rerouteMktDepthReq(int i, int i1, String s) {

    }

    @Override
    public void marketRule(int i, PriceIncrement[] priceIncrements) {

    }

    @Override
    public void pnl(int i, double v, double v1, double v2) {

    }

    @Override
    public void pnlSingle(int i, Decimal decimal, double v, double v1, double v2, double v3) {

    }

    @Override
    public void historicalTicks(int i, List<HistoricalTick> list, boolean b) {

    }

    @Override
    public void historicalTicksBidAsk(int i, List<HistoricalTickBidAsk> list, boolean b) {

    }

    @Override
    public void historicalTicksLast(int i, List<HistoricalTickLast> list, boolean b) {

    }

    @Override
    public void tickByTickAllLast(int i, int i1, long l, double v, Decimal decimal, TickAttribLast tickAttribLast, String s, String s1) {

    }

    @Override
    public void tickByTickBidAsk(int i, long l, double v, double v1, Decimal decimal, Decimal decimal1, TickAttribBidAsk tickAttribBidAsk) {

    }

    @Override
    public void tickByTickMidPoint(int i, long l, double v) {

    }

    @Override
    public void orderBound(long l, int i, int i1) {

    }

    @Override
    public void completedOrder(Contract contract, Order order, OrderState orderState) {

    }

    @Override
    public void completedOrdersEnd() {

    }

    @Override
    public void replaceFAEnd(int i, String s) {

    }

    @Override
    public void wshMetaData(int i, String s) {

    }

    @Override
    public void wshEventData(int i, String s) {

    }

    @Override
    public void historicalSchedule(int i, String s, String s1, String s2, List<HistoricalSession> list) {

    }

    @Override
    public void userInfo(int i, String s) {

    }

}
