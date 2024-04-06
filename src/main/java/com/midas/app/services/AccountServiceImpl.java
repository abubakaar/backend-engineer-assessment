package com.midas.app.services;

import com.midas.app.models.Account;
import com.midas.app.repositories.AccountRepository;
import com.midas.app.workflows.CreateAccountWorkflow;
import com.midas.generated.model.AccountUpdateRequestDto;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.workflow.Workflow;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
  private final Logger logger = Workflow.getLogger(AccountServiceImpl.class);

  private final WorkflowClient workflowClient;

  private final AccountRepository accountRepository;

  private final WorkflowServiceStubs workflowServiceStubs;

  /**
   * createAccount creates a new account in the system or provider.
   *
   * @param details is the details of the account to be created.
   * @return Account
   */
  @Override
  public Account createAccount(Account details) {
    WorkflowOptions options =
        WorkflowOptions.newBuilder()
            .setTaskQueue(CreateAccountWorkflow.QUEUE_NAME)
            .setWorkflowId(details.getEmail())
            .build();

    logger.info("initiating workflow to create account for email: {}", details.getEmail());

    CreateAccountWorkflow workflow =
        workflowClient.newWorkflowStub(CreateAccountWorkflow.class, options);

    return workflow.createAccount(details);
  }

  /**
   * getAccounts returns a list of accounts.
   *
   * @return List<Account>
   */
  @Override
  public List<Account> getAccounts() {
    return accountRepository.findAll();
  }

  /**
   * updateAccounts returns a account to be updated.
   *
   * @return Account
   */
  @Override
  public Account updateAccount(UUID accountId, AccountUpdateRequestDto updateRequest) {
    // Fetch the Account
    Account existingAccount =
        accountRepository
            .findById(accountId)
            .orElseThrow(() -> new NoClassDefFoundError("Account not found"));

    //  Apply Updates
    if (updateRequest.getFirstName() != null) {
      existingAccount.setFirstName(updateRequest.getFirstName());
    }
    if (updateRequest.getLastName() != null) {
      existingAccount.setLastName(updateRequest.getLastName());
    }
    if (updateRequest.getEmail() != null) {
      existingAccount.setEmail(updateRequest.getEmail());
    }

    // Save Changes
    return accountRepository.save(existingAccount);
  }
}
