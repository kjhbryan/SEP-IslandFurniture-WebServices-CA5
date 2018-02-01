package service;

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
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Stateless
@Path("entity.storeentity")
public class StoreentityFacadeREST extends AbstractFacade<Storeentity> {

    @PersistenceContext(unitName = "WebService")
    private EntityManager em;

    public StoreentityFacadeREST() {
        super(Storeentity.class);
    }

    @POST
    @Override
    @Consumes({"application/xml", "application/json"})
    public void create(Storeentity entity) {
        super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({"application/xml", "application/json"})
    public void edit(@PathParam("id") Long id, Storeentity entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Long id) {
        super.remove(super.find(id));
    }

    @GET
    @Path("{id}")
    @Produces({"application/xml", "application/json"})
    public Storeentity find(@PathParam("id") Long id) {
        return super.find(id);
    }

    @GET
    @Path("stores")
    @Produces({"application/json"})
    public List<Storeentity> listAllStores() {
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

    //get the item quantity based on the storeID
    //this function is used by ECommerce_StockAvailability servlet
    @GET
    @Path("getQuantity")
    @Produces({"application/json"})
    public Response getItemQuantityOfStore(@QueryParam("storeID") Long storeID, @QueryParam("SKU") String SKU) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345");
            String stmt = "SELECT sum(l.QUANTITY) as sum FROM storeentity s, warehouseentity w, storagebinentity sb, storagebinentity_lineitementity sbli, lineitementity l, itementity i where s.WAREHOUSE_ID=w.ID and w.ID=sb.WAREHOUSE_ID and sb.ID=sbli.StorageBinEntity_ID and sbli.lineItems_ID=l.ID and l.ITEM_ID=i.ID and s.ID=? and i.SKU=?";
            PreparedStatement ps = conn.prepareStatement(stmt);
            ps.setLong(1, storeID);
            ps.setString(2, SKU);
            ResultSet rs = ps.executeQuery();
            int qty = 0;
            if (rs.next()) {
                qty = rs.getInt("sum");
            }

            return Response.ok(qty + "", MediaType.APPLICATION_JSON).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    
    @PUT
    @Path("createECommerceTransactionRecord")
    @Produces("application/json")
    public Response createECommerceTransactionRecord(@QueryParam("finalPrice") double finalPrice, @QueryParam("memberId") Long memberId) {
        try {
              
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345");
            
            Long storeId = (long)59;
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

            int transactionRecordId = 0;
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                transactionRecordId = rs.getInt(1);
            }
            return Response
                    .status(200)
                    .entity(transactionRecordId+"")
                    .build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
    }
    
    @PUT
    @Path("createECommerceLineItemRecord")
    @Consumes("application/json")
    @Produces("application/json")
    public Response createECommerceLineItemRecord(@QueryParam("quantity") int quantity, @QueryParam("itemId") String itemId, @QueryParam("SKU") String SKU, @QueryParam("transactionRecordId") int transactionRecordId) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345");
            String stmt = "INSERT INTO lineitementity (QUANTITY,ITEM_ID) VALUES (?,?)";
            PreparedStatement ps = conn.prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, quantity);
            ps.setInt(2, Integer.parseInt(itemId));
            ps.executeUpdate();

            int lineItemId = 0;
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                lineItemId = rs.getInt(1);
            }

            stmt = "INSERT INTO salesrecordentity_lineitementity (SalesRecordEntity_ID,itemsPurchased_ID) VALUES (?,?)";
            ps = conn.prepareStatement(stmt);
            ps.setInt(1, transactionRecordId);
            ps.setInt(2, lineItemId);
            ps.executeUpdate();

            List<Integer> getQuantityDetails = getCurrentQuantityFromSKU(SKU);
            int newQuantity = getQuantityDetails.get(0) - quantity;

            stmt = "UPDATE  storeentity s, warehouseentity w, storagebinentity sb, storagebinentity_lineitementity sbli, lineitementity l, itementity i SET l.QUANTITY = ?  where s.WAREHOUSE_ID=w.ID and w.ID=sb.WAREHOUSE_ID and sb.ID=sbli.StorageBinEntity_ID and sbli.lineItems_ID=l.ID and l.ITEM_ID=i.ID and s.ID=? and i.SKU=? AND sbli.lineItems_ID = ?;";
            ps = conn.prepareStatement(stmt);
            ps.setInt(1, newQuantity);
            ps.setInt(2, 59);
            ps.setString(3, SKU);
            ps.setInt(4, getQuantityDetails.get(1));
            ps.executeUpdate();

            return Response
                    .status(200)
                    .build();

        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
    }
    
    
    public List<Integer> getCurrentQuantityFromSKU(String SKU) {
        List<Integer> ints = new ArrayList<>();
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345");
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
    
    @GET
    @Path("getStoreDetail")
    @Produces({"application/json"})
    public Response getStoreDetail(@QueryParam("storeId") Long storeId,@QueryParam("type") String type) {

        String typeReturn = "";
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345");
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
            return Response.status(Response.Status.OK).entity(typeReturn).build();
        } catch (Exception ex) {

            ex.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).build();

        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
