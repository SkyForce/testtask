package revolut;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Test;
import revolut.controller.Controller;
import revolut.repository.AccountRepository;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;


public class ControllerTest extends JerseyTest {
    @Override
    protected Application configure() {
        return new ResourceConfig(Controller.class);
    }

    @After
    public void clear() {
        AccountRepository.getInstance().clear();
    }

    @Test
    public void sequenceCreateReturnsIncreasingIds() {
        Response response = target("/create").request()
                .post(null);

        assertEquals("Http Response should be 200: ", Response.Status.OK.getStatusCode(), response.getStatus());

        long content = response.readEntity(Integer.class);
        assertEquals("Content of ressponse is: ", 1, content);

        response = target("/create").request()
                .post(null);

        content = response.readEntity(Integer.class);
        assertEquals("Content of ressponse is: ", 2, content);
    }

    @Test
    public void createWithZeroBalance() {
        target("/create").request()
                .post(null);

        Response response = target("/1").request()
                .get();

        long content = response.readEntity(Integer.class);
        assertEquals("Content of ressponse is: ", 0, content);
    }

    @Test
    public void deleteRemovesFromRepository() {
        target("/create").request()
                .post(null);

        Response response = target("/1").request()
                .delete();

        assertEquals("Http Response should be 200: ", Response.Status.OK.getStatusCode(), response.getStatus());

        response = target("/1").request()
                .delete();

        assertEquals("Http Response should be 404: ", Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void putMoneyWorks() {
        target("/create").request()
                .post(null);

        Response response = target("/1/10").request()
                .put(Entity.form(new Form()));

        assertEquals("Http Response should be 200: ", Response.Status.OK.getStatusCode(), response.getStatus());

        response = target("/1").request()
                .get();

        long content = response.readEntity(Integer.class);
        assertEquals("Content of ressponse is: ", 10, content);
    }

    @Test
    public void putMoneyToIncorrectNotWorks() {
        target("/create").request()
                .post(null);

        Response response = target("/2/10").request()
                .put(Entity.form(new Form()));

        assertEquals("Http Response should be 404: ", Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void putMoneyNegativeAmountNotWorks() {
        target("/create").request()
                .post(null);

        Response response = target("/1/-10").request()
                .put(Entity.form(new Form()));

        assertEquals("Http Response should be 409: ", Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        response = target("/1").request()
                .get();
        assertEquals("Amount must be same", 0, (long) response.readEntity(Integer.class));
    }

    @Test
    public void transferMoneyWorks() {
        target("/create").request()
                .post(null);
        target("/create").request()
                .post(null);

        target("/1/10").request()
                .put(Entity.form(new Form()));

        Response response = target("/1/2/10").request()
                .put(Entity.form(new Form()));

        assertEquals("Http Response should be 200: ", Response.Status.OK.getStatusCode(), response.getStatus());

        response = target("/1").request()
                .get();

        long content = response.readEntity(Integer.class);
        assertEquals("Content of ressponse is: ", 0, content);

        response = target("/2").request()
                .get();

        content = response.readEntity(Integer.class);
        assertEquals("Content of ressponse is: ", 10, content);
    }

    @Test
    public void transferMoneyFromIncorrectNotWorks() {
        target("/create").request()
                .post(null);
        target("/create").request()
                .post(null);

        target("/1/10").request()
                .put(Entity.form(new Form()));

        Response response = target("/0/2/10").request()
                .put(Entity.form(new Form()));

        assertEquals("Http Response should be 404: ", Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        response = target("/2").request()
                .get();
        assertEquals("Amount must be same", 0, (long) response.readEntity(Integer.class));
    }

    @Test
    public void transferMoneyToIncorrectNotWorks() {
        target("/create").request()
                .post(null);
        target("/create").request()
                .post(null);

        target("/1/10").request()
                .put(Entity.form(new Form()));

        Response response = target("/1/-1/10").request()
                .put(Entity.form(new Form()));

        assertEquals("Http Response should be 404: ", Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        response = target("/1").request()
                .get();
        assertEquals("Amount must be same", 10, (long) response.readEntity(Integer.class));
    }

    @Test
    public void transferMoneyNegativeAmountNotWorks() {
        target("/create").request()
                .post(null);
        target("/create").request()
                .post(null);

        target("/1/10").request()
                .put(Entity.form(new Form()));

        Response response = target("/1/2/-10").request()
                .put(Entity.form(new Form()));

        assertEquals("Http Response should be 409: ", Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        response = target("/1").request()
                .get();
        assertEquals("Amount must be same", 10, (long) response.readEntity(Integer.class));

        response = target("/2").request()
                .get();
        assertEquals("Amount must be same", 0, (long) response.readEntity(Integer.class));
    }

    @Test
    public void transferMoneyNotEnoughAmountNotWorks() {
        target("/create").request()
                .post(null);
        target("/create").request()
                .post(null);

        target("/1/10").request()
                .put(Entity.form(new Form()));

        Response response = target("/1/2/11").request()
                .put(Entity.form(new Form()));

        assertEquals("Http Response should be 403: ", Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());

        response = target("/1").request()
                .get();
        assertEquals("Amount must be same", 10, (long) response.readEntity(Integer.class));

        response = target("/2").request()
                .get();
        assertEquals("Amount must be same", 0, (long) response.readEntity(Integer.class));
    }

    @Test
    public void getFromIncorrectAccountNotWorks() {
        target("/create").request()
                .post(null);;

        Response response = target("/2").request()
                .get();

        assertEquals("Http Response should be 404: ", Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void concurrentAddingWorks() throws InterruptedException, ExecutionException {
        target("/create").request()
                .post(null);

        Future[] resp = new Future[1000];
        for(int i = 0; i < 1000; i++) {
            resp[i] = target("/1/1").request()
                    .async().put(Entity.form(new Form()));
        }

        for(int i = 0; i < 1000; i++)
            resp[i].get();

        Response response = target("/1").request()
                .get();

        long content = response.readEntity(Integer.class);
        assertEquals("Content of response is: ", 1000, content);
    }

    @Test
    public void concurrentTransferWorks() throws InterruptedException, ExecutionException {
        target("/create").request()
                .post(null);
        target("/create").request()
                .post(null);

        target("/1/1000").request()
                .put(Entity.form(new Form()));

        Future[] resp = new Future[1000];
        for(int i = 0; i < 1000; i++) {
            resp[i] = target("/1/2/1").request()
                    .async().put(Entity.form(new Form()));
        }

        for(int i = 0; i < 1000; i++)
            resp[i].get();

        Response response = target("/1").request()
                .get();

        long content = response.readEntity(Integer.class);
        assertEquals("Content of response is: ", 0, content);

        response = target("/2").request()
                .get();

        content = response.readEntity(Integer.class);
        assertEquals("Content of response is: ", 1000, content);
    }
}
