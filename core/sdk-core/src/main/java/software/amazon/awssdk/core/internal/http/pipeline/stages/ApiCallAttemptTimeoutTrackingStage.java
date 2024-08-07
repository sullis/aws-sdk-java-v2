/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.core.internal.http.pipeline.stages;

import static software.amazon.awssdk.core.internal.http.timers.TimerUtils.resolveTimeoutInMillis;
import static software.amazon.awssdk.core.internal.http.timers.TimerUtils.timeSyncTaskIfNeeded;
import static software.amazon.awssdk.utils.FunctionalUtils.runAndLogError;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import software.amazon.awssdk.annotations.SdkInternalApi;
import software.amazon.awssdk.core.Response;
import software.amazon.awssdk.core.client.config.SdkClientOption;
import software.amazon.awssdk.core.exception.AbortedException;
import software.amazon.awssdk.core.exception.ApiCallAttemptTimeoutException;
import software.amazon.awssdk.core.exception.ApiCallTimeoutException;
import software.amazon.awssdk.core.exception.SdkInterruptedException;
import software.amazon.awssdk.core.internal.http.HttpClientDependencies;
import software.amazon.awssdk.core.internal.http.RequestExecutionContext;
import software.amazon.awssdk.core.internal.http.pipeline.RequestPipeline;
import software.amazon.awssdk.core.internal.http.pipeline.RequestToResponsePipeline;
import software.amazon.awssdk.core.internal.http.timers.SyncTimeoutTask;
import software.amazon.awssdk.core.internal.http.timers.TimeoutTracker;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.utils.Logger;

/**
 * Wrapper around a {@link RequestPipeline} to manage the api call attempt timeout feature.
 */
@SdkInternalApi
public final class ApiCallAttemptTimeoutTrackingStage<OutputT> implements RequestToResponsePipeline<OutputT> {
    private static final Logger log = Logger.loggerFor(ApiCallAttemptTimeoutTrackingStage.class);

    private final RequestPipeline<SdkHttpFullRequest, Response<OutputT>> wrapped;
    private final Duration apiCallAttemptTimeout;
    private final ScheduledExecutorService timeoutExecutor;

    public ApiCallAttemptTimeoutTrackingStage(HttpClientDependencies dependencies,
                                              RequestPipeline<SdkHttpFullRequest,
                                              Response<OutputT>> wrapped) {
        this.wrapped = wrapped;
        this.timeoutExecutor = dependencies.clientConfiguration().option(SdkClientOption.SCHEDULED_EXECUTOR_SERVICE);
        this.apiCallAttemptTimeout = dependencies.clientConfiguration().option(SdkClientOption.API_CALL_ATTEMPT_TIMEOUT);
    }

    /**
     * Start and end api call attempt timer around the execution of the api call attempt. It's important
     * that the client execution task is canceled before the InterruptedException is handled by the wrapped
     * {@link RequestPipeline#execute} call so the interrupt status doesn't leak out to the callers code.
     */
    @Override
    public Response<OutputT> execute(SdkHttpFullRequest request, RequestExecutionContext context) throws Exception {
        try {
            long timeoutInMillis = resolveTimeoutInMillis(context.requestConfig()::apiCallAttemptTimeout, apiCallAttemptTimeout);

            TimeoutTracker timeoutTracker = timeSyncTaskIfNeeded(timeoutExecutor, timeoutInMillis, Thread.currentThread());

            Response<OutputT> response;
            try {
                context.apiCallAttemptTimeoutTracker(timeoutTracker);
                response = wrapped.execute(request, context);
            } finally {
                // Cancel the timeout tracker, guaranteeing that if it hasn't already executed and set this thread's
                // interrupt flag, it won't do so later. Every code path executed after this line *must* call
                // timeoutTracker.hasExecuted() and appropriately clear the interrupt flag if it returns true.
                timeoutTracker.cancel();
            }

            if (timeoutTracker.hasExecuted()) {
                // The timeout tracker executed before the call to cancel(), which means it set this thread's interrupt
                // flag. However, the execute() call returned before we raised an InterruptedException, so just clear
                // the interrupt flag and return the result we got back.
                Thread.interrupted();
            }
            return response;

        } catch (Exception e) {
            throw translatePipelineException(context, e);
        }
    }



    /**
     * Take the given exception thrown from the wrapped pipeline and return a more appropriate
     * timeout related exception based on its type and the the execution status.
     *
     * @param context The execution context.
     * @param e The exception thrown from the inner pipeline.
     * @return The translated exception.
     */
    private Exception translatePipelineException(RequestExecutionContext context, Exception e) {
        if (e instanceof InterruptedException) {
            return handleInterruptedException(context, (InterruptedException) e);
        }

        // Timeout tracker finished and interrupted this thread after wrapped.execute() last checked the interrupt flag,
        // but before we called timeoutTracker.cancel(). Note that if hasExecuted() returns true, its guaranteed that
        // the timeout tracker has set the interrupt flag, and if it returns false, it guarantees that it did not and
        // will never set the interrupt flag.
        if (context.apiCallAttemptTimeoutTracker().hasExecuted()) {
            // Clear the interrupt flag. Since we already have an exception from the call, which may contain information
            // that's useful to the caller, just return that instead of an ApiCallTimeoutException.
            Thread.interrupted();
        }

        return e;
    }

    /**
     * Determine if an interrupted exception is caused by the api call timeout task
     * interrupting the current thread or some other task interrupting the thread for another
     * purpose.
     *
     * @return {@link ApiCallTimeoutException} if the {@link InterruptedException} was
     * caused by the {@link SyncTimeoutTask}. Otherwise re-interrupts the current thread
     * and returns a {@link AbortedException} wrapping an {@link InterruptedException}
     */
    private RuntimeException handleInterruptedException(RequestExecutionContext context, InterruptedException e) {
        if (e instanceof SdkInterruptedException) {
            ((SdkInterruptedException) e).getResponseStream()
                                         .ifPresent(r -> runAndLogError(log.logger(),
                                                                        "Failed to close the response stream",
                                                                        r::close));
        }
        if (context.apiCallAttemptTimeoutTracker().hasExecuted()) {
            // Clear the interrupt status
            Thread.interrupted();
            return generateApiCallAttemptTimeoutException(context);
        }

        Thread.currentThread().interrupt();
        return AbortedException.create("Thread was interrupted", e);
    }

    private ApiCallAttemptTimeoutException generateApiCallAttemptTimeoutException(RequestExecutionContext context) {
        return ApiCallAttemptTimeoutException.create(
                resolveTimeoutInMillis(context.requestConfig()::apiCallAttemptTimeout, apiCallAttemptTimeout));
    }
}
