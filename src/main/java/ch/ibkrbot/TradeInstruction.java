package ch.ibkrbot;

import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.ib.client.Order;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TradeInstruction {

    private Contract contract;
    private Order order;
    private BigDecimal tick;
    private boolean isMarketOrder;

    /**
     * Constructor for stocks and forex trades.
     * @param orderId Order ID
     * @param symbol Stock symbol or base currency for forex (e.g., "CHF" for CHF/USD)
     * @param securityType "STK" for stocks, "CASH" for forex
     * @param currency Currency (e.g., "USD" for forex pairs like CHF/USD)
     * @param price Limit price for stocks or exchange rate for forex
     * @param units Quantity of stocks or amount of base currency to trade
     * @param isMarketOrder True for market order, false for limit order (only applicable to forex)
     */
    public TradeInstruction(int orderId, String symbol, String securityType, String currency, BigDecimal price, Decimal units, boolean isMarketOrder) {
        Contract contract = new Contract();
        contract.symbol(symbol);
        contract.secType(securityType);
        contract.currency(currency);

        // Set exchange based on security type
        if (securityType.equals("STK")) {
            contract.exchange("SMART"); // Stock trading exchange
        } else if (securityType.equals("CASH")) {
            contract.exchange("IDEALPRO"); // Forex trading exchange
        }

        this.contract = contract;

        Order order = new Order();
        order.orderId(orderId);
        order.action("BUY"); // Default action (can be modified later)
        order.totalQuantity(units);
        order.account(Constants.ACCOUNT);

        // Define order type based on security type
        if (securityType.equals("STK")) {
            order.orderType("LMT"); // Limit order for stocks
            order.lmtPrice(price.doubleValue()); // Convert BigDecimal to double for IBKR API
        } else if (securityType.equals("CASH")) {
            if (isMarketOrder) {
                order.orderType("MKT"); // Market order for forex
            } else {
                order.orderType("LMT"); // Limit order for forex
                order.lmtPrice(price.doubleValue());
            }
        }


        this.order = order;
        this.tick = Constants.TICK_SIZE; // Default tick size
        this.isMarketOrder = isMarketOrder;
    }

    /**
     * Constructor for creating a TradeInstruction from an existing contract and order.
     */
    public TradeInstruction(Contract contract, Order order) {
        this.contract = contract;
        this.order = order;
        this.tick = Constants.TICK_SIZE; // Default tick size
    }

    public Contract getContract() {
        return this.contract;
    }

    public Order getOrder() {
        return order;
    }

    public void setTick(BigDecimal tick) {
        if (tick == null || tick.compareTo(BigDecimal.ZERO) <= 0) {
            this.tick = Constants.TICK_SIZE;
        } else {
            this.tick = tick;
        }

        BigDecimal oldLmtPrice = BigDecimal.valueOf(this.order.lmtPrice());

        // Round down to the nearest valid tick size
        BigDecimal lmtPrice = oldLmtPrice.divide(this.tick, 0, RoundingMode.FLOOR)
                .multiply(this.tick)
                .setScale(4, RoundingMode.HALF_UP);

        this.order.lmtPrice(lmtPrice.doubleValue()); // Convert back to double for IBKR API
    }

    public boolean isMarketOrder() {
        return this.isMarketOrder;
    }
}
