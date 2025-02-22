package ch.ibkrbot;

import com.ib.client.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class IBKRClientBot {
    private EClientSocket socket;
    private final EReaderSignal readerSignal;
    private final ClientWrapper clientWrapper;
    private final Portfolio portfolio;
    private final TradeGenerator tradeGenerator;
    private CountDownLatch latch;

    public IBKRClientBot() {
        readerSignal = new EJavaSignal();
        this.portfolio = new Portfolio();
        this.clientWrapper = new ClientWrapper();
        socket = new EClientSocket(clientWrapper, readerSignal);
        this.tradeGenerator = new TradeGenerator();
        this.latch = new CountDownLatch(1);
    }

    public void initObserver() {
        this.clientWrapper.addObserver("accountSummary", (notificationName, data) -> {
            Balance b = (Balance) data;
            BigDecimal balanceValue = BigDecimal.valueOf(b.balance);
            switch (b.balanceType) {
                case "TotalCashBalance" -> portfolio.addCashBalance(b.currency, balanceValue);
                case "ExchangeRate" -> portfolio.addExchangeRate(b.currency, balanceValue);
                case "StockMarketValue" -> portfolio.addStockMarketValue(b.currency, balanceValue);
            }
        });

        this.clientWrapper.addObserver("accountSummaryEnd", (notificationName, data) -> {
            for (String curry : portfolio.getAllCurry()) {
                BigDecimal xrate = portfolio.getExchangeRate(curry);
                BigDecimal totalStockMarketValue = portfolio.getStockMarketValue("BASE");
                portfolio.addTotalStockMarketValue(curry, totalStockMarketValue.divide(xrate, 2, RoundingMode.HALF_UP));

                BigDecimal totalCashBalance = portfolio.getCashBalance("BASE");
                portfolio.addTotalCashBalance(curry, totalCashBalance.divide(xrate, 2, RoundingMode.HALF_UP));
            }
            latch.countDown();
        });

        this.clientWrapper.addObserver("updatePortfolio", (notificationName, data) -> {
            Position p = (Position) data;
            p.targetAllocation = portfolio.getTargetAllocations(p.contract.symbol());

            // Ensure that total stock market value is not zero to avoid division errors
            BigDecimal totalStockMarketValue = portfolio.getTotalStockMarketValue(p.contract.currency());
            if (totalStockMarketValue.compareTo(BigDecimal.ZERO) > 0) {
                p.currentAllocation = p.marketValue.divide(totalStockMarketValue, 4, RoundingMode.HALF_UP);
            } else {
                p.currentAllocation = BigDecimal.ZERO; // Default if total market value is zero
            }

            portfolio.addPosition(p);
        });
    }

    public void start() {
        this.connect();
        this.initObserver();
        this.requestPortfolioComposition();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        placeDomesticOrder();
        convertDomestictCash();
    }

    private int getReqIdCounter() {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        Observer o = new Observer() {
            @Override
            public void notify(String notificationName, Object data) {
                future.complete((Integer) data);
            }
        };

        this.clientWrapper.addObserver("nextValidId", o);
        this.socket.reqIds(0);

        try {
            return future.get(); // Waits for the result without busy-waiting
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            this.clientWrapper.removeObserver("nextValidId", o);
        }
    }

    private void connect() {
        socket.eConnect("127.0.0.1", 4001, 0);

        if (socket.isConnected()) {
            System.out.println("Connected to IBKR API!");

            final EReader reader = new EReader(socket, readerSignal);
            reader.start();
            new Thread(() -> {
                while (socket.isConnected()) {
                    readerSignal.waitForSignal();
                    try {
                        reader.processMsgs();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            System.out.println("Connection Failed");
        }
    }

    public void requestPortfolioComposition() {
        int reqId = getReqIdCounter();

        this.socket.reqAccountSummary(reqId, "All", "$LEDGER:ALL");
        try {
            this.latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.socket.reqAccountUpdates(true, Constants.ACCOUNT);
        this.socket.reqAccountUpdates(false, Constants.ACCOUNT);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(portfolio);
    }

    public void placeDomesticOrder() {
        List<TradeInstruction> trades = this.tradeGenerator.createDomesticOrders(portfolio);
        for (TradeInstruction t : trades) {
            int reqId = getReqIdCounter();
            BigDecimal tick = getTick(t.getContract(), BigDecimal.valueOf(t.getOrder().lmtPrice()));

            t.setTick(tick);

            BigDecimal lmtPrice = BigDecimal.valueOf(t.getOrder().lmtPrice());
            BigDecimal quantity = BigDecimal.valueOf(t.getOrder().totalQuantity().longValue());
            BigDecimal spentAmount = lmtPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);

            System.out.println("Placing order " + t.getContract().symbol() +
                    " Quantity: " + t.getOrder().totalQuantity() +
                    " LMT Price: " + lmtPrice);
           // this.socket.placeOrder(reqId, t.getContract(), t.getOrder());
            this.portfolio.reduceCashPosition(spentAmount, t.getContract().currency());

            System.out.println("Total Spent amount " + spentAmount);
        }
        System.out.println("Remaining CHF " + this.portfolio.getCashBalance("CHF"));
    }

    public void convertDomestictCash() {
        BigDecimal remainingChf = portfolio.getCashBalance(Constants.BASE_CURRY);

        if (remainingChf.compareTo(BigDecimal.ZERO) > 0) {
            int reqId = getReqIdCounter();
            TradeInstruction t = this.tradeGenerator.createForexOrder("CHF", "USD", BigDecimal.valueOf(99.1));

            System.out.println("Placing forex order From: " + t.getContract().symbol() +
                    " To: " + t.getContract().currency() +
                    " Quantity: " + t.getOrder().totalQuantity() +
                    " LMT Price: " + t.getOrder().lmtPrice() +
                    " Is Market Order: " + t.isMarketOrder());

            this.socket.placeOrder(reqId, t.getContract(), t.getOrder());
        } else {
            System.out.println("No CHF balance available for conversion.");
        }
    }

    private ContractDetails getContractDetails(Contract contract) {
        CompletableFuture<ContractDetails> future = new CompletableFuture<>();

        Observer o = new Observer() {
            @Override
            public void notify(String notificationName, Object data) {
                future.complete((ContractDetails) data);
            }
        };
        this.clientWrapper.addObserver("contractDetails", o);
        int reqId = getReqIdCounter();
        this.socket.reqContractDetails(reqId, contract);

        try {
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            this.clientWrapper.removeObserver("contractDetails", o);
        }
    }

    public BigDecimal getTick(Contract contract, BigDecimal currentPrice) {
        BigDecimal tick = BigDecimal.valueOf(0.05); // Default tick
        ContractDetails contractDetails = getContractDetails(contract);
        String marketRuleIds = contractDetails.marketRuleIds();
        List<Integer> marketRuleIdsList = Util.parseMarketRuleIds(marketRuleIds);

        CompletableFuture<PriceIncrement[]> future = new CompletableFuture<>();

        Observer o = new Observer() {
            @Override
            public void notify(String notificationName, Object data) {
                future.complete((PriceIncrement[]) data);
            }
        };
        this.clientWrapper.addObserver("marketRule", o);

        for (Integer i : marketRuleIdsList) {
            this.socket.reqMarketRule(i);
        }

        try {
            PriceIncrement[] priceIncrements = future.get();
            for (PriceIncrement p : priceIncrements) {
                if (BigDecimal.valueOf(p.lowEdge()).compareTo(currentPrice) > 0)
                    break;
                tick = BigDecimal.valueOf(p.increment());
            }
            return tick;
        } catch (Exception e) {
            System.err.println(e);
            return tick;
        } finally {
            this.clientWrapper.removeObserver("marketRule", o);
        }
    }

}
