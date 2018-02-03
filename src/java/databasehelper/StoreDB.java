/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package databasehelper;

import Entity.Storeentity;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author Bryan K
 */
public class StoreDB {
    private EntityManager em;
    private static String connection = "jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345";
    public StoreDB()
    {
        
    }
    public StoreDB(EntityManager em)
    {
        this.em = em;
    }
    
    public List<Storeentity> listAllStores(EntityManager em) {
        Query q = em.createQuery("Select s from Storeentity s where s.isdeleted=FALSE and s.countryId.name='Singapore'");
        List<Storeentity> list = q.getResultList();
        for (Storeentity s : list) {
            em.detach(s);
            s.setCountryId(null);
            s.setRegionalofficeId(null);
            s.setWarehouseId(null);
        }
        List<Storeentity> list2 = new ArrayList();
        list2.add(list.get(0));
        return list;
    }
    
    public int getItemQuantityOfStore( Long storeId, String SKU) {
        int qty = 0;
        try {
            Connection conn = DriverManager.getConnection(connection);
            String stmt = "SELECT sum(l.QUANTITY) as sum FROM storeentity s, warehouseentity w, storagebinentity sb, storagebinentity_lineitementity sbli, lineitementity l, itementity i where s.WAREHOUSE_ID=w.ID and w.ID=sb.WAREHOUSE_ID and sb.ID=sbli.StorageBinEntity_ID and sbli.lineItems_ID=l.ID and l.ITEM_ID=i.ID and s.ID=? and i.SKU=?";
            PreparedStatement ps = conn.prepareStatement(stmt);
            ps.setLong(1, storeId);
            ps.setString(2, SKU);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                qty = rs.getInt("sum");
            }

            return qty;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }
    
    
    
    public int createECommerceTransactionRecord(double finalPrice, Long memberId,Long storeId) {
        
        int transactionRecordId = -1;
        try {
            Connection conn = DriverManager.getConnection(connection);
            
            
            String currency = "SGD";
            String posName = "Counter 1";
            String servedByStaff = "Casher 1";
            Date dt = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long receiptNo = dt.getTime(); 
            String stmt = "INSERT INTO salesrecordentity (AMOUNTDUE,AMOUNTPAID,CREATEDDATE,CURRENCY,POSNAME,RECEIPTNO,SERVEDBYSTAFF,MEMBER_ID,STORE_ID) VALUES (?,?,?,?,?,?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS);
            ps.setDouble(1, finalPrice);
            ps.setDouble(2, finalPrice);
            ps.setString(3, sdf.format(dt));
            ps.setString(4, currency);
            ps.setString(5, posName);
            ps.setString(6, String.valueOf(receiptNo));
            ps.setString(7, servedByStaff);
            ps.setLong(8, memberId);
            ps.setLong(9, storeId);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                transactionRecordId = rs.getInt(1);
            }
            return transactionRecordId;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }
    
     public int createECommerceLineItemRecord(int quantity,  String itemId, String SKU,  int transactionRecordId,Long storeId) {
         int result = 0;
        try {
            Connection conn = DriverManager.getConnection(connection);
            String stmt = "INSERT INTO lineitementity (QUANTITY,ITEM_ID) VALUES (?,?)";
            PreparedStatement ps = conn.prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, quantity);
            ps.setInt(2, Integer.parseInt(itemId));
            result =  ps.executeUpdate();
            
            int lineItemId = 0;
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                lineItemId = rs.getInt(1);
            }

            stmt = "INSERT INTO salesrecordentity_lineitementity (SalesRecordEntity_ID,itemsPurchased_ID) VALUES (?,?)";
            ps = conn.prepareStatement(stmt);
            ps.setInt(1, transactionRecordId);
            ps.setInt(2, lineItemId);
            result =  ps.executeUpdate();

            List<Integer> getQuantityDetails = getCurrentQuantityFromSKU(SKU);
            int newQuantity = getQuantityDetails.get(0) - quantity;

            stmt = "UPDATE  storeentity s, warehouseentity w, storagebinentity sb, storagebinentity_lineitementity sbli, lineitementity l, itementity i SET l.QUANTITY = ?  where s.WAREHOUSE_ID=w.ID and w.ID=sb.WAREHOUSE_ID and sb.ID=sbli.StorageBinEntity_ID and sbli.lineItems_ID=l.ID and l.ITEM_ID=i.ID and s.ID=? and i.SKU=? AND sbli.lineItems_ID = ?;";
            ps = conn.prepareStatement(stmt);
            ps.setInt(1, newQuantity);
            ps.setLong(2, storeId);
            ps.setString(3, SKU);
            ps.setInt(4, getQuantityDetails.get(1));
            result = ps.executeUpdate();
            
            return result;

        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }
     
     public List<Integer> getCurrentQuantityFromSKU(String SKU) {
        List<Integer> ints = new ArrayList<>();
        try {
            Connection conn = DriverManager.getConnection(connection);
            String stmt = "SELECT QUANTITY,lineItems_Id FROM storeentity s, warehouseentity w, storagebinentity sb, storagebinentity_lineitementity sbli, lineitementity l, itementity i where s.WAREHOUSE_ID=w.ID and w.ID=sb.WAREHOUSE_ID and sb.ID=sbli.StorageBinEntity_ID and sbli.lineItems_ID=l.ID and l.ITEM_ID=i.ID and s.ID=? and i.SKU=?";
            PreparedStatement ps = conn.prepareStatement(stmt);
            ps.setLong(1, 59);
            ps.setString(2, SKU);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int quantity = rs.getInt("QUANTITY");
                int lineItemsId = rs.getInt("lineItems_Id");
                ints.add(quantity);
                ints.add(lineItemsId);
            }
            return ints;
        } catch (Exception ex) {

            ex.printStackTrace();
            return null;
        }

    }
     
     
     public Storeentity getStoreDetail(Long storeId) {

        Storeentity s = new Storeentity();
        try {
            Connection conn = DriverManager.getConnection(connection);
            String stmt = "SELECT * FROM storeentity s where s.ID = ?";
            PreparedStatement ps = conn.prepareStatement(stmt);
            ps.setLong(1, storeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                s = new Storeentity();
                s.setId(rs.getLong("ID"));
                s.setAddress(rs.getString("ADDRESS"));
                s.setEmail(rs.getString("EMAIL"));
                s.setName(rs.getString("NAME"));
                s.setPostalcode(rs.getString("POSTALCODE"));
                s.setTelephone(rs.getString("TELEPHONE"));

            }
            return s;
        } catch (Exception ex) {

            ex.printStackTrace();
            return null;
        }
    }
     
     public String getStoreNameOrAddress(Long storeId, String type)
     {
         String typeReturn = "";
        try {
            Connection conn = DriverManager.getConnection(connection);
            String stmt = "SELECT * FROM storeentity s where s.ID = ?";
            PreparedStatement ps = conn.prepareStatement(stmt);
            ps.setLong(1, storeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if(type.equals("name"))
                {
                    typeReturn = rs.getString("NAME");
                }
                else if (type.equals("address"))
                {
                    typeReturn = rs.getString("ADDRESS");
                }
            }
            return typeReturn;
        } catch (Exception ex) {

            ex.printStackTrace();
            return "";

        }
     }
     
     
    
}
