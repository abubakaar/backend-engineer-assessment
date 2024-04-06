package com.midas.app.conig;

import com.midas.app.activities.AccountActivityImpl;
import com.midas.app.providers.external.stripe.StripeConfiguration;
import com.midas.app.repositories.AccountRepository;
import com.midas.app.workflows.CreateAccountWorkflow;
import com.midas.app.workflows.CreateAccountWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkflowImplementationOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class config {

  @Bean
  public WorkflowServiceStubs workflowServiceStubs() {
    return WorkflowServiceStubs.newInstance(
        WorkflowServiceStubsOptions.newBuilder().setTarget("127.0.0.1:7233").build());
  }

  @Bean
  public WorkflowClient workflowClient(WorkflowServiceStubs workflowServiceStubs) {
    return WorkflowClient.newInstance(
        workflowServiceStubs, WorkflowClientOptions.newBuilder().setNamespace("default").build());
  }

  @Bean
  public WorkerFactory workerFactory(
      WorkflowClient workflowClient,
      AccountRepository repository,
      StripeConfiguration configuration) {

    WorkerFactory workerFactory = WorkerFactory.newInstance(workflowClient);

    WorkflowImplementationOptions workflowImplementationOptions =
        WorkflowImplementationOptions.newBuilder()
            .setFailWorkflowExceptionTypes(NullPointerException.class)
            .build();

    // create Worker
    Worker worker = workerFactory.newWorker(CreateAccountWorkflow.QUEUE_NAME);

    // Registering WorkFlow
    worker.registerWorkflowImplementationTypes(
        workflowImplementationOptions, CreateAccountWorkflowImpl.class);

    // Registering Activity
    worker.registerActivitiesImplementations(new AccountActivityImpl(repository, configuration));

    workerFactory.start();
    return workerFactory;
  }
}
