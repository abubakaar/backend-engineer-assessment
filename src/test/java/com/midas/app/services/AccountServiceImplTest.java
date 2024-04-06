package com.midas.app.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.midas.app.enums.ProviderTypes;
import com.midas.app.models.Account;
import com.midas.app.repositories.AccountRepository;
import com.midas.app.workflows.CreateAccountWorkflow;
import com.midas.generated.model.AccountUpdateRequestDto;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

  @Mock private AccountRepository accountRepository;

  @InjectMocks private AccountServiceImpl accountService;

  @Mock private WorkflowClient workflowClient;

  @Mock private CreateAccountWorkflow createAccountWorkflow;

  @Test
  void testUpdateAccount() {
    // Mock data
    UUID accountId = UUID.randomUUID();
    AccountUpdateRequestDto updateRequest = new AccountUpdateRequestDto();
    updateRequest.setFirstName("John");
    updateRequest.setLastName("Doe");
    updateRequest.setEmail("john.doe@example.com");

    Account existingAccount = new Account();
    existingAccount.setId(accountId);
    existingAccount.setFirstName("OldFirstName");
    existingAccount.setLastName("OldLastName");
    existingAccount.setEmail("old.email@example.com");

    // Mocking repository behavior
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
    when(accountRepository.save(existingAccount))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Call the method under test
    Account updatedAccount = accountService.updateAccount(accountId, updateRequest);

    // Verify repository interactions
    verify(accountRepository, times(1)).findById(accountId);
    verify(accountRepository, times(1)).save(existingAccount);

    // Assert the returned account
    assertEquals("John", updatedAccount.getFirstName());
    assertEquals("Doe", updatedAccount.getLastName());
    assertEquals("john.doe@example.com", updatedAccount.getEmail());
  }

  @Test
  public void testCreateAccount() {
    // Given
    Account details = new Account();
    details.setEmail("test@example.com");

    WorkflowOptions options =
        WorkflowOptions.newBuilder()
            .setTaskQueue(CreateAccountWorkflow.QUEUE_NAME)
            .setWorkflowId(details.getEmail())
            .build();

    // Mock the behavior of workflowClient
    when(workflowClient.newWorkflowStub(CreateAccountWorkflow.class, options))
        .thenReturn(createAccountWorkflow);

    Account expectedAccount = new Account();
    // Mock the behavior of createAccount method in the workflow
    when(createAccountWorkflow.createAccount(details)).thenReturn(expectedAccount);

    // When
    Account result = accountService.createAccount(details);

    // Then
    assertEquals(expectedAccount, result);
    // Verify that the workflowClient was called with the correct parameters
    verify(workflowClient).newWorkflowStub(CreateAccountWorkflow.class, options);
    // Verify that the createAccount method in the workflow was called with the correct parameters
    verify(createAccountWorkflow).createAccount(details);
  }

  @Test
  public void testNewFieldsExists() {
    // Given
    List<Account> expectedAccounts =
        Arrays.asList(
            Account.builder()
                .id(UUID.randomUUID())
                .providerType(ProviderTypes.STRIPE.name())
                .providerId("cus_name")
                .firstName("Abuakar")
                .build());

    // Mock the behavior of accountRepository
    when(accountRepository.findAll()).thenReturn(expectedAccounts);

    // When
    List<Account> result = accountService.getAccounts();

    // Then
    assertEquals(expectedAccounts.size(), result.size());
    assertEquals(result.get(0).getProviderType(), ProviderTypes.STRIPE.name());
    assertTrue(result.containsAll(expectedAccounts));
    verify(accountRepository).findAll();
  }
}
