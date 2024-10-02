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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.category.CategoryService;
import org.hisp.dhis.common.OrganisationUnitSelectionMode;
import org.hisp.dhis.common.QueryFilter;
import org.hisp.dhis.common.QueryOperator;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.feedback.BadRequestException;
import org.hisp.dhis.feedback.ForbiddenException;
import org.hisp.dhis.feedback.NotFoundException;
import org.hisp.dhis.message.MessageSender;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.program.Enrollment;
import org.hisp.dhis.program.EnrollmentStatus;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.sms.command.SMSCommand;
import org.hisp.dhis.sms.command.SMSCommandService;
import org.hisp.dhis.sms.incoming.IncomingSms;
import org.hisp.dhis.sms.incoming.IncomingSmsService;
import org.hisp.dhis.sms.incoming.SmsMessageStatus;
import org.hisp.dhis.sms.listener.CommandSMSListener;
import org.hisp.dhis.sms.listener.SMSProcessingException;
import org.hisp.dhis.sms.parse.ParserType;
import org.hisp.dhis.smscompression.SmsResponse;
import org.hisp.dhis.system.util.SmsUtils;
import org.hisp.dhis.trackedentity.TrackedEntity;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeService;
import org.hisp.dhis.tracker.export.Page;
import org.hisp.dhis.tracker.export.PageParams;
import org.hisp.dhis.tracker.export.enrollment.EnrollmentService;
import org.hisp.dhis.tracker.export.trackedentity.TrackedEntityOperationParams;
import org.hisp.dhis.tracker.export.trackedentity.TrackedEntityService;
import org.hisp.dhis.tracker.imports.TrackerImportParams;
import org.hisp.dhis.tracker.imports.TrackerImportService;
import org.hisp.dhis.tracker.imports.TrackerImportStrategy;
import org.hisp.dhis.tracker.imports.domain.TrackerObjects;
import org.hisp.dhis.tracker.imports.report.ImportReport;
import org.hisp.dhis.tracker.imports.report.Status;
import org.hisp.dhis.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Created by zubair@dhis2.org on 11.08.17. */
@Slf4j
@Component("org.hisp.dhis.tracker.sms.ProgramStageDataEntrySMSListener")
@Transactional
public class ProgramStageDataEntrySMSListener extends CommandSMSListener {
  @Autowired JdbcTemplate jdbcTemplate;

  private static final String MORE_THAN_ONE_TE =
      "More than one tracked entity found for given phone number";

  private static final String NO_OU_FOUND = "No organisation unit found";

  private static final String NO_TE_EXIST = "No tracked entity exists with given phone number";

  private final TrackedEntityService trackedEntityService;

  private final TrackedEntityAttributeService trackedEntityAttributeService;

  private final SMSCommandService smsCommandService;

  private final EnrollmentService enrollmentService;

  private final TrackerImportService trackerImportService;

  public ProgramStageDataEntrySMSListener(
      CategoryService dataElementCategoryService,
      UserService userService,
      IncomingSmsService incomingSmsService,
      @Qualifier("smsMessageSender") MessageSender smsSender,
      TrackedEntityService trackedEntityService,
      TrackedEntityAttributeService trackedEntityAttributeService,
      SMSCommandService smsCommandService,
      EnrollmentService enrollmentService,
      TrackerImportService trackerImportService) {
    super(dataElementCategoryService, userService, incomingSmsService, smsSender);
    this.trackedEntityService = trackedEntityService;
    this.trackedEntityAttributeService = trackedEntityAttributeService;
    this.smsCommandService = smsCommandService;
    this.enrollmentService = enrollmentService;
    this.trackerImportService = trackerImportService;
  }

  @Override
  protected SMSCommand getSMSCommand(@Nonnull IncomingSms sms) {
    return smsCommandService.getSMSCommand(
        SmsUtils.getCommandString(sms), ParserType.PROGRAM_STAGE_DATAENTRY_PARSER);
  }

