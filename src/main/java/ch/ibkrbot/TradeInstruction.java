package ch.ibkrbot;

import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.ib.client.Order;

public class TradeInstruction {

    private Contract contract;
    private Order order;

    public TradeInstruction(int orderId, String symbol, String curry, double price, int units){
        Contract contract = new Contract();
        contract.symbol(symbol);
        contract.secType("STK");
        contract.exchange("SMART");
        contract.currency(curry);

        this.contract = contract;

        Order order = new Order();
        order.orderId(orderId);
        order.action("BUY");
        order.orderType("LMT");
        order.tif("GTC");
        order.lmtPrice(price);
        order.totalQuantity(Decimal.get(units));
        order.account(Constants.ACCOUNT);

        this.order = order;
    }

    public TradeInstruction(Contract contract, Order order){
        this.contract = contract;
        this.order = order;
    }


    public Contract getContract() {
        return this.contract;
    }

    public Order getOrder() {
        return order;
    }

}
