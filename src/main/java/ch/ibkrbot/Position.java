package ch.ibkrbot;

import com.ib.client.Contract;
import com.ib.client.Decimal;

public class Position {
    public String symbol;
    public String accountName;
    public Decimal position;
    public double marketPrice;
    public double marketValue;
    public double averageCost;
    public double unrealizedPNL;
    public double realizedPNL;
    public double targetAllocation;
    public double currentAllocation;
    public String currency;

    public Position(Contract contract, String accountName, Decimal position, double marketPrice, double marketValue,
                    double averageCost, double unrealizedPNL, double realizedPNL, double targetAllocation, double currentAllocation) {
        this.symbol = contract.symbol();
        this.accountName = accountName;
        this.position = position;
        this.marketPrice = marketPrice;
        this.marketValue = marketValue;
        this.averageCost = averageCost;
        this.unrealizedPNL = unrealizedPNL;
        this.realizedPNL = realizedPNL;
        this.targetAllocation = targetAllocation;
        this.currentAllocation = currentAllocation;
        this.currency = contract.currency();
    }

    public Position(Contract contract, String accountName, Decimal position, double marketPrice, double marketValue,
                    double averageCost, double unrealizedPNL, double realizedPNL) {
        this.symbol = contract.symbol();
        this.accountName = accountName;
        this.position = position;
        this.marketPrice = marketPrice;
        this.marketValue = marketValue;
        this.averageCost = averageCost;
        this.unrealizedPNL = unrealizedPNL;
        this.realizedPNL = realizedPNL;
        this.targetAllocation = targetAllocation;
        this.currentAllocation = currentAllocation;
        this.currency = contract.currency();
    }

    // Metodo toString per debug
    @Override
    public String toString() {
        return "Stock{" +
                "symbol='" + symbol + '\'' +
                ", accountName='" + accountName + '\'' +
                ", position=" + position +
                ", marketPrice=" + marketPrice +
                ", marketValue=" + marketValue +
                ", averageCost=" + averageCost +
                ", unrealizedPNL=" + unrealizedPNL +
                ", realizedPNL=" + realizedPNL +
                ", targetAllocation=" + targetAllocation +
                ", currentAllocation=" + currentAllocation +
                ", currency='" + currency + '\'' +
                '}';
    }
}