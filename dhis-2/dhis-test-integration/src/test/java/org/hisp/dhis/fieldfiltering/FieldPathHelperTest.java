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
package org.hisp.dhis.fieldfiltering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.test.integration.SingleSetupIntegrationTestBase;
import org.hisp.dhis.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Lars Helge Overland
 */
class FieldPathHelperTest extends SingleSetupIntegrationTestBase {
  @Autowired private FieldPathHelper helper;

  @Test
  void testApplySimplePreset() {
    Map<String, FieldPath> fieldMapPath = new HashMap<>();

    FieldPath owner = new FieldPath(FieldPreset.SIMPLE, List.of(), false, true);

    helper.applyPresets(List.of(owner), fieldMapPath, DataElement.class);

    assertPropertyExists("id", fieldMapPath);
    assertPropertyExists("name", fieldMapPath);
    assertPropertyExists("shortName", fieldMapPath);
    assertPropertyExists("description", fieldMapPath);
    assertPropertyExists("valueType", fieldMapPath);
    assertPropertyExists("aggregationType", fieldMapPath);
    assertPropertyExists("domainType", fieldMapPath);

    assertNull(fieldMapPath.get("access"));
    assertNull(fieldMapPath.get("dataSetElements"));
    assertNull(fieldMapPath.get("optionSet"));
    assertNull(fieldMapPath.get("categoryCombo"));
    assertNull(fieldMapPath.get("translations"));
  }

  @Test
  void testApplyIdentifiablePreset() {
    Map<String, FieldPath> fieldMapPath = new HashMap<>();

    FieldPath owner = new FieldPath(FieldPreset.IDENTIFIABLE, List.of(), false, true);

    helper.applyPresets(List.of(owner), fieldMapPath, DataElement.class);

    assertPropertyExists("id", fieldMapPath);
    assertPropertyExists("name", fieldMapPath);
    assertPropertyExists("code", fieldMapPath);
    assertPropertyExists("created", fieldMapPath);
    assertPropertyExists("lastUpdated", fieldMapPath);
    assertPropertyExists("lastUpdatedBy", fieldMapPath);

    assertNull(fieldMapPath.get("shortName"));
    assertNull(fieldMapPath.get("description"));
  }

  @Test
  @DisplayName("Excluding skip sharing fields on the User class does not remove incorrect fields")
  void skipSharingFieldsExcludeCorrectFieldsTest() {
    // given skipSharing exclusions
    FieldPath user = new FieldPath("user", List.of(), true, false);
    FieldPath publicAccess = new FieldPath("publicAccess", List.of(), true, false);
    FieldPath userGroupAccesses = new FieldPath("userGroupAccesses", List.of(), true, false);
    FieldPath userAccesses = new FieldPath("userAccesses", List.of(), true, false);
    FieldPath externalAccess = new FieldPath("externalAccess", List.of(), true, false);
    FieldPath sharing = new FieldPath("sharing", List.of(), true, false);

    // and default preset owner
    FieldPath owner = new FieldPath("owner", List.of(), false, true);

    // when applying skipSharing exclusions for the User class
    List<FieldPath> result =
        helper.apply(
            List.of(
                user,
                owner,
                publicAccess,
                userGroupAccesses,
                userAccesses,
                externalAccess,
                sharing),
            User.class);

    // then only matching exclusions should have been applied
    // and fields starting with 'user' should still be present
    assertEquals(58, result.size()); // all user properties
    assertTrue(
        result.stream()
            .map(FieldPath::getName)
            .collect(Collectors.toList())
            .containsAll(List.of("username", "userRoles")));
  }

  private void assertPropertyExists(String propertyName, Map<String, FieldPath> fieldMapPath) {
    assertNotNull(fieldMapPath.get(propertyName));
    assertEquals(propertyName, fieldMapPath.get(propertyName).getName());
  }
}
