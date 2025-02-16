package ch.ibkrbot;


import java.util.*;

public class Portfolio {

    private final Map<String, Double> exchangeRate;
    private final Map<String, Double> targetAllocations;
    private final Map<String, Double> stockMarketValue;
    private final Map<String, Double> totalStockMarketValue;
    private final Map<String, Double> cashBalances;
    private final Map<String, Double> totalCashBalances;
    private final List<Position> positionList;

    public Portfolio(){
        this.exchangeRate = new HashMap<>();
        this.targetAllocations = new HashMap<>();
        this.stockMarketValue = new HashMap<>();
        this.totalStockMarketValue = new HashMap<>();
        this.cashBalances = new HashMap<>();
        this.totalCashBalances = new HashMap<>();
        this.positionList = new ArrayList<>();

        // Definizione percentuali target
        targetAllocations.put("CHSPI", 0.15);
        targetAllocations.put("BNDW", 0.0);
        targetAllocations.put("ACN", 0.00);
        targetAllocations.put("VT", 0.6);
        targetAllocations.put("VBR", 0.05);
        targetAllocations.put("VSS", 0.05);
        targetAllocations.put("VYMI", 0.05);
        targetAllocations.put("VYM", 0.05);
    }

    public void addCashBalance(String curry, double amount) {
        this.cashBalances.put(curry,amount);
    }

    public void addPosition(Position position) {
        positionList.stream().filter(p -> p.symbol.equals(position.symbol)).forEach(positionList::remove);
        this.positionList.add(position);
    }

    public void addExchangeRate(String curry, double rate){
        this.exchangeRate.put(curry,rate);
    }

    public void addStockMarketValue(String curry, double amount) {
        stockMarketValue.put(curry, amount);
    }

    public void addTotalStockMarketValue(String curry, double rate){
        this.totalStockMarketValue.put(curry, rate);
    }

    public void addTotalCashBalance(String curry, double amount) {
        this.totalCashBalances.put(curry, amount);
    }

    public double getExchangeRate(String curry) {
        return exchangeRate.get(curry);
    }

    public List<String> getAllCurry(){
        return new ArrayList<>(exchangeRate.keySet());
    }

    public double getTargetAllocations(String symbol) {
        return targetAllocations.get(symbol);
    }

    public List<Position> getPositionList() {
        return positionList;
    }

    public double getCashBalance(String curry) {
        return cashBalances.get(curry);
    }

    public double getTotalCashBalance(String curry) {
        return totalCashBalances.get(curry);
    }

    public double getStockMarketValue(String curry) {
        return stockMarketValue.get(curry);
    }

    public double getTotalStockMarketValue(String curry) {
        return totalStockMarketValue.get(curry);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Portfolio Details:\n");

        sb.append("Base Currency: ").append(Constants.BASE_CURRY).append("\n");

        sb.append("\nExchange Rates:\n");
        for (Map.Entry<String, Double> entry : exchangeRate.entrySet()) {
            sb.append("Currency: ").append(entry.getKey())
                    .append(", Rate: ").append(entry.getValue()).append("\n");
        }

        sb.append("\nTarget Allocations:\n");
        for (Map.Entry<String, Double> entry : targetAllocations.entrySet()) {
            sb.append("Symbol: ").append(entry.getKey())
                    .append(", Target Allocation: ").append(entry.getValue() * 100).append("%\n");
        }

        sb.append("\nStock Market Values:\n");
        for (Map.Entry<String, Double> entry : stockMarketValue.entrySet()) {
            sb.append("Currency: ").append(entry.getKey())
                    .append(", Market Value: ").append(entry.getValue()).append("\n");
        }

        sb.append("\nTotal Stock Market Values:\n");
        for (Map.Entry<String, Double> entry : totalStockMarketValue.entrySet()) {
            sb.append("Currency: ").append(entry.getKey())
                    .append(", Total Market Value: ").append(entry.getValue()).append("\n");
        }

        sb.append("\nCash Balances:\n");
        for (Map.Entry<String, Double> entry : cashBalances.entrySet()) {
            sb.append("Currency: ").append(entry.getKey())
                    .append(", Cash Balance: ").append(entry.getValue()).append("\n");
        }

        sb.append("\nTotal Cash Balances:\n");
        for (Map.Entry<String, Double> entry : totalCashBalances.entrySet()) {
            sb.append("Currency: ").append(entry.getKey())
                    .append(", Total Cash Balance: ").append(entry.getValue()).append("\n");
        }

        sb.append("\nPositions:\n");
        for (Position position : positionList) {
            sb.append(position).append("\n");
        }

        return sb.toString();
    }
}
