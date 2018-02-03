/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package databasehelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author Bryan K
 */
public class CountryDB {
    private static String connection = "jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345";
    public CountryDB()
    {
    }
    
    public int getQuantity(Long countryID, String SKU)
    {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345");
            String stmt = "SELECT sum(l.QUANTITY) as sum FROM storeentity s, warehouseentity w, storagebinentity sb, storagebinentity_lineitementity sbli, lineitementity l, itementity i where s.WAREHOUSE_ID=w.ID and w.ID=sb.WAREHOUSE_ID and sb.ID=sbli.StorageBinEntity_ID and sbli.lineItems_ID=l.ID and l.ITEM_ID=i.ID and s.COUNTRY_ID=? and i.SKU=?";
            PreparedStatement ps = conn.prepareStatement(stmt);
            ps.setLong(1, countryID);
            ps.setString(2, SKU);
            ResultSet rs = ps.executeQuery();
            int qty = 0;
            if (rs.next()) {
                qty = rs.getInt("sum");
            }

            return qty;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }
}
