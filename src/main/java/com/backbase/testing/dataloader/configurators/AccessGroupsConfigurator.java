package com.backbase.testing.dataloader.configurators;


import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.testing.dataloader.data.EntitlementsDataGenerator;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.data.DataGroupsPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

public class AccessGroupsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessGroupsConfigurator.class);

    private EntitlementsDataGenerator entitlementsDataGenerator = new EntitlementsDataGenerator();
    private AccessGroupIntegrationRestClient accessGroupIntegrationRestClient = new AccessGroupIntegrationRestClient();

    public void ingestFunctionGroupsWithAllPrivilegesForAllFunctions(String externalLegalEntityId) {
        FunctionsGetResponseBody[] functions = accessGroupIntegrationRestClient.retrieveFunctions()
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(FunctionsGetResponseBody[].class);

        for (FunctionsGetResponseBody function : functions) {
            List<Privilege> functionPrivileges = function.getPrivileges();
            List<String> privileges = new ArrayList<>();

            functionPrivileges.forEach(fp -> privileges.add(fp.getPrivilege()));

            ingestFunctionGroupByFunctionName(externalLegalEntityId, function.getName(), privileges);
        }
    }

    private void ingestFunctionGroupByFunctionName(String externalLegalEntityId, String functionName, List<String> privileges) {
        String functionId = accessGroupIntegrationRestClient.retrieveFunctionByName(functionName)
                .getFunctionId();
        accessGroupIntegrationRestClient.ingestFunctionGroup(entitlementsDataGenerator.generateFunctionGroupsPostRequestBody(externalLegalEntityId, functionId, privileges))
                .then()
                .statusCode(SC_CREATED);
        LOGGER.info(String.format("Function group ingested (legal entity [%s]) for function [%s] with privileges %s", externalLegalEntityId, functionName, privileges));
    }

    public void ingestDataGroupForArrangements(String externalLegalEntityId, List<String> internalArrangementIds) {
        accessGroupIntegrationRestClient.ingestDataGroup(entitlementsDataGenerator.generateDataGroupsPostRequestBody(externalLegalEntityId, DataGroupsPostRequestBody.Type.ARRANGEMENTS, internalArrangementIds))
                .then()
                .statusCode(SC_CREATED);
        LOGGER.info(String.format("Data group ingested (legal entity [%s]) for arrangements %s", externalLegalEntityId, internalArrangementIds));
    }
}