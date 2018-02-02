/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entity;

import java.util.Date;
import java.util.List;
/**
 *
 * @author Bryan K
 */
public class SalesRecordItem {
    private long id;
    private long orderNo;
    private Date datePurchased;
    private String storeName;
    private String storeAddress;
    private double totalAmount;
    private List<ShoppingCartLineItem> shoppingCartLineItemList;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(long orderNo) {
        this.orderNo = orderNo;
    }

    public Date getDatePurchased() {
        return datePurchased;
    }

    public void setDatePurchased(Date datePurchased) {
        this.datePurchased = datePurchased;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }
     public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<ShoppingCartLineItem> getShoppingCartLineItemList() {
        return shoppingCartLineItemList;
    }

    public void setShoppingCartLineItemList(List<ShoppingCartLineItem> shoppingCartLineItemList) {
        this.shoppingCartLineItemList = shoppingCartLineItemList;
    }
    
    
}
