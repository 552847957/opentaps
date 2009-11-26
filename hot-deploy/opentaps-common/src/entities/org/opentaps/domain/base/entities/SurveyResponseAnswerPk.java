package org.opentaps.domain.base.entities;

/*
* Copyright (c) 2008 - 2009 Open Source Strategies, Inc.
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

// DO NOT EDIT THIS FILE!  THIS IS AUTO GENERATED AND WILL GET WRITTEN OVER PERIODICALLY WHEN THE DATA MODEL CHANGES
// EXTEND THIS CLASS INSTEAD.

import java.io.Serializable;
import javax.persistence.*;

import java.lang.String;

@Embeddable
public class SurveyResponseAnswerPk implements Serializable {
    @Column(name="SURVEY_RESPONSE_ID")
    private String surveyResponseId;
    @Column(name="SURVEY_QUESTION_ID")
    private String surveyQuestionId;
    @Column(name="SURVEY_MULTI_RESP_COL_ID")
    private String surveyMultiRespColId;
    
    /**
     * Auto generated value setter.
     * @param surveyResponseId the surveyResponseId to set
     */
    public void setSurveyResponseId(String surveyResponseId) {
        this.surveyResponseId = surveyResponseId;
    }
    /**
     * Auto generated value setter.
     * @param surveyQuestionId the surveyQuestionId to set
     */
    public void setSurveyQuestionId(String surveyQuestionId) {
        this.surveyQuestionId = surveyQuestionId;
    }
    /**
     * Auto generated value setter.
     * @param surveyMultiRespColId the surveyMultiRespColId to set
     */
    public void setSurveyMultiRespColId(String surveyMultiRespColId) {
        this.surveyMultiRespColId = surveyMultiRespColId;
    }

    /**
     * Auto generated value accessor.
     * @return <code>String</code>
     */  
    public String getSurveyResponseId() {
        return this.surveyResponseId;
    }
    /**
     * Auto generated value accessor.
     * @return <code>String</code>
     */  
    public String getSurveyQuestionId() {
        return this.surveyQuestionId;
    }
    /**
     * Auto generated value accessor.
     * @return <code>String</code>
     */  
    public String getSurveyMultiRespColId() {
        return this.surveyMultiRespColId;
    }
}