package io.apiman.manager.api.rest;

import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.developers.DeveloperApiPlanSummaryDto;
import io.apiman.manager.api.beans.developers.ApiVersionPolicySummaryDto;
import io.apiman.manager.api.beans.orgs.NewOrganizationBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.beans.summary.ApiVersionSummaryBean;
import io.apiman.manager.api.rest.exceptions.ApiVersionNotFoundException;
import io.apiman.manager.api.rest.exceptions.InvalidSearchCriteriaException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.OrganizationAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.OrganizationNotFoundException;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Developer portal resources.
 * <p>
 * Many of the endpoints have more permissive access than the main API, but may provide more limited data
 * for security and privacy reasons.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Path("devportal")
public interface IDeveloperPortalResource {
    @POST
    @Path("apis/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SearchResultsBean<ApiSummaryBean> searchExposedApis(SearchCriteriaBean criteria)
            throws OrganizationNotFoundException, InvalidSearchCriteriaException;

    @GET
    @Path("apis/featured")
    @Produces(MediaType.APPLICATION_JSON)
    List<ApiSummaryBean> getFeaturedApis();

    @POST
    @Path("organizations")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    OrganizationBean createHomeOrgForDeveloper(NewOrganizationBean newOrg)
            throws OrganizationAlreadyExistsException;

    @GET
    @Path("organizations/{orgId}/apis/{apiId}/versions/")
    List<ApiVersionSummaryBean> listApiVersions(@PathParam("orgId") String orgId, @PathParam("apiId") String apiId);

    @GET
    @Path("organizations/{orgId}/apis/{apiId}/versions/{apiVersion}")
    @Produces(MediaType.APPLICATION_JSON)
    ApiVersionBean getApiVersion(@PathParam("orgId") String orgId, @PathParam("apiId") String apiId, @PathParam("apiVersion") String apiVersion)
            throws ApiVersionNotFoundException;

    @GET
    @Path("organizations/{orgId}/apis/{apiId}/versions/{apiVersion}/plans")
    @Produces(MediaType.APPLICATION_JSON)
    List<DeveloperApiPlanSummaryDto> getApiVersionPlans(@PathParam("orgId") String orgId, @PathParam("apiId") String apiId, @PathParam("apiVersion") String apiVersion)
            throws ApiVersionNotFoundException;

    @GET
    @Path("organizations/{orgId}/apis/{apiId}/versions/{apiVersion}/policies")
    @Produces(MediaType.APPLICATION_JSON)
    List<ApiVersionPolicySummaryDto> listApiPolicies(@PathParam("orgId") String orgId, @PathParam("apiId") String apiId, @PathParam("apiVersion") String apiVersion)
            throws OrganizationNotFoundException, ApiVersionNotFoundException, NotAuthorizedException;

    @GET
    @Path("organizations/{orgId}/apis/{apiId}/versions/{apiVersion}/definition")
    @Produces({ MediaType.APPLICATION_JSON, "application/wsdl+xml", "application/x-yaml" })
    Response getApiDefinition(@PathParam("orgId") String orgId, @PathParam("apiId") String apiId, @PathParam("apiVersion") String apiVersion)
            throws ApiVersionNotFoundException;
}