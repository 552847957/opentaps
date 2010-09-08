/*
 * Copyright (c) opentaps Group LLC
 *
 * Opentaps is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Opentaps is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Opentaps.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opentaps.domain.activities;

import java.util.List;

import org.opentaps.domain.party.Party;
import org.opentaps.foundation.entity.EntityNotFoundException;
import org.opentaps.foundation.repository.RepositoryException;

/**
 * Repository for Activities to handle interaction of Activities-related domain with
 * the entity engine (database) and the service engine.
 */
public interface ActivityRepositoryInterface {

    /**
     * Finds the Activity with the given Id.
     *
     * @param activityId Activity identifier
     * @return an instance of Activity
     * @throws RepositoryException, EntityNotFoundException if an error occurs
     */
    public Activity getActivityById(String activityId) throws RepositoryException, EntityNotFoundException;

    /**
     * Finds the List of the completed Activities
     *
     * @return the list of Activity
     * @throws RepositoryException if an error occurs
     */
    public List<Activity> getCompletedActivities() throws RepositoryException;
    
    /**
     * Create ActivityFact with target party id and member party id 
     * 
     * @param teamMemberPartyId the target party identifier
     * @param targetPartyId the target party identifier
     * @param teamMemberRoleTypeId the team member role type identifier
     * @param targetRoleTypeId the target role type identifier
     * @param activity the current activity
     * @throws RepositoryException if an error occurs
     */
    public void createActivityFact(String teamMemberPartyId, String targetPartyId, String teamMemberRoleTypeId, String targetRoleTypeId, Activity activity) throws RepositoryException;
    
    /**
     * Finds the Participants with the given WorkEffort Id.
     * 
     * @param workEffortId WorkEffort identifier
     * @return the list of Party
     * @throws RepositoryException if an error occurs
     * @throws EntityNotFoundException if an error occurs
     */
    public List<Party> getParticipants(String workEffortId) throws RepositoryException, EntityNotFoundException;
}
