/*
 * Copyright (c) 2004-2022, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.tracker.imports.sms;

import java.util.List;
import org.hisp.dhis.category.CategoryService;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.message.MessageSender;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.relationship.RelationshipConstraint;
import org.hisp.dhis.relationship.RelationshipType;
import org.hisp.dhis.relationship.RelationshipTypeService;
import org.hisp.dhis.sms.incoming.IncomingSms;
import org.hisp.dhis.sms.incoming.IncomingSmsService;
import org.hisp.dhis.sms.listener.CompressionSMSListener;
import org.hisp.dhis.sms.listener.SMSProcessingException;
import org.hisp.dhis.smscompression.SmsConsts.SubmissionType;
import org.hisp.dhis.smscompression.SmsResponse;
import org.hisp.dhis.smscompression.models.RelationshipSmsSubmission;
import org.hisp.dhis.smscompression.models.SmsSubmission;
import org.hisp.dhis.smscompression.models.Uid;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeService;
import org.hisp.dhis.trackedentity.TrackedEntityTypeService;
import org.hisp.dhis.tracker.imports.TrackerImportParams;
import org.hisp.dhis.tracker.imports.TrackerImportService;
import org.hisp.dhis.tracker.imports.TrackerImportStrategy;
import org.hisp.dhis.tracker.imports.domain.MetadataIdentifier;
import org.hisp.dhis.tracker.imports.domain.Relationship;
import org.hisp.dhis.tracker.imports.domain.RelationshipItem;
import org.hisp.dhis.tracker.imports.domain.TrackerObjects;
import org.hisp.dhis.tracker.imports.report.ImportReport;
import org.hisp.dhis.tracker.imports.report.Status;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("org.hisp.dhis.tracker.sms.RelationshipSMSListener")
@Transactional
public class RelationshipSMSListener extends CompressionSMSListener {
  private final RelationshipTypeService relationshipTypeService;

  private final TrackerImportService trackerImportService;

  public RelationshipSMSListener(
      IncomingSmsService incomingSmsService,
      @Qualifier("smsMessageSender") MessageSender smsSender,
      UserService userService,
      TrackedEntityTypeService trackedEntityTypeService,
      TrackedEntityAttributeService trackedEntityAttributeService,
      ProgramService programService,
      OrganisationUnitService organisationUnitService,
      CategoryService categoryService,
      DataElementService dataElementService,
      IdentifiableObjectManager identifiableObjectManager,
      RelationshipTypeService relationshipTypeService,
      TrackerImportService trackerImportService) {
    super(
        incomingSmsService,
        smsSender,
        userService,
        trackedEntityTypeService,
        trackedEntityAttributeService,
        programService,
        organisationUnitService,
        categoryService,
        dataElementService,
        identifiableObjectManager);
    this.relationshipTypeService = relationshipTypeService;
    this.trackerImportService = trackerImportService;
  }

  @Override
  protected SmsResponse postProcess(IncomingSms sms, SmsSubmission submission, User user)
      throws SMSProcessingException {
    RelationshipSmsSubmission subm = (RelationshipSmsSubmission) submission;

    RelationshipType relType =
        relationshipTypeService.getRelationshipType(subm.getRelationshipType().getUid());
    if (relType == null) {
      throw new SMSProcessingException(SmsResponse.INVALID_RELTYPE.set(subm.getRelationshipType()));
    }

    TrackerImportParams params =
        TrackerImportParams.builder().importStrategy(TrackerImportStrategy.CREATE).build();
    TrackerObjects trackerObjects =
        TrackerObjects.builder()
            .relationships(
                List.of(
                    Relationship.builder()
                        .relationshipType(MetadataIdentifier.ofUid(relType))
                        .relationship(
                            subm.getRelationship() != null ? subm.getRelationship().getUid() : null)
                        .from(relationshipItem(relType.getFromConstraint(), subm.getFrom()))
                        .to(relationshipItem(relType.getToConstraint(), subm.getTo()))
                        .build()))
            .build();
    ImportReport importReport = trackerImportService.importTracker(params, trackerObjects);

    if (Status.OK == importReport.getStatus()) {
      return SmsResponse.SUCCESS;
    }

    // TODO(DHIS2-18003) we need to map tracker import report errors/warnings to an sms
    return SmsResponse.UNKNOWN_ERROR;
  }

  private RelationshipItem relationshipItem(RelationshipConstraint constraint, Uid uid) {
    return switch (constraint.getRelationshipEntity()) {
      case TRACKED_ENTITY_INSTANCE ->
          RelationshipItem.builder().trackedEntity(uid.getUid()).build();
      case PROGRAM_INSTANCE -> RelationshipItem.builder().enrollment(uid.getUid()).build();
      case PROGRAM_STAGE_INSTANCE -> RelationshipItem.builder().event(uid.getUid()).build();
    };
  }

  @Override
  protected boolean handlesType(SubmissionType type) {
    return (type == SubmissionType.RELATIONSHIP);
  }
}
