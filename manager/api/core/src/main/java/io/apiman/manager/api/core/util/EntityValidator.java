/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.manager.api.core.util;

import io.apiman.manager.api.beans.apis.ApiStatus;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.apis.ApiVersionStatusBean;
import io.apiman.manager.api.beans.apis.StatusItemBean;
import io.apiman.manager.api.beans.clients.ClientBean;
import io.apiman.manager.api.beans.clients.ClientStatus;
import io.apiman.manager.api.beans.clients.ClientVersionBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.contracts.ContractStatus;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.api.core.IApiValidator;
import io.apiman.manager.api.core.IClientValidator;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.core.i18n.Messages;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import com.google.common.collect.Lists;

/**
 * TODO could rework this to return or set a status?
 *
 * Validates the state of various entities, including APIs and clients.
 *
 * @author eric.wittmann@redhat.com
 */
public class EntityValidator implements IApiValidator, IClientValidator {

    @Inject
    private IStorageQuery storageQuery;
    @Inject
    private IStorage storage;

    /**
     * Constructor.
     */
    public EntityValidator() {
    }

    /**
     * @see io.apiman.manager.api.core.IClientValidator#isReady(io.apiman.manager.api.beans.clients.ClientVersionBean)
     */
    @Override
    public boolean isReady(ClientVersionBean client) throws Exception {
        List<ContractSummaryBean> contracts = storageQuery.getClientContracts(client.getClient().getOrganization().getId(), client
                .getClient().getId(), client.getVersion());
        // If empty, not ready
        if (contracts.isEmpty()) {
            return false;
        }
        // None must be in unapproved state
        return contracts.stream().allMatch(c -> c.getStatus() == ContractStatus.Created);
    }

    /**
     * @see io.apiman.manager.api.core.IClientValidator#isReady(io.apiman.manager.api.beans.clients.ClientVersionBean, boolean)
     */
    @Override
    public boolean isReady(ClientVersionBean client, boolean hasContracts) throws Exception {
        if (!hasContracts) {
            return false;
        }
        return isReady(client);
    }

    @Override
    public ClientStatus determineStatus(ClientVersionBean cvb, List<ContractBean> contracts) {
        ClientStatus currentStatus = cvb.getStatus();

        boolean anyAwaitingApproval = contracts.stream().anyMatch(c -> c.getStatus() == ContractStatus.AwaitingApproval);
        if (anyAwaitingApproval) {
            return ClientStatus.AwaitingApproval;
        }
        // If already registered, then continue to be registered (indicates some number of contracts are still active on gateway).
        if (currentStatus == ClientStatus.Registered) {
            return ClientStatus.Registered;
        }
        // No contracts and not registered, then just created state
        if (contracts.isEmpty()) {
            return ClientStatus.Created;
        }
        // Ready to be published
        return ClientStatus.Ready;
    }

    @Override
    public ClientStatus determineStatus(ClientVersionBean bean) throws StorageException {
        OrganizationBean orgBean = bean.getClient().getOrganization();
        ClientBean cb = bean.getClient();
        ArrayList<ContractBean> contracts = Lists.newArrayList(storage.getAllContracts(orgBean.getId(), cb.getId(), bean.getVersion()));
        return determineStatus(bean, contracts);
    }

    /**
     * @see io.apiman.manager.api.core.IApiValidator#isReady(io.apiman.manager.api.beans.apis.ApiVersionBean)
     */
    @Override
    public boolean isReady(ApiVersionBean api) {
        boolean ready = true;
        if (api.getEndpoint() == null || api.getEndpoint().trim().length() == 0) {
            ready = false;
        }
        if (api.getEndpointType() == null) {
            ready = false;
        }
        if (!api.isPublicAPI() && (api.getPlans() == null || api.getPlans().isEmpty())) {
            ready = false;
        }
        if (api.getGateways() == null || api.getGateways().isEmpty()) {
            ready = false;
        }
        return ready;
    }

    /**
     * @see io.apiman.manager.api.core.IApiValidator#getStatus(io.apiman.manager.api.beans.apis.ApiVersionBean, java.util.List)
     */
    @Override
    public ApiVersionStatusBean getStatus(ApiVersionBean api, List<PolicySummaryBean> policies) {
        ApiVersionStatusBean status = new ApiVersionStatusBean();
        status.setStatus(api.getStatus());

        // Why are we not yet "Ready"?
        if (api.getStatus() == ApiStatus.Created || api.getStatus() == ApiStatus.Ready) {
            // 1. Implementation endpoint + endpoint type
            /////////////////////////////////////////////
            StatusItemBean item = new StatusItemBean();
            item.setId("endpoint"); //$NON-NLS-1$
            item.setName(Messages.i18n.format("EntityValidator.endpoint.name")); //$NON-NLS-1$
            item.setDone(true);
            if (api.getEndpoint() == null || api.getEndpoint().trim().isEmpty() || api.getEndpointType() == null) {
                item.setDone(false);
                item.setRemediation(Messages.i18n.format("EntityValidator.endpoint.description")); //$NON-NLS-1$
            }
            status.getItems().add(item);

            // 2. Gateway selected
            item = new StatusItemBean();
            item.setId("gateways"); //$NON-NLS-1$
            item.setName(Messages.i18n.format("EntityValidator.gateways.name")); //$NON-NLS-1$
            item.setDone(true);
            if (api.getGateways() == null || api.getGateways().isEmpty()) {
                item.setDone(false);
                item.setRemediation(Messages.i18n.format("EntityValidator.gateways.description")); //$NON-NLS-1$
            }
            status.getItems().add(item);

            // 3. Public or at least one plan
            /////////////////////////////////
            item = new StatusItemBean();
            item.setId("plans"); //$NON-NLS-1$
            item.setName(Messages.i18n.format("EntityValidator.plans.name")); //$NON-NLS-1$
            item.setDone(true);
            if (!api.isPublicAPI() && (api.getPlans() == null || api.getPlans().isEmpty())) {
                item.setDone(false);
                item.setRemediation(Messages.i18n.format("EntityValidator.plans.description")); //$NON-NLS-1$
            }
            status.getItems().add(item);

            // 4. At least one Policy (optional)
            ////////////////////////////////////
            item = new StatusItemBean();
            item.setId("policies"); //$NON-NLS-1$
            item.setName(Messages.i18n.format("EntityValidator.policies.name")); //$NON-NLS-1$
            item.setDone(true);
            item.setOptional(true);
            if (policies.isEmpty()) {
                item.setDone(false);
                item.setRemediation(Messages.i18n.format("EntityValidator.policies.description")); //$NON-NLS-1$
            }
            status.getItems().add(item);
        }

        return status;
    }

    /**
     * @return the storageQuery
     */
    public IStorageQuery getStorageQuery() {
        return storageQuery;
    }

    /**
     * @param storageQuery the storageQuery to set
     */
    public void setStorageQuery(IStorageQuery storageQuery) {
        this.storageQuery = storageQuery;
    }

}
