package revolut.model;

import revolut.exceptions.InvalidAmountException;
import revolut.exceptions.NotEnoughMoneyException;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Account {
    private AtomicReference<BigDecimal> amount = new AtomicReference<>(BigDecimal.ZERO);
    private final int userId;

    public Account(int userId) {
        this.userId = userId;
    }

    public void putMoney(final BigDecimal amount) throws InvalidAmountException {
        if(amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException();
        }
        this.amount.updateAndGet(cur -> cur.add(amount));
    }

    public void transfer(Account toAccount, final BigDecimal amount) throws NotEnoughMoneyException, InvalidAmountException {
        if(amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException();
        }
        AtomicBoolean ok = new AtomicBoolean(true);
        this.amount.updateAndGet(cur -> {
            if(cur.compareTo(amount) >= 0)
                return cur.subtract(amount);
            else {
                ok.set(false);
                return cur;
            }
        });
        if(!ok.get()) {
            throw new NotEnoughMoneyException();
        }
        toAccount.putMoney(amount);
    }

    public BigDecimal getAmount() {
        return amount.get();
    }

    public int getId() {
        return userId;
    }
}
