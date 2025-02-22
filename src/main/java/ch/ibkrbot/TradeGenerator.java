package ch.ibkrbot;

import com.ib.client.Decimal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class TradeGenerator {

    private int tradeCounter;

    public TradeGenerator() {
        this.tradeCounter = 1000;
    }

    public List<TradeInstruction> createDomesticOrders(Portfolio portfolio) {
        List<TradeInstruction> trades = new ArrayList<>();

        // Recupera i valori come BigDecimal per evitare errori di precisione
        BigDecimal totalCashInDomestic = portfolio.getTotalCashBalance(Constants.BASE_CURRY);
        BigDecimal cashDomestic = portfolio.getCashBalance(Constants.BASE_CURRY);
        BigDecimal cashDomesticToInvest = BigDecimal.ZERO;

        // Somma delle allocazioni target nella valuta domestica
        BigDecimal totTargetAllocationDomestic = BigDecimal.ZERO;
        BigDecimal totCurrentAllocationDomestic = BigDecimal.ZERO;

        for (Position pos : portfolio.getPositionList()) {
            if (pos.contract.currency().equals(Constants.BASE_CURRY)) {
                totTargetAllocationDomestic = totTargetAllocationDomestic.add(pos.targetAllocation);
                totCurrentAllocationDomestic = totCurrentAllocationDomestic.add(pos.currentAllocation);
            }
        }

        BigDecimal diffTargetAllocation = totTargetAllocationDomestic.subtract(totCurrentAllocationDomestic);
        BigDecimal rebalAllocation = totTargetAllocationDomestic.add(diffTargetAllocation);

        if (rebalAllocation.multiply(totalCashInDomestic).compareTo(cashDomestic) > 0) {
            cashDomesticToInvest = cashDomestic;
        } else {
            cashDomesticToInvest = cashDomestic.multiply(rebalAllocation);
        }

        for (Position pos : portfolio.getPositionList()) {
            if (pos.contract.currency().equals(Constants.BASE_CURRY)) {
                BigDecimal price = pos.marketPrice.multiply(Constants.PRCTG_LIMIT_PRICE).setScale(2, RoundingMode.HALF_UP);

                BigDecimal unitsToBuy = cashDomesticToInvest.divide(price, 0, RoundingMode.FLOOR);

                if (unitsToBuy.compareTo(BigDecimal.ZERO) > 0) {
                    System.out.println("Creation of order " + unitsToBuy + " for units " + pos.contract.symbol() + " at price " + price);
                    trades.add(new TradeInstruction(tradeCounter,pos.contract.symbol(), pos.contract.getSecType(),pos.contract.currency(), price, Decimal.get(unitsToBuy), false));
                    tradeCounter++;
                } else {
                    System.out.println("No orders placed for " + pos.contract.symbol());
                }
            }
        }
        return trades;
    }

    public TradeInstruction createForexOrder(String baseCurrency, String quoteCurrency, BigDecimal amount) {
        BigDecimal exchangeRate = new BigDecimal("1.9"); // Fetch correct exchange rate

        return new TradeInstruction(
                tradeCounter,
                baseCurrency,  // Correctly use base currency as symbol
                "CASH",        // Use "CASH" for forex orders
                quoteCurrency,
                exchangeRate,  // Use actual exchange rate
                Decimal.get(amount),
                false          // Market order
        ); }
}
