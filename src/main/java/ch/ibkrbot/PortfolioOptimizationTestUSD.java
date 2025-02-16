package ch.ibkrbot;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.Optimisation;

import java.util.*;

public class PortfolioOptimizationTestUSD {
    public static void main(String[] args) {
        // Dati di input
        double cashUSD = 800; // Budget iniziale in USD
        double totalTargetUSD = 0.8;

        // Definizione del portafoglio
        Map<String, Stock> portfolio = new HashMap<>();

        // test 1
//        portfolio.put("ACN", new Stock("ACN", 387.179657, 10, 0.0, 0.0708, "USD"));
//        portfolio.put("BNDW", new Stock("BNDW", 68.6060028, 151, 0.0, 0.1897, "USD"));
//        portfolio.put("VBR", new Stock("VBR", 203.43154905, 18, 0.05/totalTargetUSD, 0.0695/totalTargetUSD, "USD"));
//        portfolio.put("VSS", new Stock("VSS", 117.0719986, 33, 0.05/totalTargetUSD, 0.0711/totalTargetUSD, "USD"));
//        portfolio.put("VT", new Stock("VT", 122.09400175, 179, 0.6/totalTargetUSD, 0.4006/totalTargetUSD, "USD"));
//        portfolio.put("VYM", new Stock("VYM", 132.95399475, 12, 0.05/totalTargetUSD, 0.0291/totalTargetUSD, "USD"));
//        portfolio.put("VYMI", new Stock("VYMI", 71.16600035, 55, 0.05/totalTargetUSD, 0.0721/totalTargetUSD, "USD"));

        // test 2
      //  portfolio.put("ACN", new Stock("ACN", 387.179657, 10, 0.0, 0.0708, "USD"));
      //  portfolio.put("BNDW", new Stock("BNDW", 68.6060028, 151, 0.0, 0.1897, "USD"));
        portfolio.put("VBR", new Stock("VBR", 203.43154905, 18, 0.05/totalTargetUSD, 0.05/totalTargetUSD, "USD"));
        portfolio.put("VSS", new Stock("VSS", 117.0719986, 33, 0.05/totalTargetUSD, 0.05/totalTargetUSD, "USD"));
        portfolio.put("VT", new Stock("VT", 122.09400175, 179, 0.6/totalTargetUSD, 0.6/totalTargetUSD, "USD"));
        portfolio.put("VYM", new Stock("VYM", 132.95399475, 12, 0.05/totalTargetUSD, 0.05/totalTargetUSD, "USD"));
        portfolio.put("VYMI", new Stock("VYMI", 71.16600035, 55, 0.05/totalTargetUSD, 0.05/totalTargetUSD, "USD"));

        // Creazione del modello di ottimizzazione
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Map<String, Variable> variables = new HashMap<>();

        // Calcoliamo il totale dell'allocazione target per i titoli in USD
        double totalTargetAllocationUSD = 0;
        for (Stock stock : portfolio.values()) {
            if (stock.currency.equals("USD")) {
                totalTargetAllocationUSD += stock.targetAllocation;
            }
        }

        // Creazione delle variabili per ogni titolo in USD
        for (Stock stock : portfolio.values()) {
            if (stock.currency.equals("USD") && stock.targetAllocation>0) {
                // Variabile intera per il numero di titoli acquistabili
                Variable x = model.addVariable(stock.symbol).integer(true).lower(0);
                variables.put(stock.symbol, x);
            }
        }

        // Funzione Obiettivo: Massimizzare l'allocazione del capitale dando priorità ai titoli sottoallocati
        Expression objective = model.addExpression("MaximizeAllocation");

        for (Stock stock : portfolio.values()) {
            if (stock.currency.equals("USD") && stock.targetAllocation>0) {
                Variable v = variables.get(stock.symbol);
                objective.set(variables.get(stock.symbol), stock.price);
            }
        }
        objective.weight(1.0); // Massimizzazione

        // Vincolo di Budget: Non possiamo superare il cash disponibile in USD
        Expression budgetConstraint = model.addExpression("Budget");
        for (Stock stock : portfolio.values()) {
            if (stock.currency.equals("USD") && stock.targetAllocation>0) {
                budgetConstraint.set(variables.get(stock.symbol), stock.price);
            }
        }
        budgetConstraint.upper(cashUSD); // Il totale investito non può superare il cash USD disponibile


        // Calcolo delle sottoallocazioni e sovrallocazioni
        double totalUnderAllocated = 0; // S^+
        double totalOverAllocated = 0;  // S^-
        double beta = 0.3; // Fattore di riduzione per i titoli sovrallocati

        for (Stock stock : portfolio.values()) {
            if (stock.currency.equals("USD") && stock.targetAllocation>0) {
                double deltaA = stock.targetAllocation - stock.currentAllocation;
                if (deltaA < 0) {
                    totalUnderAllocated += -deltaA;
                } else {
                    totalOverAllocated += deltaA;
                }
            }
        }

        // Creazione del vincolo di allocazione proporzionale
        for (Stock stock : portfolio.values()) {
            if (stock.currency.equals("USD") && stock.targetAllocation > 0) {
                double deltaA = stock.currentAllocation - stock.targetAllocation;
                Expression allocationConstraint = model.addExpression("Allocation_" + stock.symbol);
                Variable v = variables.get(stock.symbol);

                if (deltaA > 0.01 && totalUnderAllocated > 0) { // Stock is over allocated
                        allocationConstraint.set(v, stock.price);
                        double allocationFactor = beta*(deltaA) / totalOverAllocated;
                        allocationConstraint.upper(allocationFactor * cashUSD);
                } else if (deltaA < -0.01 && totalOverAllocated > 0) { // Stock is under allocated
                        allocationConstraint.set(v, stock.price);
                        double allocationFactor = -deltaA / totalUnderAllocated;
                        allocationConstraint.upper(allocationFactor * cashUSD);
                } else { // Stock is correctly allocated
                    allocationConstraint.set(v, stock.price);
                    double allocationFactor = stock.targetAllocation;
                    allocationConstraint.upper(Math.max(allocationFactor*cashUSD, stock.price));
                }
            }
        }







        // Risoluzione del modello
        Optimisation.Result result = model.maximise();

        System.out.println(model);

        // Output dei risultati
        System.out.println("Ottimizzazione completata");
        if (result.getState().isFeasible()) {
            double remainingCashUSD = cashUSD;
            System.out.println("Soluzione trovata:");
            for (Stock stock : portfolio.values()) {
                if (variables.containsKey(stock.symbol)) {
                    int unitsToBuy = variables.get(stock.symbol).getValue().intValue();
                    if (unitsToBuy > 0) {
                        remainingCashUSD -= unitsToBuy * stock.price;
                        System.out.println("Compra " + unitsToBuy + " unità di " + stock.symbol);
                    }
                }
            }
            System.out.print("Cash Rimanente "+remainingCashUSD );
        } else {
            System.out.println("Nessuna soluzione fattibile trovata.");
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

        // Positive = over allocated, Negative = under allocated
        public double getDeltaAllocation(){
            return this.currentAllocation - this.targetAllocation;
        }
    }
}
