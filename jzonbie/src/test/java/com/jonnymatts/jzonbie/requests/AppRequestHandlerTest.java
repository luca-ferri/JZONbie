package com.jonnymatts.jzonbie.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jonnymatts.jzonbie.Request;
import com.jonnymatts.jzonbie.Response;
import com.jonnymatts.jzonbie.history.CallHistory;
import com.jonnymatts.jzonbie.history.Exchange;
import com.jonnymatts.jzonbie.history.FixedCapacityCache;
import com.jonnymatts.jzonbie.metadata.MetaDataContext;
import com.jonnymatts.jzonbie.priming.AppRequestFactory;
import com.jonnymatts.jzonbie.priming.PrimingContext;
import com.jonnymatts.jzonbie.priming.ZombiePriming;
import com.jonnymatts.jzonbie.responses.AppResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppRequestHandlerTest {

    @Mock private PrimingContext primingContext;
    @Mock private CallHistory callHistory;
    @Mock private FixedCapacityCache<AppRequest> failedRequests;
    @Mock private AppRequestFactory appRequestFactory;
    @Mock private Request request;
    @Mock private MetaDataContext metaDataContext;

    private ZombiePriming zombiePriming;
    private Exchange exchange;

    private AppRequestHandler appRequestHandler;

    private AppRequest appRequest;

    private AppResponse appResponse;

    @BeforeEach
    void setUp() throws Exception {
        appRequestHandler = new AppRequestHandler(primingContext, callHistory, failedRequests, appRequestFactory);

        appRequest = get("/");
        appResponse = ok();

        zombiePriming = new ZombiePriming(appRequest, appResponse);
        exchange = new Exchange(appRequest, appResponse);

        when(appRequestFactory.create(request)).thenReturn(appRequest);
    }

    @Test
    void handleReturnsPrimedResponseIfPrimingKeyExistsInPrimingContext() {
        when(primingContext.getPrimedRequest(appRequest)).thenReturn(of(appRequest));
        when(primingContext.getResponse(appRequest))
                .thenReturn(of(appResponse));
        final Response got = appRequestHandler.handle(request, metaDataContext);

        assertThat(got).isEqualTo(appResponse);
    }

    @Test
    void handleAddsPrimingRequestToCallHistory() throws JsonProcessingException {
        when(primingContext.getPrimedRequest(appRequest)).thenReturn(of(appRequest));
        when(primingContext.getResponse(appRequest))
                .thenReturn(of(appResponse));
        appRequestHandler.handle(request, metaDataContext);

        verify(callHistory).add(appRequest, exchange);
    }

    @Test
    void handleThrowsPrimingNotFoundExceptionIfPrimingIsNotFound() throws Exception {
        assertThatThrownBy(() -> appRequestHandler.handle(request, metaDataContext))
                .isExactlyInstanceOf(PrimingNotFoundException.class)
                .hasFieldOrPropertyWithValue("request", appRequest);
    }

    @Test
    void handleAddsRequestToFailedRequestsIfPrimingIsNotFound() throws Exception {
        try{
            appRequestHandler.handle(request, metaDataContext);
        } catch (Exception e) {
            verify(failedRequests).add(appRequest);
        }
    }
}