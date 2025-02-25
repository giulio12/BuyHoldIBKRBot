package ch.ibkrbot;

import com.ib.client.Decimal;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TradeGenerator {

    private int tradeCounter;

    public TradeGenerator() {
        this.tradeCounter = 1000;
    }

    public List<TradeInstruction> createDomesticTrades(Portfolio portfolio) {
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
        Decimal units = Decimal.get(amount.setScale(0,RoundingMode.FLOOR));

        return new TradeInstruction(
                tradeCounter,
                baseCurrency,  // Correctly use base currency as symbol
                "CASH",        // Use "CASH" for forex orders
                quoteCurrency,
                null,  // Use actual exchange rate
                units,
                true          // Market order
        ); }

    public List<TradeInstruction> createForeignTrades(Portfolio portfolio, String curry) {
        List<TradeInstruction> trades = new ArrayList<>();

        // Creation of the optmization model
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Map<String, Variable> variables = new HashMap<>();

//        // Calcoliamo il totale dell'allocazione target per i titoli in USD
//        double totalTargetAllocationUSD = 0;
//        for (PortfolioOptimizationTestUSD.Stock stock : portfolio.values()) {
//            if (stock.currency.equals("USD")) {
//                totalTargetAllocationUSD += stock.targetAllocation;
//            }
//        }

        // Creation of variables for each foreign title
        for (Position pos : portfolio.getPositionList()) {
            BigDecimal targetAllocation = portfolio.getTargetAllocationForCurry(pos.contract.symbol(), curry);
            if (pos.contract.currency().equals(curry) && targetAllocation.compareTo(BigDecimal.ZERO) > 0) {
                Variable x = model.addVariable(pos.contract.symbol()).integer(true).lower(0);
                variables.put(pos.contract.symbol(), x);
            }
        }

        // Objective function: maximize allocation giving priority to under allocated titles
        Expression objective = model.addExpression("MaximizeAllocation");

        for (Position pos : portfolio.getPositionList()) {
            BigDecimal targetAllocation = portfolio.getTargetAllocationForCurry(pos.contract.symbol(), curry);
            if (pos.contract.currency().equals(curry) && targetAllocation.compareTo(BigDecimal.ZERO) > 0) {
                Variable v = variables.get(pos.contract.symbol());
                objective.set(variables.get(pos.contract.symbol()), pos.marketPrice);
            }
        }
        objective.weight(1.0); // Maximize

        // 1. Constraint: don't exceed available amount in USD
        Expression budgetConstraint = model.addExpression("Budget");
        for (Position pos : portfolio.getPositionList()) {
            BigDecimal targetAllocation = portfolio.getTargetAllocationForCurry(pos.contract.symbol(), curry);
            if (pos.contract.currency().equals(curry) && targetAllocation.compareTo(BigDecimal.ZERO) > 0) {
                budgetConstraint.set(variables.get(pos.contract.symbol()), pos.marketPrice);
            }
        }
        budgetConstraint.upper(portfolio.getCashBalance(curry)); // Total to be invested cannot exceed available amount


        // Calculate over allocated vs. under allocated
        BigDecimal totalUnderAllocated = BigDecimal.valueOf(0); // S^+
        BigDecimal totalOverAllocated = BigDecimal.valueOf(0);  // S^-
        BigDecimal beta = BigDecimal.valueOf(0.3); // Reduction factor for overallocated

        for (Position pos : portfolio.getPositionList()) {
            BigDecimal targetAllocation = portfolio.getTargetAllocationForCurry(pos.contract.symbol(), curry);
            BigDecimal currentAllocation = portfolio.getCurrentAllocationForCurry(pos.contract.symbol(), curry);
            if (pos.contract.currency().equals(curry) && targetAllocation.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal deltaA = targetAllocation.subtract(currentAllocation);
                if (deltaA.compareTo(BigDecimal.ZERO) > 0) {
                    totalUnderAllocated = totalUnderAllocated.subtract(deltaA);
                } else {
                    totalOverAllocated = totalOverAllocated.add(deltaA);
                }
            }
        }

        // Creazione del vincolo di allocazione proporzionale
        // 2. Constraint: proportional allocation according to under vs. over allocated
        for (Position pos : portfolio.getPositionList()) {
            BigDecimal targetAllocation = portfolio.getTargetAllocationForCurry(pos.contract.symbol(), curry);
            if (pos.contract.currency().equals(curry) && targetAllocation.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal currentAllocation = portfolio.getCurrentAllocationForCurry(pos.contract.symbol(), curry);
                BigDecimal deltaA = currentAllocation.subtract(targetAllocation);
                Expression allocationConstraint = model.addExpression("Allocation_" + pos.contract.symbol());
                Variable v = variables.get(pos.contract.symbol());

                if (deltaA.compareTo(new BigDecimal(0.01)) > 0 && totalUnderAllocated.compareTo(BigDecimal.ZERO) > 0) { // Stock is over allocated
                    allocationConstraint.set(v, pos.marketPrice);
                    BigDecimal allocationFactor = beta.multiply(deltaA).divide(totalOverAllocated);
                    allocationConstraint.upper(allocationFactor.multiply(portfolio.getCashBalance(curry)));
                } else if (deltaA.compareTo(new BigDecimal(-0.01)) < 0 && totalOverAllocated.compareTo(BigDecimal.ZERO) > 0) { // Stock is under allocated

                    allocationConstraint.set(v, pos.marketPrice);
                    BigDecimal allocationFactor = deltaA.negate().divide(totalUnderAllocated);
                    allocationConstraint.upper(allocationFactor.multiply(portfolio.getCashBalance(curry)));
                } else { // Stock is correctly allocated
                    allocationConstraint.set(v, pos.marketPrice);
                    BigDecimal targetCash = targetAllocation.multiply(portfolio.getCashBalance(curry));
                    allocationConstraint.upper(pos.marketPrice.max(targetCash));
                }
            }
        }

        // Risoluzione del modello
        Optimisation.Result result = model.maximise();

        System.out.println(model);

        // Output dei risultati
        System.out.println("Ottimizzazione completata");
        if (result.getState().isFeasible()) {
            BigDecimal remainingCashUSD = portfolio.getCashBalance(curry);
            System.out.println("Soluzione trovata:");
            for (Position pos : portfolio.getPositionList()) {
                if (variables.containsKey(pos.contract.symbol())) {
                    int unitsToBuy = variables.get(pos.contract.symbol()).getValue().intValue();
                    if (unitsToBuy > 0) {
                        BigDecimal price = pos.marketPrice.multiply(Constants.PRCTG_LIMIT_PRICE).setScale(2, RoundingMode.HALF_UP);
                        trades.add(new TradeInstruction(tradeCounter,pos.contract.symbol(), pos.contract.getSecType(),pos.contract.currency(), price, Decimal.get(unitsToBuy), false));
                        remainingCashUSD = remainingCashUSD.subtract(pos.marketPrice.multiply(new BigDecimal(unitsToBuy))) ;
                        System.out.println("Compra " + unitsToBuy + " unità di " + pos.contract.symbol());

                    }
                }
            }
            System.out.print("Cash Rimanente "+ remainingCashUSD );
        } else {
            System.out.println("Nessuna soluzione fattibile trovata.");
        }

        return trades;
    }
}
