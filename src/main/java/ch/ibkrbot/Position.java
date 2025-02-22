package ch.ibkrbot;

import com.ib.client.Contract;
import com.ib.client.Decimal;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Position {

    public Contract contract;
    public String accountName;
    public Decimal position; // IBKR usa Decimal per le quantità
    public BigDecimal marketPrice;
    public BigDecimal marketValue;
    public BigDecimal averageCost;
    public BigDecimal unrealizedPNL;
    public BigDecimal realizedPNL;
    public BigDecimal targetAllocation;
    public BigDecimal currentAllocation;

    public Position(Contract contract, String accountName, Decimal position, double marketPrice, double marketValue,
                    double averageCost, double unrealizedPNL, double realizedPNL) {
        this.contract = contract;
        this.accountName = accountName;
        this.position = position;
        this.marketPrice = BigDecimal.valueOf(marketPrice).setScale(2, RoundingMode.HALF_UP);
        this.marketValue = BigDecimal.valueOf(marketValue).setScale(2, RoundingMode.HALF_UP);
        this.averageCost = BigDecimal.valueOf(averageCost).setScale(2, RoundingMode.HALF_UP);
        this.unrealizedPNL = BigDecimal.valueOf(unrealizedPNL).setScale(2, RoundingMode.HALF_UP);
        this.realizedPNL = BigDecimal.valueOf(realizedPNL).setScale(2, RoundingMode.HALF_UP);
        this.targetAllocation = BigDecimal.ZERO;
        this.currentAllocation = BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return String.format("Position{Symbol='%s', Account='%s', Position=%s, MarketPrice=%s, MarketValue=%s, AverageCost=%s, UnrealizedPNL=%s, RealizedPNL=%s, TargetAllocation=%s, CurrentAllocation=%s, Currency='%s'}",
                contract.symbol(), accountName, position, marketPrice, marketValue, averageCost,
                unrealizedPNL, realizedPNL, targetAllocation, currentAllocation, contract.currency());
    }

}
