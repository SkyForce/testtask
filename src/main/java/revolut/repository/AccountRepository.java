package revolut.repository;

import revolut.exceptions.UserNotFoundException;
import revolut.model.Account;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AccountRepository {
    private Map<Integer, Account> users = new ConcurrentHashMap<>();
    private AtomicInteger count = new AtomicInteger();

    private static AccountRepository repository = new AccountRepository();

    public Account createAccount() {
        int userId = count.incrementAndGet();
        Account account = new Account(userId);
        users.put(userId, account);
        return account;
    }

    public Account getById(Integer id) throws UserNotFoundException {

        Account account = users.get(id);
        if(account == null) {
            throw new UserNotFoundException();
        }
        return account;
    }

    public static AccountRepository getInstance() {
        return repository;
    }

    public void deleteAccount(Integer id) throws UserNotFoundException {
        Account account = users.remove(id);
        if(account == null) {
            throw new UserNotFoundException();
        }
    }

    public void clear() {
        users.clear();
        count.set(0);
    }
}
