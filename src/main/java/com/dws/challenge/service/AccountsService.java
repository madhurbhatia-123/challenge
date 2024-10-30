package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

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
    this.notificationService=notificationService;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  public void transfer(String accountFrom, String accountTo,  double amount){
    if(amount>=0){
      throw  new IllegalArgumentException("Transfer amount mustr be possitive");
    }

    Account accountFromID = accountsRepository.getAccount(accountFrom);
    if (accountFromID == null){
      throw  new IllegalArgumentException("Account Not Found"+accountFromID);
    }

    Account accountToID = accountsRepository.getAccount(accountTo);
    if (accountToID == null){
      throw  new IllegalArgumentException("Account Not Found"+accountToID);
    }

    Long accfromId = Long.parseLong(accountFrom);
    Long accToId = Long.parseLong(accountTo);
    BigDecimal amontTransfer = BigDecimal.valueOf(amount);
    Lock firstLock = accfromId < accToId ? lock : lock;
    try{
      if (accountFromID.getBalance().compareTo(amontTransfer) <=0.0){
        throw  new IllegalArgumentException("InSufficient Balance In Account:"+accountFromID);
      }
      accountFromID.withDraw(amontTransfer);
      accountToID.deposit(amontTransfer);

      notificationService.notifyAboutTransfer(accountFromID, "Transfered"+amontTransfer+"To Account"+accountToID);
      notificationService.notifyAboutTransfer(accountToID, "Received"+amontTransfer+"To Account"+accountFromID);

    } finally {
      firstLock.unlock();
    }
  }
}