  @Override
  public void postProcess(
      @Nonnull IncomingSms sms,
      @Nonnull String username,
      @Nonnull SMSCommand smsCommand,
      @Nonnull Map<String, String> dataValues) {
    Set<OrganisationUnit> orgUnits = getOrganisationUnits(sms);
    List<TrackedEntity> trackedEntities = getTrackedEntityByPhoneNumber(sms, smsCommand, orgUnits);
    if (!validate(trackedEntities, orgUnits, sms)) {
      return;
    }
    TrackedEntity trackedEntity = trackedEntities.get(0);

    // TODO(ivo) create or update one enrollment but fail on multiple enrollments
    List<Enrollment> enrollments;
    try {
      enrollments =
          new ArrayList<>(
              enrollmentService.getEnrollments(
                  trackedEntity.getUid(), smsCommand.getProgram(), EnrollmentStatus.ACTIVE));
    } catch (BadRequestException | ForbiddenException | NotFoundException e) {
      // TODO(tracker) Find a better error message for these exceptions
      throw new SMSProcessingException(SmsResponse.UNKNOWN_ERROR);
    }

    if (enrollments.isEmpty()) {
      Enrollment enrollment = new Enrollment();
      enrollment.setEnrollmentDate(new Date());
      enrollment.setOccurredDate(new Date());
      enrollment.setProgram(smsCommand.getProgram());
      enrollment.setStatus(EnrollmentStatus.ACTIVE);

      // identifiableObjectManager.save(enrollment);

      enrollments.add(enrollment);
    } else if (enrollments.size() > 1) {
      update(sms, SmsMessageStatus.FAILED, false);

      sendFeedback(
          "Multiple active Enrollments exists for program: " + smsCommand.getProgram().getUid(),
          sms.getOriginator(),
          ERROR);

      return;
    }

    TrackerImportParams params =
        TrackerImportParams.builder().importStrategy(TrackerImportStrategy.CREATE).build();
    OrganisationUnit orgUnit = orgUnits.iterator().next();
    // TODO(ivo) also map tracked entity and enrollment
    TrackerObjects trackerObjects = null;
    // mapCommand(sms, smsCommand, dataValues, orgUnit, username, dataElementCategoryService);
    ImportReport importReport = trackerImportService.importTracker(params, trackerObjects);

    if (Status.OK == importReport.getStatus()) {
      update(sms, SmsMessageStatus.PROCESSED, true);
      sendFeedback(
          StringUtils.defaultIfEmpty(smsCommand.getSuccessMessage(), SMSCommand.SUCCESS_MESSAGE),
          sms.getOriginator(),
          INFO);
      return;
    }

    // TODO(DHIS2-18003) we need to map tracker import report errors/warnings to an sms
    log.error(
        "Failed to process SMS command {} of parser type EVENT_REGISTRATION_PARSER {}",
        smsCommand.getName(),
        importReport);
    throw new IllegalStateException(importReport.toString());
  }

