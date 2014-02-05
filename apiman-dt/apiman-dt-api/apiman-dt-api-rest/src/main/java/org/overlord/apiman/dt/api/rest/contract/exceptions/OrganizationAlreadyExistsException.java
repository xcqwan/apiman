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

package org.overlord.apiman.dt.api.rest.contract.exceptions;


/**
 * Thrown when trying to create an Organization that already exists.
 *
 * @author eric.wittmann@redhat.com
 */
public class OrganizationAlreadyExistsException extends AbstractAlreadyExistsException {

    private static final long serialVersionUID = -1444829046948798598L;

    /**
     * Creates an exception from an organization name.
     * @param organizationName
     */
    public static final OrganizationAlreadyExistsException create(String organizationName) {
        return new OrganizationAlreadyExistsException("Organization already exists: " + organizationName);
    }
    
    /**
     * Constructor.
     */
    public OrganizationAlreadyExistsException() {
    }
    
    /**
     * Constructor.
     * @param message
     */
    public OrganizationAlreadyExistsException(String message) {
        super(message);
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.exceptions.AbstractRestException#getErrorCode()
     */
    @Override
    public int getErrorCode() {
        return ErrorCodes.ORG_ALREADY_EXISTS;
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.exceptions.AbstractRestException#getMoreInfo()
     */
    @Override
    public String getMoreInfoUrl() {
        return ErrorCodes.ORG_ALREADY_EXISTS_INFO;
    }

}
