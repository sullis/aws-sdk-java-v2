package software.amazon.awssdk.services.jsonprotocoltests.paginators;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;
import software.amazon.awssdk.annotations.Generated;
import software.amazon.awssdk.core.pagination.sync.PaginatedItemsIterable;
import software.amazon.awssdk.core.pagination.sync.PaginatedResponsesIterator;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.core.pagination.sync.SyncPageFetcher;
import software.amazon.awssdk.services.jsonprotocoltests.JsonProtocolTestsClient;
import software.amazon.awssdk.services.jsonprotocoltests.internal.UserAgentUtils;
import software.amazon.awssdk.services.jsonprotocoltests.model.PaginatedOperationWithResultKeyAndMoreResultsRequest;
import software.amazon.awssdk.services.jsonprotocoltests.model.PaginatedOperationWithResultKeyAndMoreResultsResponse;
import software.amazon.awssdk.services.jsonprotocoltests.model.SimpleStruct;

/**
 * <p>
 * Represents the output for the
 * {@link software.amazon.awssdk.services.jsonprotocoltests.JsonProtocolTestsClient#paginatedOperationWithResultKeyAndMoreResultsPaginator(software.amazon.awssdk.services.jsonprotocoltests.model.PaginatedOperationWithResultKeyAndMoreResultsRequest)}
 * operation which is a paginated operation. This class is an iterable of
 * {@link software.amazon.awssdk.services.jsonprotocoltests.model.PaginatedOperationWithResultKeyAndMoreResultsResponse}
 * that can be used to iterate through all the response pages of the operation.
 * </p>
 * <p>
 * When the operation is called, an instance of this class is returned. At this point, no service calls are made yet and
 * so there is no guarantee that the request is valid. As you iterate through the iterable, SDK will start lazily
 * loading response pages by making service calls until there are no pages left or your iteration stops. If there are
 * errors in your request, you will see the failures only after you start iterating through the iterable.
 * </p>
 *
 * <p>
 * The following are few ways to iterate through the response pages:
 * </p>
 * 1) Using a Stream
 *
 * <pre>
 * {@code
 * software.amazon.awssdk.services.jsonprotocoltests.paginators.PaginatedOperationWithResultKeyAndMoreResultsIterable responses = client.paginatedOperationWithResultKeyAndMoreResultsPaginator(request);
 * responses.stream().forEach(....);
 * }
 * </pre>
 *
 * 2) Using For loop
 *
 * <pre>
 * {
 *     &#064;code
 *     software.amazon.awssdk.services.jsonprotocoltests.paginators.PaginatedOperationWithResultKeyAndMoreResultsIterable responses = client
 *             .paginatedOperationWithResultKeyAndMoreResultsPaginator(request);
 *     for (software.amazon.awssdk.services.jsonprotocoltests.model.PaginatedOperationWithResultKeyAndMoreResultsResponse response : responses) {
 *         // do something;
 *     }
 * }
 * </pre>
 *
 * 3) Use iterator directly
 *
 * <pre>
 * {@code
 * software.amazon.awssdk.services.jsonprotocoltests.paginators.PaginatedOperationWithResultKeyAndMoreResultsIterable responses = client.paginatedOperationWithResultKeyAndMoreResultsPaginator(request);
 * responses.iterator().forEachRemaining(....);
 * }
 * </pre>
 * <p>
 * <b>Please notice that the configuration of MaxResults won't limit the number of results you get with the paginator.
 * It only limits the number of results in each page.</b>
 * </p>
 * <p>
 * <b>Note: If you prefer to have control on service calls, use the
 * {@link #paginatedOperationWithResultKeyAndMoreResults(software.amazon.awssdk.services.jsonprotocoltests.model.PaginatedOperationWithResultKeyAndMoreResultsRequest)}
 * operation.</b>
 * </p>
 */
@Generated("software.amazon.awssdk:codegen")
public class PaginatedOperationWithResultKeyAndMoreResultsIterable implements
                                                                   SdkIterable<PaginatedOperationWithResultKeyAndMoreResultsResponse> {
    private final JsonProtocolTestsClient client;

    private final PaginatedOperationWithResultKeyAndMoreResultsRequest firstRequest;

    private final SyncPageFetcher nextPageFetcher;

    public PaginatedOperationWithResultKeyAndMoreResultsIterable(JsonProtocolTestsClient client,
                                                                 PaginatedOperationWithResultKeyAndMoreResultsRequest firstRequest) {
        this.client = client;
        this.firstRequest = UserAgentUtils.applyPaginatorUserAgent(firstRequest);
        this.nextPageFetcher = new PaginatedOperationWithResultKeyAndMoreResultsResponseFetcher();
    }

    @Override
    public Iterator<PaginatedOperationWithResultKeyAndMoreResultsResponse> iterator() {
        return PaginatedResponsesIterator.builder().nextPageFetcher(nextPageFetcher).build();
    }

    /**
     * Returns an iterable to iterate through the paginated
     * {@link PaginatedOperationWithResultKeyAndMoreResultsResponse#items()} member. The returned iterable is used to
     * iterate through the results across all response pages and not a single page.
     *
     * This method is useful if you are interested in iterating over the paginated member in the response pages instead
     * of the top level pages. Similar to iteration over pages, this method internally makes service calls to get the
     * next list of results until the iteration stops or there are no more results.
     */
    public final SdkIterable<SimpleStruct> items() {
        Function<PaginatedOperationWithResultKeyAndMoreResultsResponse, Iterator<SimpleStruct>> getIterator = response -> {
            if (response != null && response.items() != null) {
                return response.items().iterator();
            }
            return Collections.emptyIterator();
        };
        return PaginatedItemsIterable.<PaginatedOperationWithResultKeyAndMoreResultsResponse, SimpleStruct> builder()
                                     .pagesIterable(this).itemIteratorFunction(getIterator).build();
    }

    private class PaginatedOperationWithResultKeyAndMoreResultsResponseFetcher implements
                                                                               SyncPageFetcher<PaginatedOperationWithResultKeyAndMoreResultsResponse> {
        @Override
        public boolean hasNextPage(PaginatedOperationWithResultKeyAndMoreResultsResponse previousPage) {
            return previousPage.truncated() != null && previousPage.truncated().booleanValue();
        }

        @Override
        public PaginatedOperationWithResultKeyAndMoreResultsResponse nextPage(
            PaginatedOperationWithResultKeyAndMoreResultsResponse previousPage) {
            if (previousPage == null) {
                return client.paginatedOperationWithResultKeyAndMoreResults(firstRequest);
            }
            return client.paginatedOperationWithResultKeyAndMoreResults(firstRequest.toBuilder()
                                                                                    .nextToken(previousPage.nextToken()).build());
        }
    }
}