  private List<TrackedEntity> getTrackedEntityByPhoneNumber(
      IncomingSms sms, SMSCommand command, Set<OrganisationUnit> ous) {
    List<TrackedEntityAttribute> attributes =
        trackedEntityAttributeService.getAllTrackedEntityAttributes().stream()
            .filter(attr -> attr.getValueType().equals(ValueType.PHONE_NUMBER))
            .toList();

    List<TrackedEntity> trackedEntities = new ArrayList<>();

    //    attributes.parallelStream()
    attributes.stream()
        .map(attr -> getParams(attr, sms, command.getProgram(), ous))
        .forEach(
            param -> {
              try {
                Page<TrackedEntity> page =
                    trackedEntityService.getTrackedEntities(param, new PageParams(1, 2, false));
                trackedEntities.addAll(page.getItems());
              } catch (BadRequestException | ForbiddenException | NotFoundException e) {
                // TODO(tracker) Find a better error message for these exceptions
                throw new SMSProcessingException(SmsResponse.UNKNOWN_ERROR);
              }
            });

    attributes.stream()
        .forEach(
            a -> {

              // full query made by listener
              String query =
                  """
  select TE.trackedentityid,
                    TE.uid,
                    TE.created,
                    TE.lastupdated,
                    TE.createdatclient,
                    TE.lastupdatedatclient,
                    TE.inactive,
                    TE.potentialduplicate,
                    TE.deleted,
                    TE.trackedentitytypeid
             FROM (SELECT DISTINCT TE.trackedentityid     as trackedentityid,
                                   TE.trackedentitytypeid as trackedentitytypeid,
                                   TE.uid                 as uid,
                                   TE.created             as created,
                                   TE.lastupdated         as lastupdated,
                                   TE.createdatclient     as createdatclient,
                                   TE.lastupdatedatclient as lastupdatedatclient,
                                   TE.inactive            as inactive,
                                   TE.potentialduplicate  as potentialduplicate,
                                   TE.deleted             as deleted
                   FROM trackedentity TE
                            INNER JOIN program P
                                       ON P.trackedentitytypeid = TE.trackedentitytypeid AND P.programid IN (%s)
                            INNER JOIN trackedentityattributevalue "rjK0coA4EWq"
                                       ON "rjK0coA4EWq".trackedentityattributeid = %s AND
                                          "rjK0coA4EWq".trackedentityid = TE.trackedentityid AND
                                          lower("rjK0coA4EWq".value) = '7654321'
                            LEFT JOIN trackedentityprogramowner PO
                                      ON PO.trackedentityid = TE.trackedentityid AND P.programid = PO.programid
                            INNER JOIN organisationunit OU ON OU.organisationunitid =
                                                              COALESCE(PO.organisationunitid, TE.organisationunitid)
                   where TE.trackedentitytypeid = %s
                     and TE.deleted IS FALSE
                   ORDER BY TE.trackedentityid desc
                   LIMIT 2 OFFSET 0) TE
             ORDER BY TE.trackedentityid desc
  """
                      .formatted(
                          command.getProgram().getId(),
                          a.getId(),
                          command.getProgram().getTrackedEntityType().getId());
              List<Map<String, Object>> dbResult = jdbcTemplate.queryForList(query);
            });

    return trackedEntities;
  }

  private boolean hasMoreThanOneEntity(List<TrackedEntity> trackedEntities) {
    return trackedEntities.size() > 1;
  }

  private TrackedEntityOperationParams getParams(
      TrackedEntityAttribute attribute,
      IncomingSms sms,
      Program program,
      Set<OrganisationUnit> ous) {

    QueryFilter queryFilter = new QueryFilter();
    queryFilter.setOperator(QueryOperator.EQ);
    queryFilter.setFilter(sms.getOriginator());

    return TrackedEntityOperationParams.builder()
        .filters(Map.of(attribute.getUid(), List.of(queryFilter)))
        //        .programUid(program.getUid())
        .trackedEntityTypeUid(program.getTrackedEntityType().getUid())
        //        .orgUnitMode(OrganisationUnitSelectionMode.ACCESSIBLE)
        .orgUnitMode(OrganisationUnitSelectionMode.ALL)
        //        .organisationUnits(
        //            ous.stream().map(BaseIdentifiableObject::getUid).collect(Collectors.toSet()))
        .build();
  }

  private boolean validate(
      List<TrackedEntity> trackedEntities, Set<OrganisationUnit> ous, IncomingSms sms) {
    if (trackedEntities == null || trackedEntities.isEmpty()) {
      sendFeedback(NO_TE_EXIST, sms.getOriginator(), ERROR);
      return false;
    }

    if (hasMoreThanOneEntity(trackedEntities)) {
      sendFeedback(MORE_THAN_ONE_TE, sms.getOriginator(), ERROR);
      return false;
    }

    if (validateOrganisationUnits(ous)) {
      sendFeedback(NO_OU_FOUND, sms.getOriginator(), ERROR);
      return false;
    }

    return true;
  }

  private boolean validateOrganisationUnits(Set<OrganisationUnit> ous) {
    return ous == null || ous.isEmpty();
  }
}
