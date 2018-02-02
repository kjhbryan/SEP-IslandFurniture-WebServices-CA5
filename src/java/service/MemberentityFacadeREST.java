package service;

import Entity.Itementity;
import Entity.Lineitementity;
import Entity.Member;
import Entity.Memberentity;
import Entity.Qrphonesyncentity;
import Entity.SalesRecordItem;
import Entity.ShoppingCartLineItem;
import Entity.Storeentity;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Stateless
@Path("entity.memberentity")
public class MemberentityFacadeREST extends AbstractFacade<Memberentity> {

    @PersistenceContext(unitName = "WebService")
    private EntityManager em;

    public MemberentityFacadeREST() {
        super(Memberentity.class);
    }

    @POST
    @Override
    @Consumes({"application/xml", "application/json"})
    public void create(Memberentity entity) {
        super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({"application/xml", "application/json"})
    public void edit(@PathParam("id") Long id, Memberentity entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Long id) {
        super.remove(super.find(id));
    }

    @GET
    @Path("members")
    @Produces({"application/json"})
    public List<Memberentity> listAllMembers() {
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

    //this function is used by ECommerce_MemberLoginServlet
    @GET
    @Path("login")
    @Produces("application/json")
    public Response loginMember(@QueryParam("email") String email, @QueryParam("password") String password) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345");
            String stmt = "SELECT * FROM memberentity m WHERE m.EMAIL=?";
            PreparedStatement ps = conn.prepareStatement(stmt);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            rs.next();
            String passwordSalt = rs.getString("PASSWORDSALT");
            String passwordHash = generatePasswordHash(passwordSalt, password);
            if (passwordHash.equals(rs.getString("PASSWORDHASH"))) {
                return Response.ok(email, MediaType.APPLICATION_JSON).build();
            } else {
                System.out.println("Login credentials provided were incorrect, password wrong.");
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
    
    @GET
    @Path("getMember")
    @Produces({"application/json"})
    public Response getMember(@QueryParam("email") String email)
    {
        try{
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/islandfurniture-it07??zeroDateTimeBehavior=convertToNull&user=root&password=12345");
            String stmt =  "SELECT * FROM memberentity m WHERE m.EMAIL=?";
            PreparedStatement ps = conn.prepareStatement(stmt);
            ps.setString(1,email);
            ResultSet rs = ps.executeQuery();
            
            if(rs.next()){
                Member m = new Member();
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
                return Response.status(200).entity(m).build();
            }
            else
            {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
    
    @POST
    @Path("memberEditProfile")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response editMember(@QueryParam("name") String name, @QueryParam("phone") String phone,@QueryParam("country") String country,
            @QueryParam("address") String address,@QueryParam("securityQuestion") int securityQuestion,@QueryParam("securityAnswer") String securityAnswer,
            @QueryParam("age") int age,@QueryParam("income") int income,@QueryParam("email") String email, @QueryParam("password") String password)
    {
        try{
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/islandfurniture-it07??zeroDateTimeBehavior=convertToNull&user=root&password=12345");
            String stmt =  "SELECT * FROM memberentity m WHERE m.EMAIL=?";
            PreparedStatement ps = conn.prepareStatement(stmt);
            ps.setString(1,email);
            ResultSet rs = ps.executeQuery();
            String passwordSalt = "";
            String passwordHash = "";
            if(rs.next()){
                passwordSalt = rs.getString("PASSWORDSALT");
                passwordHash = generatePasswordHash(passwordSalt, password);
            }
            if(!password.equals("")){
                stmt = "UPDATE memberentity m SET m.NAME=?,m.PHONE=?,m.CITY=?,m.ADDRESS=?,m.SECURITYQUESTION=?,m.SECURITYANSWER=?,m.AGE=?,m.INCOME=?,m.PASSWORDHASH=? WHERE m.EMAIL=?";
            }
            else
            {
                 stmt = "UPDATE memberentity m SET m.NAME=?,m.PHONE=?,m.CITY=?,m.ADDRESS=?,m.SECURITYQUESTION=?,m.SECURITYANSWER=?,m.AGE=?,m.INCOME=? WHERE m.EMAIL=?";
            
            }
            ps = conn.prepareStatement(stmt);
            ps.setString(1,name);
            ps.setString(2, phone);
            ps.setString(3, country);
            ps.setString(4, address);
            ps.setInt(5, securityQuestion);
            ps.setString(6, securityAnswer);
            ps.setInt(7, age);
            ps.setInt(8, income);
            if(!password.equals(""))
            {
                ps.setString(9, passwordHash);
                ps.setString(10,email);
            }
            else
            {
                ps.setString(9,email);
            }
            int result = ps.executeUpdate();
            if(result >0)
            {
                return Response.status(200).build();
            }
            else
            {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).build();
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
    
    @GET
    @Path("uploadShoppingList")
    @Produces({"application/json"})
    public String uploadShoppingList(@QueryParam("email") String email, @QueryParam("shoppingList") String shoppingList) {
        System.out.println("webservice: uploadShoppingList called");
        System.out.println(shoppingList);
        try {
            Query q = em.createQuery("select m from Memberentity m where m.email=:email and m.isdeleted=false");
            q.setParameter("email", email);
            Memberentity m = (Memberentity) q.getSingleResult();
            List<Lineitementity> list = m.getLineitementityList();
            if (!list.isEmpty()) {
                for (Lineitementity lineItem : list) {
                    em.refresh(lineItem);
                    em.flush();
                    em.remove(lineItem);
                }
            }
            m.setLineitementityList(new ArrayList<Lineitementity>());
            em.flush();

            Scanner sc = new Scanner(shoppingList);
            sc.useDelimiter(",");
            while (sc.hasNext()) {
                String SKU = sc.next();
                Integer quantity = Integer.parseInt(sc.next());
                if (quantity != 0) {
                    q = em.createQuery("select i from Itementity i where i.sku=:SKU and i.isdeleted=false");
                    q.setParameter("SKU", SKU);
                    Itementity item = (Itementity) q.getSingleResult();

                    Lineitementity lineItem = new Lineitementity();

                    lineItem.setItemId(item);
                    lineItem.setQuantity(quantity);
                    System.out.println("Item: " + item.getSku());
                    System.out.println("Quantity: " + quantity);
                    m.getLineitementityList().add(lineItem);
                }
            }
            return "success";
            //return s;
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    @GET
    @Path("syncWithPOS")
    @Produces({"application/json"})
    public String tieMemberToSyncRequest(@QueryParam("email") String email, @QueryParam("qrCode") String qrCode) {
        System.out.println("tieMemberToSyncRequest() called");
        try {
            Query q = em.createQuery("SELECT p from Qrphonesyncentity p where p.qrcode=:qrCode");
            q.setParameter("qrCode", qrCode);
            Qrphonesyncentity phoneSyncEntity = (Qrphonesyncentity) q.getSingleResult();
            if (phoneSyncEntity == null) {
                return "fail";
            } else {
                phoneSyncEntity.setMemberemail(email);
                em.merge(phoneSyncEntity);
                em.flush();
                return "success";
            }
        } catch (Exception ex) {
            System.out.println("tieMemberToSyncRequest(): Error");
            ex.printStackTrace();
            return "fail";
        }
    }
    
    @GET
    @Path("getSalesHistory")
    @Produces({"application/json"})
    public Response getSalesHistory(@QueryParam("memberId") Long memberId, @QueryParam("countryId") Long countryId) {
        int orderNo = 1;
        Long salesRecordId = (long)0;
        List<SalesRecordItem> salesRecordItems = new ArrayList<>();
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345");
            String stmt = "SELECT * FROM salesrecordentity s,salesrecordentity_lineitementity sl WHERE MEMBER_ID = ? AND s.ID = sl.SalesRecordEntity_ID";
            PreparedStatement ps = conn.prepareStatement(stmt);
            ps.setLong(1, memberId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if(salesRecordId != rs.getLong("ID"))
                {
                   SalesRecordItem s = new SalesRecordItem();
                   s.setId(rs.getLong("ID"));
                   salesRecordId = rs.getLong("ID");
                   s.setOrderNo(orderNo);
                   s.setDatePurchased(rs.getDate("CREATEDDATE"));
                   s.setStoreName(getStoreDetails(rs.getInt("STORE_ID")).getName());
                   s.setStoreAddress(getStoreDetails(rs.getInt("STORE_ID")).getAddress());
                   s.setTotalAmount(rs.getDouble("AMOUNTDUE"));
                   s.setShoppingCartLineItemList(getLineItemList(s.getId(), countryId));
                   salesRecordItems.add(s);
                   orderNo++; 
                }
                 
            }
            GenericEntity<List<SalesRecordItem>> entity = new GenericEntity<List<SalesRecordItem>>(salesRecordItems) {
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
    public Storeentity getStoreDetails(int storeId) {

           Storeentity s = new Storeentity();
           try {
               Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345");
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
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
