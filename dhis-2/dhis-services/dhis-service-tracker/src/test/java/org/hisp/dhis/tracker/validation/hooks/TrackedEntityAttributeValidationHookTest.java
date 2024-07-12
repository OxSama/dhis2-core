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
package org.hisp.dhis.tracker.validation.hooks;

import static org.hisp.dhis.DhisConvenienceTest.createTrackedEntityAttributeValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.encryption.EncryptionStatus;
import org.hisp.dhis.external.conf.DhisConfigurationProvider;
import org.hisp.dhis.fileresource.FileResource;
import org.hisp.dhis.option.Option;
import org.hisp.dhis.option.OptionSet;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityType;
import org.hisp.dhis.trackedentity.TrackedEntityTypeAttribute;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;
import org.hisp.dhis.tracker.TrackerIdSchemeParams;
import org.hisp.dhis.tracker.TrackerImportStrategy;
import org.hisp.dhis.tracker.TrackerType;
import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Attribute;
import org.hisp.dhis.tracker.domain.MetadataIdentifier;
import org.hisp.dhis.tracker.domain.TrackedEntity;
import org.hisp.dhis.tracker.preheat.TrackerPreheat;
import org.hisp.dhis.tracker.report.TrackerErrorCode;
import org.hisp.dhis.tracker.util.Constant;
import org.hisp.dhis.tracker.validation.ValidationErrorReporter;
import org.hisp.dhis.tracker.validation.service.attribute.TrackedAttributeValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * @author Luca Cambi <luca@dhis2.org>
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class TrackedEntityAttributeValidationHookTest {
  private static final String TE_UID = "fdsf9e90rei";

  private static final String MANDATORY_ATTRIBUTE_UID = "cFr94jf03AA";

  @InjectMocks private TrackedEntityAttributeValidationHook trackedEntityAttributeValidationHook;

  @Mock private TrackedAttributeValidationService teAttrService;

  @Mock private TrackerPreheat preheat;

  @Mock private DhisConfigurationProvider dhisConfigurationProvider;

  private TrackerBundle bundle;

  private ValidationErrorReporter reporter;

  private TrackerIdSchemeParams idSchemes;

  @BeforeEach
  public void setUp() {
    bundle = TrackerBundle.builder().preheat(preheat).build();
    idSchemes = TrackerIdSchemeParams.builder().build();
    when(preheat.getIdSchemes()).thenReturn(idSchemes);
    reporter = new ValidationErrorReporter(idSchemes);
    when(dhisConfigurationProvider.getEncryptionStatus()).thenReturn(EncryptionStatus.OK);
  }

  @Test
  void shouldPassValidation() {
    TrackedEntityAttribute trackedEntityAttribute = new TrackedEntityAttribute();
    trackedEntityAttribute.setUid("uid");
    trackedEntityAttribute.setValueType(ValueType.TEXT);

    when(preheat.getTrackedEntityAttribute((MetadataIdentifier) any()))
        .thenReturn(trackedEntityAttribute);
    when(preheat.getTrackedEntityType((MetadataIdentifier) any()))
        .thenReturn(new TrackedEntityType());

    TrackedEntity trackedEntity =
        TrackedEntity.builder()
            .attributes(
                Collections.singletonList(
                    Attribute.builder()
                        .attribute(MetadataIdentifier.ofUid("uid"))
                        .value("value")
                        .build()))
            .trackedEntityType(MetadataIdentifier.ofUid("trackedEntityType"))
            .build();

    trackedEntityAttributeValidationHook.validateTrackedEntity(reporter, bundle, trackedEntity);

    assertFalse(reporter.hasErrors());
    assertEquals(0, reporter.getErrors().size());
  }

  @Test
  void shouldFailValidationWhenCreatingTEAndThereAreMissingMandatoryFields() {
    String tet = "tet";

    TrackedEntityType trackedEntityType = new TrackedEntityType();
    TrackedEntityTypeAttribute trackedEntityTypeAttribute = new TrackedEntityTypeAttribute();
    trackedEntityTypeAttribute.setMandatory(true);

    TrackedEntityAttribute trackedEntityAttribute = new TrackedEntityAttribute();
    trackedEntityAttribute.setUid("c");
    trackedEntityTypeAttribute.setTrackedEntityAttribute(trackedEntityAttribute);

    trackedEntityType.setTrackedEntityTypeAttributes(
        Collections.singletonList(trackedEntityTypeAttribute));

    when(preheat.getTrackedEntityType(MetadataIdentifier.ofUid(tet))).thenReturn(trackedEntityType);

    TrackedEntity trackedEntity =
        TrackedEntity.builder()
            .trackedEntityType(MetadataIdentifier.ofUid(tet))
            .attributes(
                Arrays.asList(
                    Attribute.builder()
                        .attribute(MetadataIdentifier.ofUid("a"))
                        .value("value")
                        .build(),
                    Attribute.builder()
                        .attribute(MetadataIdentifier.ofUid("b"))
                        .value("value")
                        .build()))
            .build();

    TrackedEntityAttribute contextAttribute = new TrackedEntityAttribute();
    contextAttribute.setUid("uid");
    contextAttribute.setValueType(ValueType.TEXT);

    when(preheat.getTrackedEntityAttribute((MetadataIdentifier) any()))
        .thenReturn(contextAttribute);
    bundle.setStrategy(trackedEntity, TrackerImportStrategy.CREATE);

    trackedEntityAttributeValidationHook.validateTrackedEntity(reporter, bundle, trackedEntity);

    assertTrue(reporter.hasErrors());
    assertEquals(1, reporter.getErrors().size());
    assertEquals(
        1,
        reporter.getErrors().stream()
            .filter(e -> e.getErrorCode() == TrackerErrorCode.E1090)
            .count());
  }

  @Test
  void shouldPassValidationWhenUpdatingTEAndPayloadIsMissingMandatoryFieldButItIsPresentInDB() {
    String tet = "tet";

    TrackedEntityType trackedEntityType = new TrackedEntityType();
    TrackedEntityTypeAttribute trackedEntityTypeAttribute = new TrackedEntityTypeAttribute();
    trackedEntityTypeAttribute.setMandatory(true);

    TrackedEntityAttribute trackedEntityAttribute = new TrackedEntityAttribute();
    trackedEntityAttribute.setUid(MANDATORY_ATTRIBUTE_UID);
    trackedEntityTypeAttribute.setTrackedEntityAttribute(trackedEntityAttribute);

    trackedEntityType.setTrackedEntityTypeAttributes(
        Collections.singletonList(trackedEntityTypeAttribute));

    when(preheat.getTrackedEntityType(MetadataIdentifier.ofUid(tet))).thenReturn(trackedEntityType);
    when(preheat.getTrackedEntity(TE_UID)).thenReturn(trackedEntity(trackedEntityAttribute));

    TrackedEntity trackedEntity =
        TrackedEntity.builder()
            .trackedEntity(TE_UID)
            .trackedEntityType(MetadataIdentifier.ofUid(tet))
            .attributes(
                Arrays.asList(
                    Attribute.builder()
                        .attribute(MetadataIdentifier.ofUid("a"))
                        .value("value")
                        .build(),
                    Attribute.builder()
                        .attribute(MetadataIdentifier.ofUid("b"))
                        .value("value")
                        .build()))
            .build();

    TrackedEntityAttribute contextAttribute = new TrackedEntityAttribute();
    contextAttribute.setUid("uid");
    contextAttribute.setValueType(ValueType.TEXT);

    when(preheat.getTrackedEntityAttribute((MetadataIdentifier) any()))
        .thenReturn(contextAttribute);
    bundle.setStrategy(trackedEntity, TrackerImportStrategy.UPDATE);

    trackedEntityAttributeValidationHook.validateTrackedEntity(reporter, bundle, trackedEntity);

    assertFalse(reporter.hasErrors());
  }

  @Test
  void shouldFailValidationWhenUpdatingTEAndPayloadIsMissingMandatoryFieldAndItIsNotPresentInDB() {
    String tet = "tet";

    TrackedEntityType trackedEntityType = new TrackedEntityType();
    TrackedEntityTypeAttribute trackedEntityTypeAttribute = new TrackedEntityTypeAttribute();
    trackedEntityTypeAttribute.setMandatory(true);

    TrackedEntityAttribute trackedEntityAttribute = new TrackedEntityAttribute();
    trackedEntityAttribute.setUid(MANDATORY_ATTRIBUTE_UID);
    trackedEntityTypeAttribute.setTrackedEntityAttribute(trackedEntityAttribute);

    trackedEntityType.setTrackedEntityTypeAttributes(
        Collections.singletonList(trackedEntityTypeAttribute));

    when(preheat.getTrackedEntityType(MetadataIdentifier.ofUid(tet))).thenReturn(trackedEntityType);
    when(preheat.getTrackedEntity(TE_UID)).thenReturn(trackedEntity());

    TrackedEntity trackedEntity =
        TrackedEntity.builder()
            .trackedEntity(TE_UID)
            .trackedEntityType(MetadataIdentifier.ofUid(tet))
            .attributes(
                Arrays.asList(
                    Attribute.builder()
                        .attribute(MetadataIdentifier.ofUid("a"))
                        .value("value")
                        .build(),
                    Attribute.builder()
                        .attribute(MetadataIdentifier.ofUid("b"))
                        .value("value")
                        .build()))
            .build();

    TrackedEntityAttribute contextAttribute = new TrackedEntityAttribute();
    contextAttribute.setUid("uid");
    contextAttribute.setValueType(ValueType.TEXT);

    when(preheat.getTrackedEntityAttribute((MetadataIdentifier) any()))
        .thenReturn(contextAttribute);
    bundle.setStrategy(trackedEntity, TrackerImportStrategy.UPDATE);
    bundle.setTrackedEntities(List.of(trackedEntity));

    trackedEntityAttributeValidationHook.validateTrackedEntity(reporter, bundle, trackedEntity);

    assertEquals(1, reporter.getErrors().size());
    assertEquals(
        1,
        reporter.getErrors().stream()
            .filter(e -> e.getErrorCode() == TrackerErrorCode.E1090)
            .count());
  }

  @Test
  void shouldFailValidationMissingTea() {
    TrackedEntity trackedEntity =
        TrackedEntity.builder()
            .attributes(
                Arrays.asList(
                    Attribute.builder().attribute(MetadataIdentifier.ofUid("aaaaa")).build(),
                    Attribute.builder().attribute(MetadataIdentifier.ofUid("bbbbb")).build()))
            .trackedEntityType(MetadataIdentifier.ofUid("tet"))
            .build();

    when(preheat.getTrackedEntityType((MetadataIdentifier) any()))
        .thenReturn(new TrackedEntityType());

    trackedEntityAttributeValidationHook.validateTrackedEntity(reporter, bundle, trackedEntity);

    assertTrue(reporter.hasErrors());
    assertEquals(2, reporter.getErrors().size());
    assertEquals(
        2,
        reporter.getErrors().stream()
            .filter(e -> e.getErrorCode() == TrackerErrorCode.E1006)
            .count());
  }

  @Test
  void shouldFailWhenCreatingTEAndMandatoryAttributeIsSetToDeleted() {
    String tea = MANDATORY_ATTRIBUTE_UID;
    String tet = "tet";

    TrackedEntity trackedEntity =
        TrackedEntity.builder()
            .trackedEntity(TE_UID)
            .attributes(
                Collections.singletonList(
                    Attribute.builder().attribute(MetadataIdentifier.ofUid(tea)).build()))
            .trackedEntityType(MetadataIdentifier.ofUid(tet))
            .build();

    TrackedEntityType trackedEntityType = new TrackedEntityType();
    TrackedEntityTypeAttribute trackedEntityTypeAttribute = new TrackedEntityTypeAttribute();
    trackedEntityTypeAttribute.setMandatory(true);

    TrackedEntityAttribute trackedEntityAttribute = new TrackedEntityAttribute();
    trackedEntityAttribute.setUid(tea);
    trackedEntityTypeAttribute.setTrackedEntityAttribute(trackedEntityAttribute);

    trackedEntityType.setTrackedEntityTypeAttributes(
        Collections.singletonList(trackedEntityTypeAttribute));

    when(preheat.getTrackedEntityType(MetadataIdentifier.ofUid(tet))).thenReturn(trackedEntityType);
    when(preheat.getTrackedEntityAttribute(MetadataIdentifier.ofUid(tea)))
        .thenReturn(trackedEntityAttribute);
    bundle.setStrategy(trackedEntity, TrackerImportStrategy.CREATE);
    bundle.setTrackedEntities(List.of(trackedEntity));

    trackedEntityAttributeValidationHook.validateTrackedEntity(reporter, bundle, trackedEntity);

    assertTrue(reporter.hasErrors());
    assertEquals(1, reporter.getErrors().size());
    assertEquals(
        1,
        reporter.getErrors().stream()
            .filter(e -> e.getErrorCode() == TrackerErrorCode.E1076)
            .count());
  }

  @Test
  void shouldFailWhenUpdatingTEAndMandatoryAttributeIsSetToDeleted() {
    String tea = MANDATORY_ATTRIBUTE_UID;
    String tet = "tet";

    TrackedEntity trackedEntity =
        TrackedEntity.builder()
            .attributes(
                Collections.singletonList(
                    Attribute.builder().attribute(MetadataIdentifier.ofUid(tea)).build()))
            .trackedEntityType(MetadataIdentifier.ofUid(tet))
            .build();

    TrackedEntityType trackedEntityType = new TrackedEntityType();
    TrackedEntityTypeAttribute trackedEntityTypeAttribute = new TrackedEntityTypeAttribute();
    trackedEntityTypeAttribute.setMandatory(true);

    TrackedEntityAttribute trackedEntityAttribute = new TrackedEntityAttribute();
    trackedEntityAttribute.setUid(tea);
    trackedEntityTypeAttribute.setTrackedEntityAttribute(trackedEntityAttribute);

    trackedEntityType.setTrackedEntityTypeAttributes(
        Collections.singletonList(trackedEntityTypeAttribute));

    when(preheat.getTrackedEntityType(MetadataIdentifier.ofUid(tet))).thenReturn(trackedEntityType);
    when(preheat.getTrackedEntityAttribute(MetadataIdentifier.ofUid(tea)))
        .thenReturn(trackedEntityAttribute);
    bundle.setTrackedEntities(List.of(trackedEntity));

    bundle.setStrategy(trackedEntity, TrackerImportStrategy.UPDATE);
    trackedEntityAttributeValidationHook.validateTrackedEntity(reporter, bundle, trackedEntity);

    assertTrue(reporter.hasErrors());
    assertEquals(1, reporter.getErrors().size());
    assertEquals(
        1,
        reporter.getErrors().stream()
            .filter(e -> e.getErrorCode() == TrackerErrorCode.E1076)
            .count());
  }

  @Test
  void shouldFailValueTooLong() {
    TrackedEntityAttribute trackedEntityAttribute = new TrackedEntityAttribute();
    trackedEntityAttribute.setValueType(ValueType.TEXT);

    StringBuilder sbString = new StringBuilder();

    for (int i = 0; i < Constant.MAX_ATTR_VALUE_LENGTH + 1; i++) {
      sbString.append("a");
    }

    TrackedEntity te = TrackedEntity.builder().trackedEntity(CodeGenerator.generateUid()).build();
    trackedEntityAttributeValidationHook.validateAttributeValue(
        reporter, te, trackedEntityAttribute, sbString.toString());

    assertTrue(reporter.hasErrors());
    assertEquals(1, reporter.getErrors().size());
    assertTrue(
        reporter.hasErrorReport(
            err ->
                TrackerErrorCode.E1077.equals(err.getErrorCode())
                    && TrackerType.TRACKED_ENTITY.equals(err.getTrackerType())
                    && te.getTrackedEntity().equals(err.getUid())));
  }

  @Test
  void shouldFailDataValueIsValid() {
    TrackedEntityAttribute trackedEntityAttribute = new TrackedEntityAttribute();
    trackedEntityAttribute.setValueType(ValueType.NUMBER);

    TrackedEntity te = TrackedEntity.builder().trackedEntity(CodeGenerator.generateUid()).build();
    trackedEntityAttributeValidationHook.validateAttributeValue(
        reporter, te, trackedEntityAttribute, "value");

    assertTrue(reporter.hasErrors());
    assertEquals(1, reporter.getErrors().size());
    assertTrue(
        reporter.hasErrorReport(
            err ->
                TrackerErrorCode.E1085.equals(err.getErrorCode())
                    && TrackerType.TRACKED_ENTITY.equals(err.getTrackerType())
                    && te.getTrackedEntity().equals(err.getUid())));
  }

  @Test
  void shouldFailEncryptionStatus() {
    TrackedEntityAttribute trackedEntityAttribute = new TrackedEntityAttribute();
    trackedEntityAttribute.setValueType(ValueType.AGE);
    trackedEntityAttribute.setConfidential(true);

    when(dhisConfigurationProvider.getEncryptionStatus())
        .thenReturn(EncryptionStatus.ENCRYPTION_PASSWORD_TOO_SHORT);
    when(dhisConfigurationProvider.getProperty(any())).thenReturn("property");

    when(preheat.getTrackedEntityAttribute((MetadataIdentifier) any()))
        .thenReturn(trackedEntityAttribute);

    TrackedEntity te = TrackedEntity.builder().trackedEntity(CodeGenerator.generateUid()).build();
    trackedEntityAttributeValidationHook.validateAttributeValue(
        reporter, te, trackedEntityAttribute, "value");

    assertTrue(reporter.hasErrors());
    assertEquals(1, reporter.getErrors().size());
    assertTrue(
        reporter.hasErrorReport(
            err ->
                TrackerErrorCode.E1112.equals(err.getErrorCode())
                    && TrackerType.TRACKED_ENTITY.equals(err.getTrackerType())
                    && te.getTrackedEntity().equals(err.getUid())));
  }

  @Test
  void shouldFailOptionSetNotValid() {
    TrackedEntityAttribute trackedEntityAttribute = getTrackedEntityAttributeWithOptionSet();

    when(preheat.getTrackedEntityAttribute((MetadataIdentifier) any()))
        .thenReturn(trackedEntityAttribute);
    when(preheat.getTrackedEntityType((MetadataIdentifier) any()))
        .thenReturn(new TrackedEntityType());

    TrackedEntity trackedEntity =
        TrackedEntity.builder()
            .attributes(
                Collections.singletonList(
                    Attribute.builder()
                        .attribute(MetadataIdentifier.ofUid("uid"))
                        .value("wrongCode")
                        .build()))
            .trackedEntityType(MetadataIdentifier.ofUid("trackedEntityType"))
            .build();

    trackedEntityAttributeValidationHook.validateTrackedEntity(reporter, bundle, trackedEntity);

    assertTrue(reporter.hasErrors());
    assertEquals(1, reporter.getErrors().size());
    assertEquals(
        1,
        reporter.getErrors().stream()
            .filter(e -> e.getErrorCode() == TrackerErrorCode.E1125)
            .count());
  }

  @Test
  void shouldPassValidationValueInOptionSet() {
    TrackedEntityAttribute trackedEntityAttribute = getTrackedEntityAttributeWithOptionSet();

    when(preheat.getTrackedEntityAttribute((MetadataIdentifier) any()))
        .thenReturn(trackedEntityAttribute);
    when(preheat.getTrackedEntityType((MetadataIdentifier) any()))
        .thenReturn(new TrackedEntityType());

    TrackedEntity trackedEntity =
        TrackedEntity.builder()
            .attributes(
                Collections.singletonList(
                    Attribute.builder()
                        .attribute(MetadataIdentifier.ofUid("trackedEntity"))
                        .value("CODE")
                        .build()))
            .trackedEntityType(MetadataIdentifier.ofUid("trackedEntityType"))
            .build();

    trackedEntityAttributeValidationHook.validateTrackedEntity(reporter, bundle, trackedEntity);

    assertFalse(reporter.hasErrors());
    assertEquals(0, reporter.getErrors().size());
  }

  @Test
  void shouldPassValidationWhenValueIsNullAndAttributeIsNotMandatory() {
    TrackedEntityTypeAttribute trackedEntityTypeAttribute = new TrackedEntityTypeAttribute();

    TrackedEntityAttribute trackedEntityAttribute = new TrackedEntityAttribute();
    trackedEntityAttribute.setUid("trackedEntityAttribute");
    trackedEntityAttribute.setValueType(ValueType.TEXT);
    trackedEntityTypeAttribute.setTrackedEntityAttribute(trackedEntityAttribute);

    TrackedEntityType trackedEntityType = new TrackedEntityType();
    trackedEntityType.setTrackedEntityTypeAttributes(
        Collections.singletonList(trackedEntityTypeAttribute));

    when(preheat.getTrackedEntityAttribute(MetadataIdentifier.ofUid("trackedEntityAttribute")))
        .thenReturn(trackedEntityAttribute);
    when(preheat.getTrackedEntityType((MetadataIdentifier) any())).thenReturn(trackedEntityType);

    TrackedEntity trackedEntity =
        TrackedEntity.builder()
            .attributes(
                Collections.singletonList(
                    Attribute.builder()
                        .attribute(MetadataIdentifier.ofUid("trackedEntityAttribute"))
                        .build()))
            .trackedEntityType(MetadataIdentifier.ofUid("trackedEntityType"))
            .build();

    trackedEntityAttributeValidationHook.validateTrackedEntity(reporter, bundle, trackedEntity);

    assertFalse(reporter.hasErrors());
    assertEquals(0, reporter.getErrors().size());
  }

  @Test
  void shouldFailValidationWhenValueIsNullAndAttributeIsMandatory() {
    TrackedEntity trackedEntity =
        TrackedEntity.builder()
            .trackedEntity(TE_UID)
            .attributes(
                Collections.singletonList(
                    Attribute.builder()
                        .attribute(MetadataIdentifier.ofUid("trackedEntityAttribute"))
                        .build()))
            .trackedEntityType(MetadataIdentifier.ofUid("trackedEntityType"))
            .build();

    TrackedEntityTypeAttribute trackedEntityTypeAttribute = new TrackedEntityTypeAttribute();

    TrackedEntityAttribute trackedEntityAttribute = new TrackedEntityAttribute();
    trackedEntityAttribute.setUid("trackedEntityAttribute");
    trackedEntityAttribute.setValueType(ValueType.TEXT);
    trackedEntityTypeAttribute.setTrackedEntityAttribute(trackedEntityAttribute);
    trackedEntityTypeAttribute.setMandatory(true);

    TrackedEntityType trackedEntityType = new TrackedEntityType();
    trackedEntityType.setTrackedEntityTypeAttributes(
        Collections.singletonList(trackedEntityTypeAttribute));

    when(preheat.getTrackedEntityAttribute(MetadataIdentifier.ofUid("trackedEntityAttribute")))
        .thenReturn(trackedEntityAttribute);

    when(preheat.getTrackedEntityType((MetadataIdentifier) any())).thenReturn(trackedEntityType);
    bundle.setTrackedEntities(List.of(trackedEntity));

    trackedEntityAttributeValidationHook.validateTrackedEntity(reporter, bundle, trackedEntity);

    assertTrue(reporter.hasErrors());
    assertEquals(1, reporter.getErrors().size());
    assertEquals(TrackerErrorCode.E1076, reporter.getErrors().get(0).getErrorCode());
  }

  @Test
  void validateFileResourceOwner() {
    TrackedEntityType trackedEntityType = new TrackedEntityType();
    trackedEntityType.setUid("tet");
    TrackedEntityAttribute trackedEntityAttribute = new TrackedEntityAttribute();
    trackedEntityAttribute.setValueType(ValueType.FILE_RESOURCE);
    trackedEntityAttribute.setUid("tea");

    String fileResourceUid = CodeGenerator.generateUid();

    FileResource fileResource = new FileResource();
    fileResource.setAssigned(true);
    fileResource.setUid(fileResourceUid);

    when(preheat.get(FileResource.class, fileResourceUid)).thenReturn(fileResource);
    when(preheat.getTrackedEntityAttribute(MetadataIdentifier.ofUid("tea")))
        .thenReturn(trackedEntityAttribute);
    when(preheat.getTrackedEntityType(MetadataIdentifier.ofUid("tet")))
        .thenReturn(trackedEntityType);

    Attribute attribute = new Attribute();
    attribute.setAttribute(MetadataIdentifier.ofUid("tea"));
    attribute.setValueType(ValueType.FILE_RESOURCE);
    attribute.setValue(fileResourceUid);

    TrackedEntity trackedEntity =
        TrackedEntity.builder()
            .attributes(Collections.singletonList(attribute))
            .trackedEntityType(MetadataIdentifier.ofUid("tet"))
            .build();

    bundle.setStrategy(trackedEntity, TrackerImportStrategy.CREATE);

    trackedEntityAttributeValidationHook.validateTrackedEntity(reporter, bundle, trackedEntity);

    assertTrue(reporter.hasErrors());
    assertEquals(1, reporter.getErrors().size());
    assertEquals(TrackerErrorCode.E1009, reporter.getErrors().get(0).getErrorCode());

    reporter = new ValidationErrorReporter(idSchemes);

    trackedEntity.setTrackedEntity("XYZ");
    fileResource.setFileResourceOwner("ABC");

    bundle.setStrategy(trackedEntity, TrackerImportStrategy.UPDATE);

    trackedEntityAttributeValidationHook.validateTrackedEntity(reporter, bundle, trackedEntity);

    assertTrue(reporter.hasErrors());
    assertEquals(1, reporter.getErrors().size());
    assertEquals(TrackerErrorCode.E1009, reporter.getErrors().get(0).getErrorCode());

    reporter = new ValidationErrorReporter(idSchemes);

    trackedEntity.setTrackedEntity("ABC");
    fileResource.setFileResourceOwner("ABC");

    bundle.setStrategy(trackedEntity, TrackerImportStrategy.UPDATE);

    trackedEntityAttributeValidationHook.validateTrackedEntity(reporter, bundle, trackedEntity);

    assertFalse(reporter.hasErrors());
    assertEquals(0, reporter.getErrors().size());
  }

  private TrackedEntityAttribute getTrackedEntityAttributeWithOptionSet() {
    TrackedEntityAttribute trackedEntityAttribute = new TrackedEntityAttribute();
    trackedEntityAttribute.setUid("uid");
    trackedEntityAttribute.setValueType(ValueType.TEXT);

    OptionSet optionSet = new OptionSet();
    Option option = new Option();
    option.setCode("CODE");

    Option option1 = new Option();
    option1.setCode("CODE1");

    optionSet.setOptions(Arrays.asList(option, option1));

    trackedEntityAttribute.setOptionSet(optionSet);
    return trackedEntityAttribute;
  }

  private org.hisp.dhis.trackedentity.TrackedEntityInstance trackedEntity() {
    org.hisp.dhis.trackedentity.TrackedEntityInstance trackedEntity =
        new org.hisp.dhis.trackedentity.TrackedEntityInstance();
    trackedEntity.setUid(TE_UID);
    return trackedEntity;
  }

  private org.hisp.dhis.trackedentity.TrackedEntityInstance trackedEntity(
      TrackedEntityAttribute attribute) {
    org.hisp.dhis.trackedentity.TrackedEntityInstance trackedEntity =
        new org.hisp.dhis.trackedentity.TrackedEntityInstance();
    trackedEntity.setUid(TE_UID);
    TrackedEntityAttributeValue attributeValue =
        createTrackedEntityAttributeValue('c', trackedEntity, attribute);
    attributeValue.setValue("value");
    Set<TrackedEntityAttributeValue> attributes = Set.of(attributeValue);
    trackedEntity.setTrackedEntityAttributeValues(attributes);
    return trackedEntity;
  }
}
