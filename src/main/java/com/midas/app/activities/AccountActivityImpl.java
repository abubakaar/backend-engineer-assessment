package com.midas.app.activities;

import com.midas.app.models.Account;
import com.midas.app.repositories.AccountRepository;
import com.midas.app.services.AccountServiceImpl;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import io.temporal.workflow.Workflow;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

@RequiredArgsConstructor
public class AccountActivityImpl implements AccountActivity {

  private final Logger logger = Workflow.getLogger(AccountServiceImpl.class);

  private final AccountRepository repository;

  @Override
  public Account saveAccount(Account account) {
    logger.info("Started saving account for email: {}", account.getEmail());
    return repository.save(account);
  }

  @Override
  public Account createPaymentAccount(Account account) {
    try {

      Stripe.apiKey = "";
      logger.info("Started workflow to create account for email: {}", account.getEmail());

      CustomerCreateParams params =
          CustomerCreateParams.builder()
              .setName(account.getFirstName() + account.getLastName())
              .setEmail(account.getEmail())
              .build();

      Customer customer = Customer.create(params);
      logger.info("Account for user {} Created Successfully", account.getEmail());

      account.setProviderId(customer.getId());
      return account;

    } catch (StripeException e) {
      logger.error(e.getMessage());
      return account;
    }
  }
}
