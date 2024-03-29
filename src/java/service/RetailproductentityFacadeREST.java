package service;

import Entity.RetailProduct;
import Entity.Retailproductentity;
import databasehelper.RetailProductDB;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

@Stateless
@Path("entity.retailproductentity")
public class RetailproductentityFacadeREST extends AbstractFacade<Retailproductentity> {

    @PersistenceContext(unitName = "WebService")
    private EntityManager em;
    private RetailProductDB retailProductDb = new RetailProductDB();

    public RetailproductentityFacadeREST() {
        super(Retailproductentity.class);
    }

    @POST
    @Override
    @Consumes({"application/xml", "application/json"})
    public void create(Retailproductentity entity) {
        super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({"application/xml", "application/json"})
    public void edit(@PathParam("id") Long id, Retailproductentity entity) {
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
    public Retailproductentity find(@PathParam("id") Long id) {
        return super.find(id);
    }

    @GET
    @Override
    @Produces({"application/xml", "application/json"})
    public List<Retailproductentity> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({"application/xml", "application/json"})
    public List<Retailproductentity> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces("text/plain")
    public String countREST() {
        return String.valueOf(super.count());
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    @GET
    @Path("getRetailProductList")
    @Produces("application/json")
    public Response getRetailProductList(@QueryParam("countryID") Long countryID) {
            List<RetailProduct> list = new ArrayList<>();
            list = retailProductDb.getRetailProductList(countryID);
            if(list == null)
            {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            GenericEntity<List<RetailProduct>> entity = new GenericEntity<List<RetailProduct>>(list) {
            };
            return Response
                    .status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
                    .header("Access-Control-Allow-Credentials", "true")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                    .header("Access-Control-Max-Age", "1209600")
                    .entity(entity)
                    .build();
    }
}
