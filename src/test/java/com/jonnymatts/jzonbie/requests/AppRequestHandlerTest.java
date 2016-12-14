package com.jonnymatts.jzonbie.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import com.google.common.collect.Multimap;
import com.jonnymatts.jzonbie.model.PrimedRequest;
import com.jonnymatts.jzonbie.model.PrimedRequestFactory;
import com.jonnymatts.jzonbie.model.PrimedResponse;
import com.jonnymatts.jzonbie.model.JZONbieRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import spark.Request;
import spark.Response;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppRequestHandlerTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Mock private Multimap<PrimedRequest, PrimedResponse> primingContext;

    @Mock private List<JZONbieRequest> callHistory;

    @Mock private PrimedRequestFactory primedRequestFactory;

    @Mock private Request request;

    @Mock private Response response;

    @Fixture private JZONbieRequest JZONbieRequest;

    @Fixture private String path;

    @Fixture private String responseString;

    private AppRequestHandler appRequestHandler;

    private PrimedRequest primedRequest;

    private PrimedResponse primedResponse;

    @Before
    public void setUp() throws Exception {
        appRequestHandler = new AppRequestHandler(primingContext, callHistory, primedRequestFactory);

        primedRequest = JZONbieRequest.getPrimedRequest();
        primedResponse = JZONbieRequest.getPrimedResponse();

        when(primedRequestFactory.create(request)).thenReturn(primedRequest);
        when(primingContext.get(primedRequest))
                .thenReturn(singletonList(primedResponse));
    }

    @Test
    public void handleReturnsPrimedResponseIfPrimingKeyExistsInPrimingContext() throws JsonProcessingException {
        final Object got = appRequestHandler.handle(request, response);

        assertThat(got).isEqualTo(primedResponse.getBody());

        verify(response).status(primedResponse.getStatusCode());
        primedResponse.getHeaders().entrySet()
                .forEach(entry -> verify(response).header(entry.getKey(), entry.getValue()));
        verify(primingContext).remove(primedRequest, primedResponse);
    }

    @Test
    public void handleDoesNotAddHeadersToResponseIfPrimedResponseDoesNotHaveHeaders() throws JsonProcessingException {
        primedResponse.setHeaders(null);

        appRequestHandler.handle(request, response);

        verify(response).status(primedResponse.getStatusCode());
        verifyNoMoreInteractions(response);
    }

    @Test
    public void handleAddsPrimingRequestToCallHistory() throws JsonProcessingException {
        appRequestHandler.handle(request, response);

        verify(callHistory).add(JZONbieRequest);
    }
}