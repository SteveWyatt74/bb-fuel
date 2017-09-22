package com.backbase.testing.dataloader.clients.user;

import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.data.CommonConstants;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import com.backbase.integration.user.rest.spec.v2.users.EntitlementsAdminPostRequestBody;
import com.backbase.integration.user.rest.spec.v2.users.UsersPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class UserIntegrationRestClient extends RestClient {


    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_USER_INTEGRATION_SERVICE = "/user-integration-service/" + SERVICE_VERSION + "/users";
    private static final String ENDPOINT_ENTITLEMENTS_ADMIN = ENDPOINT_USER_INTEGRATION_SERVICE + "/entitlementsAdmin";

    public UserIntegrationRestClient() {
        super(globalProperties.get(CommonConstants.PROPERTY_ENTITLEMENTS_BASE_URI));
    }

    public Response ingestEntitlementsAdminUnderLE(String userExternalId, String legalEntityExternalId) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(new EntitlementsAdminPostRequestBody()
                        .withExternalId(userExternalId)
                        .withLegalEntityExternalId(legalEntityExternalId))
                .post(ENDPOINT_ENTITLEMENTS_ADMIN);
    }

    public Response ingestUser(UsersPostRequestBody body) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(body)
                .post(ENDPOINT_USER_INTEGRATION_SERVICE);
    }
}