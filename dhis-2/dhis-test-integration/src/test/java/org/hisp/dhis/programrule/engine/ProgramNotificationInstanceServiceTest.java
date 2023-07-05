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
package org.hisp.dhis.programrule.engine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.program.Enrollment;
import org.hisp.dhis.program.EnrollmentService;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.notification.NotificationTrigger;
import org.hisp.dhis.program.notification.ProgramNotificationInstance;
import org.hisp.dhis.program.notification.ProgramNotificationInstanceParam;
import org.hisp.dhis.program.notification.ProgramNotificationInstanceService;
import org.hisp.dhis.program.notification.ProgramNotificationRecipient;
import org.hisp.dhis.program.notification.ProgramNotificationTemplate;
import org.hisp.dhis.program.notification.ProgramNotificationTemplateService;
import org.hisp.dhis.programrule.ProgramRule;
import org.hisp.dhis.programrule.ProgramRuleAction;
import org.hisp.dhis.programrule.ProgramRuleActionType;
import org.hisp.dhis.test.integration.IntegrationTestBase;
import org.hisp.dhis.trackedentity.TrackedEntity;
import org.hisp.dhis.trackedentity.TrackedEntityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zubair Asghar
 */
class ProgramNotificationInstanceServiceTest extends IntegrationTestBase {

  private Program program;

  private ProgramRule programRule;

  private ProgramRuleAction programRuleAction;

  private ProgramNotificationTemplate programNotificationTemplate;

  private OrganisationUnit organisationUnit;

  private TrackedEntity trackedEntity;

  private Enrollment enrollment;

  private Enrollment enrollmentB;

  @Autowired private EnrollmentService enrollmentService;

  @Autowired private ProgramService programService;

  @Autowired private TrackedEntityService trackedEntityService;

  @Autowired private ProgramNotificationTemplateService programNotificationTemplateService;

  @Autowired private ProgramNotificationInstanceService programNotificationInstanceService;

  @Autowired private OrganisationUnitService organisationUnitService;

  @Autowired private IdentifiableObjectManager manager;

  @Autowired private ProgramRuleEngineService programRuleEngineService;

  @Override
  protected void setUpTest() throws Exception {
    organisationUnit = createOrganisationUnit('O');
    organisationUnitService.addOrganisationUnit(organisationUnit);
    program = createProgram('P');
    programService.addProgram(program);
    trackedEntity = createTrackedEntity('T', organisationUnit);
    trackedEntityService.addTrackedEntity(trackedEntity);
    programRule = createProgramRule('R', program);
    programRule.setCondition("true");
    manager.save(programRule);
    programNotificationTemplate =
        createProgramNotificationTemplate(
            "test", 1, NotificationTrigger.PROGRAM_RULE, ProgramNotificationRecipient.USER_GROUP);
    programNotificationTemplate.setAutoFields();
    programNotificationTemplateService.save(programNotificationTemplate);
    programRuleAction = createProgramRuleAction('A');
    programRuleAction.setProgramRuleActionType(ProgramRuleActionType.SCHEDULEMESSAGE);
    programRuleAction.setTemplateUid(programNotificationTemplate.getUid());
    programRuleAction.setData("'2020-12-12'");
    manager.save(programRuleAction);
    programRule.getProgramRuleActions().add(programRuleAction);
    manager.update(programRule);
    enrollment = createEnrollment(program, trackedEntity, organisationUnit);
    enrollmentService.addEnrollment(enrollment);
    enrollmentB = createEnrollment(program, trackedEntity, organisationUnit);
    enrollmentService.addEnrollment(enrollmentB);
  }

  @Test
  void testGetProgramNotificationInstance() {
    programRuleEngineService.evaluateEnrollmentAndRunEffects(enrollment.getId());
    List<ProgramNotificationInstance> programNotificationInstances =
        programNotificationInstanceService.getProgramNotificationInstances(
            ProgramNotificationInstanceParam.builder().enrollment(enrollment).build());
    assertFalse(programNotificationInstances.isEmpty());
    assertSame(enrollment, programNotificationInstances.get(0).getEnrollment());
    ProgramNotificationInstanceParam param =
        ProgramNotificationInstanceParam.builder().enrollment(enrollment).build();
    List<ProgramNotificationInstance> instances =
        programNotificationInstanceService.getProgramNotificationInstances(param);
    assertFalse(instances.isEmpty());
  }

  @Test
  void testDeleteProgramNotificationInstance() {
    programRuleEngineService.evaluateEnrollmentAndRunEffects(enrollmentB.getId());
    List<ProgramNotificationInstance> programNotificationInstances =
        programNotificationInstanceService.getProgramNotificationInstances(
            ProgramNotificationInstanceParam.builder().enrollment(enrollmentB).build());
    assertFalse(programNotificationInstances.isEmpty());
    assertSame(enrollmentB, programNotificationInstances.get(0).getEnrollment());
    programNotificationInstanceService.delete(programNotificationInstances.get(0));
    List<ProgramNotificationInstance> instances =
        programNotificationInstanceService.getProgramNotificationInstances(
            ProgramNotificationInstanceParam.builder().enrollment(enrollmentB).build());
    assertTrue(instances.isEmpty());
  }
}
