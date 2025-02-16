package ch.ibkrbot;

import com.ib.client.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

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
        this.clientWrapper.addObserver("accountSummary", new Observer() {
            @Override
            public void notify(String notificationName, Object data) {
                Balance b = (Balance) data;
                switch (b.balanceType) {
                    case "TotalCashBalance" -> portfolio.addCashBalance(b.currency, b.balance);
                    case "ExchangeRate" -> portfolio.addExchangeRate(b.currency, b.balance);
                    case "StockMarketValue" -> portfolio.addStockMarketValue(b.currency, b.balance);
                }
            }
        });
        this.clientWrapper.addObserver("accountSummaryEnd", new Observer() {
            @Override
            public void notify(String notificationName, Object data) {
                for (String curry : portfolio.getAllCurry()) {
                    double xrate = portfolio.getExchangeRate(curry);

                    double totalStockMarketValue = portfolio.getStockMarketValue("BASE");
                    portfolio.addTotalStockMarketValue(curry, totalStockMarketValue / xrate);

                    double totalCashBalance = portfolio.getCashBalance("BASE");
                    portfolio.addTotalCashBalance(curry, totalCashBalance / xrate);
                }
                latch.countDown();
            }
        });
        this.clientWrapper.addObserver("updatePortfolio", new Observer() {
            @Override
            public void notify(String notificationName, Object data) {
                Position p = (Position) data;
                p.targetAllocation = portfolio.getTargetAllocations(p.symbol);
                p.currentAllocation = p.marketValue / portfolio.getTotalStockMarketValue(p.currency);
                portfolio.addPosition(p);
            }
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
        placeDomestiOrder();

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
            return future.get(); // Attende il risultato senza busy-waiting
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

    public void placeDomestiOrder() {
        List<TradeInstruction> trades = this.tradeGenerator.generatedDomesticOrders(portfolio);
        for (TradeInstruction t : trades) {
            int reqId = getReqIdCounter();
            socket.placeOrder(reqId, t.getContract(), t.getOrder());
        }
    }
}
