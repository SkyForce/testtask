package revolut.controller;

import revolut.exceptions.InvalidAmountException;
import revolut.exceptions.NotEnoughMoneyException;
import revolut.exceptions.UserNotFoundException;
import revolut.model.Account;
import revolut.repository.AccountRepository;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;

@Path("/")
public class Controller {

    private AccountRepository repository = AccountRepository.getInstance();

    @POST
    @Path("/create")
    public Response createAccount() {
        Account account = repository.createAccount();
        return Response.ok(account.getId()).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteAccount(@PathParam("id") Integer id) {
        try {
            repository.deleteAccount(id);
        }
        catch (UserNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity("User doesn't exist").build();
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/{id}/{amount}")
    public Response putMoney(@PathParam("id") Integer id, @PathParam("amount") BigDecimal amount) {
        try {
            Account account = repository.getById(id);
            account.putMoney(amount);
        }
        catch (UserNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity("User doesn't exist").build();
        }
        catch (InvalidAmountException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid amount").build();
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/{fromId}/{toId}/{amount}")
    public Response transfer(@PathParam("fromId") Integer fromId, @PathParam("toId") Integer toId, @PathParam("amount") BigDecimal amount) {
        try {
            Account fromAccount = repository.getById(fromId);
            Account toAccount = repository.getById(toId);
            fromAccount.transfer(toAccount, amount);
        }
        catch (UserNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity("User doesn't exist").build();
        }
        catch (NotEnoughMoneyException e) {
            return Response.status(Response.Status.FORBIDDEN).entity("Not enough money to transfer").build();
        }
        catch (InvalidAmountException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid amount").build();
        }
        return Response.ok().build();
    }


    @GET
    @Path("/{id}")
    public Response getAmount(@PathParam("id") Integer id) {
        try {
            Account account = repository.getById(id);
            return Response.ok(account.getAmount()).build();
        }
        catch (UserNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity("User doesn't exist").build();
        }
    }


}
