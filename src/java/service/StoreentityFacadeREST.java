package service;

import Entity.Storeentity;
import databasehelper.StoreDB;
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
    private StoreDB storeDb = new StoreDB();

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

        return storeDb.listAllStores(em);
    }

    //get the item quantity based on the storeID
    //this function is used by ECommerce_StockAvailability servlet
    @GET
    @Path("getQuantity")
    @Produces({"application/json"})
    public Response getItemQuantityOfStore(@QueryParam("storeId") Long storeId, @QueryParam("SKU") String SKU) {
        int qty = storeDb.getItemQuantityOfStore(storeId, SKU);
        if (qty == -1) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(qty + "", MediaType.APPLICATION_JSON).build();
    }

    @PUT
    @Path("createECommerceTransactionRecord")
    @Produces("application/json")
    public Response createECommerceTransactionRecord(@QueryParam("finalPrice") double finalPrice, @QueryParam("memberId") Long memberId,@QueryParam("storeId") Long storeId) {

        int transactionRecordId = storeDb.createECommerceTransactionRecord(finalPrice, memberId,storeId);
        if (transactionRecordId == -1) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        return Response
                .status(200)
                .entity(transactionRecordId + "")
                .build();
    }

    @PUT
    @Path("createECommerceLineItemRecord")
    @Consumes("application/json")
    @Produces("application/json")
    public Response createECommerceLineItemRecord(@QueryParam("quantity") int quantity, @QueryParam("itemId") String itemId, @QueryParam("SKU") String SKU, @QueryParam("transactionRecordId") int transactionRecordId,@QueryParam("storeId") Long storeId) {

        int result = storeDb.createECommerceLineItemRecord(quantity, itemId, SKU, transactionRecordId,storeId);
        if (result == 0) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }

        return Response
                .status(200)
                .build();
    }

    @GET
    @Path("getStoreNameOrAddress")
    @Produces({"application/json"})
    public Response getStoreNameOrAddress(@QueryParam("storeId") Long storeId,@QueryParam ("type") String type) {

        String typeReturn = storeDb.getStoreNameOrAddress(storeId,type);
        if (typeReturn.equals("")) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.OK).entity(typeReturn).build();
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
