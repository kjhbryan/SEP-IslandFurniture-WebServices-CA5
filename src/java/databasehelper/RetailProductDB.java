/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package databasehelper;

import Entity.RetailProduct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;

/**
 *
 * @author Bryan K
 */
public class RetailProductDB {
    private static String connection = "jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345";
    public RetailProductDB()
    {
        
    }
    
    
    public List<RetailProduct> getRetailProductList(@QueryParam("countryID") Long countryID) {
        List<RetailProduct> list = new ArrayList<>();
        try {
            String stmt = "";
            PreparedStatement ps;
            Connection conn = DriverManager.getConnection(connection);

            if (countryID == null) {
                stmt = "SELECT i.ID as id, i.NAME as name, r.IMAGEURL as imageURL, i.SKU as sku, i.DESCRIPTION as description, i.TYPE as type, i._LENGTH as length, i.WIDTH as width, i.HEIGHT as height, i.CATEGORY as category FROM itementity i, retailproductentity r where i.ID=r.ID and i.ISDELETED=FALSE;";
                ps = conn.prepareStatement(stmt);
            } else {
                stmt = "SELECT i.ID as id, i.NAME as name, r.IMAGEURL as imageURL, i.SKU as sku, i.DESCRIPTION as description, i.TYPE as type, i._LENGTH as length, i.WIDTH as width, i.HEIGHT as height, i.CATEGORY as category, ic.RETAILPRICE as price FROM itementity i, retailproductentity r, item_countryentity ic where i.ID=r.ID and i.ID=ic.ITEM_ID and i.ISDELETED=FALSE and ic.COUNTRY_ID=?;";
                ps = conn.prepareStatement(stmt);
                ps.setLong(1, countryID);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                RetailProduct r = new RetailProduct();
                r.setId(rs.getLong("id"));
                r.setName(rs.getString("name"));
                r.setImageUrl(rs.getString("imageURL"));
                r.setSKU(rs.getString("sku"));
                r.setDescription(rs.getString("description"));
                r.setType(rs.getString("type"));
                r.setCategory(rs.getString("category"));
                if (countryID != null) {
                    r.setPrice(rs.getDouble("price"));
                }
                list.add(r);
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
