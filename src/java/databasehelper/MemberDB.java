/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package databasehelper;

import Entity.Member;
import Entity.Memberentity;
import Entity.SalesRecordItem;
import Entity.ShoppingCartLineItem;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author Bryan K
 */
public class MemberDB {
    private static String connection = "jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345";
    public MemberDB()
    {
        
    }
    
    public List<Memberentity> listAllMembers(EntityManager em) {
        Query q = em.createQuery("Select s from Memberentity s where s.isdeleted=FALSE");
        List<Memberentity> list = q.getResultList();
        for (Memberentity m : list) {
            em.detach(m);
            m.setCountryId(null);
            m.setLoyaltytierId(null);
            m.setLineitementityList(null);
            m.setWishlistId(null);
        }
        List<Memberentity> list2 = new ArrayList();
        list2.add(list.get(0));
        return list;
    }
    
    
    public String loginMember(String email,  String password) {
        String result = "";
        try {
            Connection conn = DriverManager.getConnection(connection);
            String stmt = "SELECT * FROM memberentity m WHERE m.EMAIL=?";
            PreparedStatement ps = conn.prepareStatement(stmt);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            rs.next();
            String passwordSalt = rs.getString("PASSWORDSALT");
            String passwordHash = generatePasswordHash(passwordSalt, password);
            if (passwordHash.equals(rs.getString("PASSWORDHASH"))) {
                return email;
            } else {
                System.out.println("Login credentials provided were incorrect, password wrong.");
                return result;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }
    
    public Member getMember(String email) {
        Member m = null;
        try {
            Connection conn = DriverManager.getConnection(connection);
            String stmt = "SELECT * FROM memberentity m WHERE m.EMAIL=?";
            PreparedStatement ps = conn.prepareStatement(stmt);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                m = new Member();
                m.setId(rs.getLong("ID"));
                m.setName(rs.getString("NAME"));
                m.setEmail(rs.getString("EMAIL"));
                m.setPhone(rs.getString("PHONE"));
                m.setCity(rs.getString("CITY"));
                m.setAddress(rs.getString("ADDRESS"));
                m.setSecurityQuestion(rs.getInt("SECURITYQUESTION"));
                m.setSecurityAnswer(rs.getString("SECURITYANSWER"));
                m.setAge(rs.getInt("AGE"));
                m.setIncome(rs.getInt("INCOME"));
                return m;
            } else {
                return m;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    
    
    public int editMember(String name, String phone,  String country,String address, int securityQuestion,  String securityAnswer,int age, int income, String email,  String password) {
        int result = 0;
        try {
            Connection conn = DriverManager.getConnection(connection);
            String stmt = "SELECT * FROM memberentity m WHERE m.EMAIL=?";
            PreparedStatement ps = conn.prepareStatement(stmt);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            String passwordSalt = "";
            String passwordHash = "";
            if (rs.next()) {
                passwordSalt = rs.getString("PASSWORDSALT");
                passwordHash = generatePasswordHash(passwordSalt, password);
            }
            if (!password.equals("")) {
                stmt = "UPDATE memberentity m SET m.NAME=?,m.PHONE=?,m.CITY=?,m.ADDRESS=?,m.SECURITYQUESTION=?,m.SECURITYANSWER=?,m.AGE=?,m.INCOME=?,m.PASSWORDHASH=? WHERE m.EMAIL=?";
            } else {
                stmt = "UPDATE memberentity m SET m.NAME=?,m.PHONE=?,m.CITY=?,m.ADDRESS=?,m.SECURITYQUESTION=?,m.SECURITYANSWER=?,m.AGE=?,m.INCOME=? WHERE m.EMAIL=?";

            }
            ps = conn.prepareStatement(stmt);
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, country);
            ps.setString(4, address);
            ps.setInt(5, securityQuestion);
            ps.setString(6, securityAnswer);
            ps.setInt(7, age);
            ps.setInt(8, income);
            if (!password.equals("")) {
                ps.setString(9, passwordHash);
                ps.setString(10, email);
            } else {
                ps.setString(9, email);
            }
            result = ps.executeUpdate();
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }
    
    public List<SalesRecordItem> getSalesHistory( Long memberId, Long countryId) {
        StoreDB storeDb = new StoreDB();
        int orderNo = 1;
        Long salesRecordId = (long) 0;
        List<SalesRecordItem> salesRecordItems = null;
        try {
            salesRecordItems = new ArrayList<>();
            Connection conn = DriverManager.getConnection(connection);
            String stmt = "SELECT * FROM salesrecordentity s,salesrecordentity_lineitementity sl WHERE MEMBER_ID = ? AND s.ID = sl.SalesRecordEntity_ID";
            PreparedStatement ps = conn.prepareStatement(stmt);
            ps.setLong(1, memberId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (salesRecordId != rs.getLong("ID")) {
                    SalesRecordItem s = new SalesRecordItem();
                    s.setId(rs.getLong("ID"));
                    salesRecordId = rs.getLong("ID");
                    s.setOrderNo(orderNo);
                    s.setDatePurchased(rs.getDate("CREATEDDATE"));
                    s.setStoreName(storeDb.getStoreDetail(rs.getLong("STORE_ID")).getName());
                    s.setStoreAddress(storeDb.getStoreDetail(rs.getLong("STORE_ID")).getAddress());
                    s.setTotalAmount(rs.getDouble("AMOUNTDUE"));
                    s.setShoppingCartLineItemList(getLineItemList(s.getId(), countryId));
                    salesRecordItems.add(s);
                    orderNo++;
                }
            }
            return salesRecordItems;
        } catch (Exception ex) {

            ex.printStackTrace();
            return null;
        }

    }
    
    public List<ShoppingCartLineItem> getLineItemList(Long salesRecordId, Long countryId) {
        
        List<ShoppingCartLineItem> shoppingCartLineItems = new ArrayList<>();
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345");
            String stmt = "SELECT * FROM salesrecordentity_lineitementity sl,lineitementity l,itementity i,furnitureentity f,item_countryentity ic WHERE sl.SalesRecordEntity_ID = ? AND sl.itemsPurchased_ID = l.ID AND i.ID = l.ITEM_ID AND i.ID = f.ID AND ic.ITEM_ID = i.ID AND ic.COUNTRY_ID = ?";
            PreparedStatement ps = conn.prepareStatement(stmt);
            ps.setLong(1, salesRecordId);
            ps.setLong(2, countryId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ShoppingCartLineItem s = new ShoppingCartLineItem();
                s.setId(rs.getString("ITEM_ID"));
                s.setImageURL(rs.getString("IMAGEURL"));
                s.setSKU(rs.getString("SKU"));
                s.setQuantity(rs.getInt("QUANTITY"));
                s.setPrice(rs.getInt("RETAILPRICE"));
                s.setName(rs.getString("NAME"));
                shoppingCartLineItems.add(s);
            }

            return shoppingCartLineItems;
        } catch (Exception ex) {

            ex.printStackTrace();
            return null;
        }

    }
    
    public String generatePasswordSalt() {
        byte[] salt = new byte[16];
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.nextBytes(salt);
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("\nServer failed to generate password salt.\n" + ex);
        }
        return Arrays.toString(salt);
    }
    
    public String generatePasswordHash(String salt, String password) {
        String passwordHash = null;
        try {
            password = salt + password;
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            passwordHash = sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("\nServer failed to hash password.\n" + ex);
        }
        return passwordHash;
    }
}
