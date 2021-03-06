package demo.order;

import demo.address.Address;
import demo.address.AddressType;
import demo.domain.BaseEntity;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple domain class for the {@link Order} concept in the order context.
 *
 * @author Kenny Bastani
 * @author Josh Long
 */
@Document
public class Order extends BaseEntity {

    @Id
    private ObjectId orderId;
    private String accountNumber;
    @Transient
    private OrderStatus orderStatus;
    private List<LineItem> lineItems = new ArrayList<>();
    private Address shippingAddress;

    public Order() {
    }

    public Order(String accountNumber, Address shippingAddress) {
        this.accountNumber = accountNumber;
        this.shippingAddress = shippingAddress;
        this.shippingAddress.setAddressType(AddressType.SHIPPING);
        this.orderStatus = OrderStatus.PURCHASED;
    }

    public String getOrderId() {
        return orderId != null ? orderId.toHexString() : null;
    }

    public void setOrderId(String id) {
        this.orderId = orderId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public List<LineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public void addLineItem(LineItem lineItem) {
        lineItems.add(lineItem);
    }

    public Order incorporate(OrderEvent orderEvent) {

        if(orderStatus == null) {
            orderStatus = OrderStatus.PURCHASED;
        }

        switch (orderStatus) {
            case PURCHASED:
                if (orderEvent.getType() == OrderEventType.CREATED)
                    orderStatus = OrderStatus.PENDING;
                break;
            case PENDING:
                if (orderEvent.getType() == OrderEventType.ORDERED) {
                    orderStatus = OrderStatus.CONFIRMED;
                } else if (orderEvent.getType() == OrderEventType.CREATED) {
                    orderStatus = OrderStatus.PURCHASED;
                }
                break;
            case CONFIRMED:
                if (orderEvent.getType() == OrderEventType.SHIPPED) {
                    orderStatus = OrderStatus.SHIPPED;
                } else if (orderEvent.getType() == OrderEventType.RESERVED) {
                    orderStatus = OrderStatus.PENDING;
                }
                break;
            case SHIPPED:
                if (orderEvent.getType() == OrderEventType.DELIVERED) {
                    orderStatus = OrderStatus.DELIVERED;
                } else if (orderEvent.getType() == OrderEventType.ORDERED) {
                    orderStatus = OrderStatus.CONFIRMED;
                }
                break;
        }

        return this;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", orderStatus=" + orderStatus +
                ", lineItems=" + lineItems +
                ", shippingAddress=" + shippingAddress +
                "} " + super.toString();
    }
}
