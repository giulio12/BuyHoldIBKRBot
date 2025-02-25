package ch.ibkrbot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Portfolio {

    private final Map<String, BigDecimal> exchangeRate;
    private final Map<String, BigDecimal> targetAllocations;
    private final Map<String, BigDecimal> stockMarketValue;
    private final Map<String, BigDecimal> totalStockMarketValue;
    private final Map<String, BigDecimal> cashBalances;
    private final Map<String, BigDecimal> totalCashBalances;
    private final List<Position> positionList;

    public Portfolio() {
        this.exchangeRate = new HashMap<>();
        this.targetAllocations = new HashMap<>();
        this.stockMarketValue = new HashMap<>();
        this.totalStockMarketValue = new HashMap<>();
        this.cashBalances = new HashMap<>();
        this.totalCashBalances = new HashMap<>();
        this.positionList = new ArrayList<>();

        // Definizione percentuali target
        targetAllocations.put("CHSPI", BigDecimal.valueOf(0.15));
        targetAllocations.put("BNDW", BigDecimal.ZERO);
        targetAllocations.put("ACN", BigDecimal.ZERO);
        targetAllocations.put("VT", BigDecimal.valueOf(0.6));
        targetAllocations.put("VBR", BigDecimal.valueOf(0.05));
        targetAllocations.put("VSS", BigDecimal.valueOf(0.05));
        targetAllocations.put("VYMI", BigDecimal.valueOf(0.05));
        targetAllocations.put("VYM", BigDecimal.valueOf(0.05));
    }

    public void addCashBalance(String curry, BigDecimal amount) {
        this.cashBalances.put(curry, amount);
    }

    public void addPosition(Position position) {
        positionList.removeIf(p -> p.contract.symbol().equals(position.contract.symbol()));
        this.positionList.add(position);
    }

    public void addExchangeRate(String curry, BigDecimal rate) {
        this.exchangeRate.put(curry, rate);
    }

    public void addStockMarketValue(String curry, BigDecimal amount) {
        stockMarketValue.put(curry, amount);
    }

    public void addTotalStockMarketValue(String curry, BigDecimal rate) {
        this.totalStockMarketValue.put(curry, rate);
    }

    public void addTotalCashBalance(String curry, BigDecimal amount) {
        this.totalCashBalances.put(curry, amount);
    }

    public BigDecimal getExchangeRate(String curry) {
        return exchangeRate.getOrDefault(curry, BigDecimal.ONE);
    }

    public List<String> getAllCurry() {
        return new ArrayList<>(exchangeRate.keySet());
    }

    public BigDecimal getTargetAllocations(String symbol) {
        return targetAllocations.getOrDefault(symbol, BigDecimal.ZERO);
    }

    public List<Position> getPositionList() {
        return positionList;
    }

    public Position getPosition(String symbol){
        for(Position pos : positionList)
            if(pos.contract.symbol().equals(symbol))
                return pos;
        return null;
    }

    public BigDecimal getCashBalance(String curry) {
        return cashBalances.getOrDefault(curry, BigDecimal.ZERO);
    }

    public BigDecimal getTotalCashBalance(String curry) {
        return totalCashBalances.getOrDefault(curry, BigDecimal.ZERO);
    }

    public BigDecimal getStockMarketValue(String curry) {
        return stockMarketValue.getOrDefault(curry, BigDecimal.ZERO);
    }

    public BigDecimal getTotalStockMarketValue(String curry) {
        return totalStockMarketValue.getOrDefault(curry, BigDecimal.ZERO);
    }

    public List<String> getAllCurryPositions() {
        List<String> result = new ArrayList<>();
        for(Position p : this.positionList){
            if(!result.contains(p.contract.currency()))
                result.add(p.contract.currency());
        }
        return result;
    }

    public BigDecimal getTotalTargetAllocationForCurry(String curry) {
        BigDecimal result = new BigDecimal(0);
        for(Position p : this.positionList){
            if(p.contract.currency().equals(curry))
                result = result.add(p.targetAllocation);
        }
        return result;
    }

    public BigDecimal getTargetAllocationForCurry(String symbol, String curry) {
        Position pos = getPosition(symbol);
        assert pos != null : "pos cannot be null in getTargetAllocationForCurry";
        return pos.targetAllocation.divide(getTotalTargetAllocationForCurry(curry));
    }

    public BigDecimal getCurrentAllocationForCurry(String symbol, String curry) {
        Position pos = getPosition(symbol);
        assert pos != null : "pos cannot be null in getTargetAllocationForCurry";
        return pos.currentAllocation.divide(getTotalTargetAllocationForCurry(curry));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Portfolio Details:\n");
        sb.append("Base Currency: ").append(Constants.BASE_CURRY).append("\n");

        sb.append("\nExchange Rates:\n");
        for (Map.Entry<String, BigDecimal> entry : exchangeRate.entrySet()) {
            sb.append("Currency: ").append(entry.getKey())
                    .append(", Rate: ").append(entry.getValue()).append("\n");
        }

        sb.append("\nTarget Allocations:\n");
        for (Map.Entry<String, BigDecimal> entry : targetAllocations.entrySet()) {
            sb.append("Symbol: ").append(entry.getKey())
                    .append(", Target Allocation: ").append(entry.getValue().multiply(BigDecimal.valueOf(100))).append("%\n");
        }

        sb.append("\nStock Market Values:\n");
        for (Map.Entry<String, BigDecimal> entry : stockMarketValue.entrySet()) {
            sb.append("Currency: ").append(entry.getKey())
                    .append(", Market Value: ").append(entry.getValue()).append("\n");
        }

        sb.append("\nTotal Stock Market Values:\n");
        for (Map.Entry<String, BigDecimal> entry : totalStockMarketValue.entrySet()) {
            sb.append("Currency: ").append(entry.getKey())
                    .append(", Total Market Value: ").append(entry.getValue()).append("\n");
        }

        sb.append("\nCash Balances:\n");
        for (Map.Entry<String, BigDecimal> entry : cashBalances.entrySet()) {
            sb.append("Currency: ").append(entry.getKey())
                    .append(", Cash Balance: ").append(entry.getValue()).append("\n");
        }

        sb.append("\nTotal Cash Balances:\n");
        for (Map.Entry<String, BigDecimal> entry : totalCashBalances.entrySet()) {
            sb.append("Currency: ").append(entry.getKey())
                    .append(", Total Cash Balance: ").append(entry.getValue()).append("\n");
        }

        sb.append("\nPositions:\n");
        for (Position position : positionList) {
            sb.append(position).append("\n");
        }

        return sb.toString();
    }

    public void reduceCashPosition(BigDecimal spentAmount, String currency) {
        BigDecimal remainingCash = getCashBalance(currency).subtract(spentAmount).setScale(2, RoundingMode.HALF_UP);
        addCashBalance(currency, remainingCash);

        // Remove cash balance BASE
        BigDecimal baseSpent = spentAmount.multiply(getExchangeRate(currency)).setScale(2, RoundingMode.HALF_UP);
        addCashBalance("BASE", getCashBalance("BASE").subtract(baseSpent));

        // Update totalCash position
        for (String curry : getAllCurry()) {
            BigDecimal xrate = getExchangeRate(curry);
            BigDecimal totalCashBalance = getCashBalance("BASE").divide(xrate, 2, RoundingMode.HALF_UP);
            addTotalCashBalance(curry, totalCashBalance);
        }
    }
}
