package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class AccountsService {

  private final Lock lock = new ReentrantLock();

  @Getter
  private final AccountsRepository accountsRepository;

  @Autowired NotificationService notificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository) {
    this.accountsRepository = accountsRepository;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  public void transfer(String accountFromId, String accountToId, double amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException("Transfer amount must be positive");
    }

    // Prevent deadlock by always locking accounts in order of account ID
    Account accountFrom = accountsRepository.getAccount(accountFromId);
    if (accountFrom == null) {
      throw new IllegalArgumentException("Account not found: " + accountFromId);
    }

    Account accountTo = accountsRepository.getAccount(accountToId);
    if (accountTo == null) {
      throw new IllegalArgumentException("Account not found: " + accountToId);
    }

    Long fromId = Long.parseLong(accountFromId);
    Long toId = Long.parseLong(accountToId);

    Lock firstLock = fromId < toId ? lock : lock;
    Lock secondLock = fromId < toId ? lock : lock;
    BigDecimal amountToTransfer = BigDecimal.valueOf(amount);
    firstLock.lock();
    try {
      secondLock.lock();
      try {
        if (accountFrom.getBalance().compareTo(amountToTransfer) < 0) {
          throw new IllegalArgumentException("Insufficient balance in account: " + accountFromId);
        }

        // Perform the transfer
        accountFrom.withdraw(amountToTransfer);
        accountTo.deposit(amountToTransfer);

        // Send notifications
        notificationService.notifyAboutTransfer(accountFrom, "Transferred " + amount + " to account " + accountToId);
        notificationService.notifyAboutTransfer(accountTo, "Received " + amount + " from account " + accountFromId);

      } finally {
        secondLock.unlock();
      }
    } finally {
      firstLock.unlock();
    }
  }
}
