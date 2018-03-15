package com.backbase.testing.dataloader.clients.accessgroup;

import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.usercontext.UserContextPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesGetResponseBody;
import com.backbase.testing.dataloader.clients.common.AbstractRestClient;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UserContextPresentationRestClient extends AbstractRestClient {

    private static final String SERVICE_VERSION = "v2";
    private static final String ACCESS_GROUP_PRESENTATION_SERVICE = "accessgroup-presentation-service";
    private static final String ENDPOINT_ACCESS_GROUPS = "/accessgroups";
    private static final String ENDPOINT_USER_CONTEXT = ENDPOINT_ACCESS_GROUPS + "/usercontext";
    private static final String ENDPOINT_USER_CONTEXT_SERVICE_AGREEMENTS = ENDPOINT_USER_CONTEXT + "/serviceagreements";
    private static final String ENDPOINT_USER_CONTEXT_LEGAL_ENTITIES_BY_SERVICE_AGREEMENT_ID = ENDPOINT_USER_CONTEXT_SERVICE_AGREEMENTS + "/%s/legalentities";

    public UserContextPresentationRestClient() {
        super(SERVICE_VERSION);
        setInitialPath(getGatewayURI() + "/" + ACCESS_GROUP_PRESENTATION_SERVICE);
    }

    public Response postUserContext(UserContextPostRequestBody userContextPostRequestBody) {
        Response response = requestSpec()
            .contentType(ContentType.JSON)
            .body(userContextPostRequestBody)
            .post(getPath(ENDPOINT_USER_CONTEXT));

        Map<String, String> cookies = new HashMap<>(response.then()
            .extract()
            .cookies());
        setUpCookies(cookies);

        return response;
    }

    public Response getServiceAgreementsForUserContext() {
        return requestSpec()
            .contentType(ContentType.JSON)
            .get(getPath(ENDPOINT_USER_CONTEXT_SERVICE_AGREEMENTS));
    }

    public Response getLegalEntitiesForServiceAgreements(String serviceAgreementId) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .get(String.format(getPath(ENDPOINT_USER_CONTEXT_LEGAL_ENTITIES_BY_SERVICE_AGREEMENT_ID), serviceAgreementId));
    }

    public void selectContextBasedOnMasterServiceAgreement() {
        ServiceAgreementGetResponseBody masterServiceAgreement = getMasterServiceAgreementForUserContext();
        String serviceAgreementId = masterServiceAgreement != null ? masterServiceAgreement.getId() : null;

        String legalEntityId = getLegalEntitiesForServiceAgreements(serviceAgreementId)
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(LegalEntitiesGetResponseBody[].class)[0].getId();

        postUserContext(new UserContextPostRequestBody()
            .withServiceAgreementId(serviceAgreementId)
            .withLegalEntityId(legalEntityId))
            .then()
            .statusCode(SC_NO_CONTENT);
    }

    private ServiceAgreementGetResponseBody getMasterServiceAgreementForUserContext() {
        ServiceAgreementGetResponseBody[] serviceAgreementGetResponseBodies = getServiceAgreementsForUserContext()
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ServiceAgreementGetResponseBody[].class);

        return Arrays.stream(serviceAgreementGetResponseBodies)
            .filter(ServiceAgreementGetResponseBody::getIsMaster)
            .findFirst()
            .orElse(null);
    }
}