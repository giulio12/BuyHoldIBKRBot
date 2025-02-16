package ch.ibkrbot;

import java.util.HashMap;
import java.util.Map;

public class PortfolioOptimizationTest {

    public static void main(String[] args) {
        // Dati di input
        double cashCHF = 1000; // Budget iniziale in CHF
        double cashUSD =0; // Budget iniziale in USD
        double exchangeRate = 0.91; // Tasso di conversione CHF → USD
        double commissionFX = 1.90; // Commissione per conversione CHF → USD
        double commissionCHF = 3.58; // Commissione fissa per acquisto in CHF
        double commissionUSD = 0.35; // Commissione fissa per acquisto in USD
        double cashToInvestCHF = cashCHF-commissionFX;

        // Definizione del portafoglio
        Map<String, Stock> portfolio = new HashMap<>();
        portfolio.put("CHSPI", new Stock("CHSPI", 150.8344269, 32, 0.15, 0.09, "CHF"));
      //  portfolio.put("CHSPI2", new Stock("CHSPI2", 150.8344269, 32, 0.15, 0.1, "CHF"));
        portfolio.put("ACN", new Stock("ACN", 387.179657, 10, 0.0, 0.0708, "USD"));
        portfolio.put("BNDW", new Stock("BNDW", 68.6060028, 151, 0.0, 0.1897, "USD"));
        portfolio.put("VBR", new Stock("VBR", 203.43154905, 18, 0.05, 0.0695, "USD"));
        portfolio.put("VSS", new Stock("VSS", 117.0719986, 33, 0.05, 0.0711, "USD"));
        portfolio.put("VT", new Stock("VT", 122.09400175, 179, 0.6, 0.4006, "USD"));
        portfolio.put("VYM", new Stock("VYM", 132.95399475, 12, 0.05, 0.0291, "USD"));
        portfolio.put("VYMI", new Stock("VYMI", 71.16600035, 55, 0.05, 0.0721, "USD"));


    // Calcolo acquisto in CHF
        double totCashInCHF = cashCHF + cashUSD * exchangeRate; // 280000
        double percentageCashCHF = cashCHF / totCashInCHF; // 0.04

    // Somma delle allocazioni target per i titoli in CHF
        double totTargetAllocationCHF = 0;
        double totCurrentAllocationCHF = 0;
        for (String symbol : portfolio.keySet()) {
            Stock stock = portfolio.get(symbol);
            if (stock.currency.equals("CHF")) {
                totTargetAllocationCHF += stock.targetAllocation;
                totCurrentAllocationCHF += stock.currentAllocation;
            }
        }

        double diffTargetAllocation = totTargetAllocationCHF - totCurrentAllocationCHF;
        double rebalAllocation = totTargetAllocationCHF + diffTargetAllocation;

        if(rebalAllocation*totCashInCHF > cashCHF){
            cashToInvestCHF = cashCHF;
        } else {
            cashToInvestCHF = cashCHF*rebalAllocation;
        }

        for (String symbol : portfolio.keySet()) {
            Stock stock = portfolio.get(symbol);
            if (stock.currency.equals("CHF")) {
                int unitsToBuy = (int) Math.floor(cashToInvestCHF / stock.price*0.98);

                if (unitsToBuy > 0) {
                    System.out.println("Acquisto " + unitsToBuy + " unità di " + stock.symbol);
                }
                else {
                    System.out.println("Nessun acquisto di "  + stock.symbol);
                }
            }
        }
    }

    // Classe per rappresentare un titolo
    static class Stock {
        String symbol;
        double price;
        int position;
        double targetAllocation;
        double currentAllocation;
        String currency;

        Stock(String symbol, double price, int position, double targetAllocation, double currentAllocation, String currency) {
            this.symbol = symbol;
            this.price = price;
            this.position = position;
            this.targetAllocation = targetAllocation;
            this.currentAllocation = currentAllocation;
            this.currency = currency;
        }
    }
}
