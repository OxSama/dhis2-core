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
package org.hisp.dhis.webapi.controller.organisationunit;

import static java.lang.Math.max;
import static org.hisp.dhis.dxf2.webmessage.WebMessageUtils.ok;
import static org.hisp.dhis.security.Authorities.F_ORGANISATION_UNIT_MERGE;
import static org.hisp.dhis.security.Authorities.F_ORGANISATION_UNIT_SPLIT;
import static org.hisp.dhis.system.util.GeoUtils.getCoordinatesFromGeometry;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.OpenApi;
import org.hisp.dhis.common.UID;
import org.hisp.dhis.dxf2.common.OrderParams;
import org.hisp.dhis.dxf2.webmessage.WebMessage;
import org.hisp.dhis.feedback.BadRequestException;
import org.hisp.dhis.feedback.ForbiddenException;
import org.hisp.dhis.feedback.NotFoundException;
import org.hisp.dhis.merge.orgunit.OrgUnitMergeQuery;
import org.hisp.dhis.merge.orgunit.OrgUnitMergeService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitQueryParams;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.organisationunit.comparator.OrganisationUnitByLevelComparator;
import org.hisp.dhis.security.RequiresAuthority;
import org.hisp.dhis.split.orgunit.OrgUnitSplitQuery;
import org.hisp.dhis.split.orgunit.OrgUnitSplitService;
import org.hisp.dhis.user.CurrentUser;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserDetails;
import org.hisp.dhis.version.VersionService;
import org.hisp.dhis.webapi.controller.AbstractCrudController;
import org.hisp.dhis.webapi.webdomain.StreamingJsonRoot;
import org.hisp.dhis.webapi.webdomain.WebOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Controller
@RequestMapping("/api/organisationUnits")
@OpenApi.Document(classifiers = {"team:platform", "purpose:metadata"})
public class OrganisationUnitController extends AbstractCrudController<OrganisationUnit> {
  @Autowired private OrganisationUnitService organisationUnitService;

  @Autowired private VersionService versionService;

  @Autowired private OrgUnitSplitService orgUnitSplitService;

  @Autowired private OrgUnitMergeService orgUnitMergeService;

  @ResponseStatus(HttpStatus.OK)
  @RequiresAuthority(anyOf = F_ORGANISATION_UNIT_SPLIT)
  @PostMapping(value = "/split", produces = APPLICATION_JSON_VALUE)
  public @ResponseBody WebMessage splitOrgUnits(@RequestBody OrgUnitSplitQuery query) {
    orgUnitSplitService.split(orgUnitSplitService.getFromQuery(query));

    return ok("Organisation unit split");
  }

  @ResponseStatus(HttpStatus.OK)
  @RequiresAuthority(anyOf = F_ORGANISATION_UNIT_MERGE)
  @PostMapping(value = "/merge", produces = APPLICATION_JSON_VALUE)
  public @ResponseBody WebMessage mergeOrgUnits(@RequestBody OrgUnitMergeQuery query) {
    orgUnitMergeService.merge(orgUnitMergeService.getFromQuery(query));

    return ok("Organisation units merged");
  }

  @OpenApi.Param(name = "fields", value = String[].class)
  @OpenApi.Param(name = "filter", value = String[].class)
  @OpenApi.Params(WebOptions.class)
  @OpenApi.Response(ObjectListResponse.class)
  @GetMapping(value = "/{uid}", params = "includeChildren=true")
  public @ResponseBody ResponseEntity<StreamingJsonRoot<OrganisationUnit>> getIncludeChildren(
      @OpenApi.Param(UID.class) @PathVariable("uid") String uid,
      @RequestParam Map<String, String> rpParameters,
      OrderParams orderParams,
      HttpServletResponse response,
      @CurrentUser UserDetails currentUser)
      throws ForbiddenException, BadRequestException, NotFoundException {
    return getChildren(uid, rpParameters, orderParams, response, currentUser);
  }

  @OpenApi.Param(name = "fields", value = String[].class)
  @OpenApi.Param(name = "filter", value = String[].class)
  @OpenApi.Params(WebOptions.class)
  @OpenApi.Response(ObjectListResponse.class)
  @GetMapping("/{uid}/children")
  public @ResponseBody ResponseEntity<StreamingJsonRoot<OrganisationUnit>> getChildren(
      @OpenApi.Param(UID.class) @PathVariable("uid") String uid,
      @RequestParam Map<String, String> rpParameters,
      OrderParams orderParams,
      HttpServletResponse response,
      @CurrentUser UserDetails currentUser)
      throws ForbiddenException, BadRequestException, NotFoundException {
    OrganisationUnit parent = getEntity(uid);
    Function<WebOptions, List<UID>> getSpecialFilterMatches =
        (options) ->
            Stream.concat(Stream.of(parent), parent.getChildren().stream()).map(UID::of).toList();
    return getObjectList(rpParameters, orderParams, response, currentUser, getSpecialFilterMatches);
  }

  @OpenApi.Param(name = "fields", value = String[].class)
  @OpenApi.Param(name = "filter", value = String[].class)
  @OpenApi.Params(WebOptions.class)
  @OpenApi.Response(ObjectListResponse.class)
  @GetMapping(value = "/{uid}", params = "level")
  public @ResponseBody ResponseEntity<StreamingJsonRoot<OrganisationUnit>> getObjectWithLevel(
      @OpenApi.Param(UID.class) @PathVariable("uid") String uid,
      @RequestParam int level,
      @RequestParam Map<String, String> rpParameters,
      OrderParams orderParams,
      HttpServletResponse response,
      @CurrentUser UserDetails currentUser)
      throws ForbiddenException, BadRequestException, NotFoundException {
    return getChildrenWithLevel(uid, level, rpParameters, orderParams, response, currentUser);
  }

  @OpenApi.Param(name = "fields", value = String[].class)
  @OpenApi.Param(name = "filter", value = String[].class)
  @OpenApi.Params(WebOptions.class)
  @OpenApi.Response(ObjectListResponse.class)
  @GetMapping(value = "/{uid}/children", params = "level")
  public @ResponseBody ResponseEntity<StreamingJsonRoot<OrganisationUnit>> getChildrenWithLevel(
      @OpenApi.Param(UID.class) @PathVariable("uid") String uid,
      @RequestParam int level,
      @RequestParam Map<String, String> rpParameters,
      OrderParams orderParams,
      HttpServletResponse response,
      @CurrentUser UserDetails currentUser)
      throws ForbiddenException, BadRequestException, NotFoundException {
    OrganisationUnit parent = getEntity(uid);
    Function<WebOptions, List<UID>> getSpecialFilterMatches =
        (options) ->
            organisationUnitService
                .getOrganisationUnitsAtLevel(parent.getLevel() + max(0, level), parent)
                .stream()
                .map(UID::of)
                .toList();
    return getObjectList(rpParameters, orderParams, response, currentUser, getSpecialFilterMatches);
  }

  @OpenApi.Param(name = "fields", value = String[].class)
  @OpenApi.Param(name = "filter", value = String[].class)
  @OpenApi.Params(WebOptions.class)
  @OpenApi.Response(ObjectListResponse.class)
  @GetMapping(value = "/{uid}", params = "includeDescendants=true")
  public @ResponseBody ResponseEntity<StreamingJsonRoot<OrganisationUnit>> getIncludeDescendants(
      @OpenApi.Param(UID.class) @PathVariable("uid") String uid,
      @RequestParam Map<String, String> rpParameters,
      OrderParams orderParams,
      HttpServletResponse response,
      @CurrentUser UserDetails currentUser)
      throws ForbiddenException, BadRequestException, NotFoundException {
    return getDescendants(uid, rpParameters, orderParams, response, currentUser);
  }

  @OpenApi.Param(name = "fields", value = String[].class)
  @OpenApi.Param(name = "filter", value = String[].class)
  @OpenApi.Params(WebOptions.class)
  @OpenApi.Response(ObjectListResponse.class)
  @GetMapping("/{uid}/descendants")
  public @ResponseBody ResponseEntity<StreamingJsonRoot<OrganisationUnit>> getDescendants(
      @OpenApi.Param(UID.class) @PathVariable("uid") String uid,
      @RequestParam Map<String, String> rpParameters,
      OrderParams orderParams,
      HttpServletResponse response,
      @CurrentUser UserDetails currentUser)
      throws ForbiddenException, BadRequestException, NotFoundException {
    OrganisationUnit parent = getEntity(uid);
    Function<WebOptions, List<UID>> getSpecialFilterMatches =
        (options) ->
            organisationUnitService.getOrganisationUnitWithChildren(parent.getUid()).stream()
                .map(UID::of)
                .toList();
    return getObjectList(rpParameters, orderParams, response, currentUser, getSpecialFilterMatches);
  }

  @OpenApi.Param(name = "fields", value = String[].class)
  @OpenApi.Param(name = "filter", value = String[].class)
  @OpenApi.Params(WebOptions.class)
  @OpenApi.Response(ObjectListResponse.class)
  @GetMapping(value = "/{uid}", params = "includeAncestors=true")
  public @ResponseBody ResponseEntity<StreamingJsonRoot<OrganisationUnit>> getIncludeAncestors(
      @OpenApi.Param(UID.class) @PathVariable("uid") String uid,
      @RequestParam Map<String, String> rpParameters,
      OrderParams orderParams,
      HttpServletResponse response,
      @CurrentUser UserDetails currentUser)
      throws ForbiddenException, BadRequestException, NotFoundException {
    return getAncestors(uid, rpParameters, orderParams, response, currentUser);
  }

  @OpenApi.Param(name = "fields", value = String[].class)
  @OpenApi.Param(name = "filter", value = String[].class)
  @OpenApi.Params(WebOptions.class)
  @OpenApi.Response(ObjectListResponse.class)
  @GetMapping("/{uid}/ancestors")
  public @ResponseBody ResponseEntity<StreamingJsonRoot<OrganisationUnit>> getAncestors(
      @OpenApi.Param(UID.class) @PathVariable("uid") String uid,
      @RequestParam Map<String, String> rpParameters,
      OrderParams orderParams,
      HttpServletResponse response,
      @CurrentUser UserDetails currentUser)
      throws ForbiddenException, BadRequestException, NotFoundException {
    OrganisationUnit parent = getEntity(uid);
    Function<WebOptions, List<UID>> getSpecialFilterMatches =
        (options) -> {
          List<OrganisationUnit> objects = new ArrayList<>(parent.getAncestors());
          Collections.reverse(objects);
          objects.add(0, parent);
          return objects.stream().map(UID::of).toList();
        };
    return getObjectList(rpParameters, orderParams, response, currentUser, getSpecialFilterMatches);
  }

  @OpenApi.Param(name = "fields", value = String[].class)
  @OpenApi.Param(name = "filter", value = String[].class)
  @OpenApi.Params(WebOptions.class)
  @OpenApi.Response(ObjectListResponse.class)
  @GetMapping("/{uid}/parents")
  public @ResponseBody ResponseEntity<StreamingJsonRoot<OrganisationUnit>> getParents(
      @OpenApi.Param(UID.class) @PathVariable("uid") String uid,
      @RequestParam Map<String, String> rpParameters,
      OrderParams orderParams,
      HttpServletResponse response,
      @CurrentUser UserDetails currentUser)
      throws ForbiddenException, BadRequestException, NotFoundException {
    OrganisationUnit root = getEntity(uid);
    Function<WebOptions, List<UID>> getSpecialFilterMatches =
        (options) -> {
          OrganisationUnit parent = root.getParent();
          List<OrganisationUnit> objects = new ArrayList<>();
          while (parent != null) {
            objects.add(parent);
            parent = parent.getParent();
          }
          return objects.stream().map(UID::of).toList();
        };
    return getObjectList(rpParameters, orderParams, response, currentUser, getSpecialFilterMatches);
  }

  @OpenApi.Param(name = "fields", value = String[].class)
  @OpenApi.Param(name = "filter", value = String[].class)
  @OpenApi.Params(WebOptions.class)
  @OpenApi.Response(ObjectListResponse.class)
  @GetMapping(params = "userOnly=true")
  public @ResponseBody ResponseEntity<StreamingJsonRoot<OrganisationUnit>> getUserOrganisationUnits(
      @RequestParam boolean userOnly,
      @RequestParam Map<String, String> rpParameters,
      OrderParams orderParams,
      HttpServletResponse response,
      @CurrentUser User currentUser)
      throws ForbiddenException, BadRequestException {
    return getObjectList(
        rpParameters,
        orderParams,
        response,
        UserDetails.fromUser(currentUser),
        options -> currentUser.getOrganisationUnits().stream().map(UID::of).toList());
  }

  @OpenApi.Param(name = "fields", value = String[].class)
  @OpenApi.Param(name = "filter", value = String[].class)
  @OpenApi.Params(WebOptions.class)
  @OpenApi.Response(ObjectListResponse.class)
  @GetMapping(params = "userDataViewOnly=true")
  public @ResponseBody ResponseEntity<StreamingJsonRoot<OrganisationUnit>>
      getUserDataViewOrganisationUnits(
          @RequestParam boolean userDataViewOnly,
          @RequestParam Map<String, String> rpParameters,
          OrderParams orderParams,
          HttpServletResponse response,
          @CurrentUser User currentUser)
          throws ForbiddenException, BadRequestException {
    return getObjectList(
        rpParameters,
        orderParams,
        response,
        UserDetails.fromUser(currentUser),
        options -> currentUser.getDataViewOrganisationUnits().stream().map(UID::of).toList());
  }

  @OpenApi.Param(name = "fields", value = String[].class)
  @OpenApi.Param(name = "filter", value = String[].class)
  @OpenApi.Params(WebOptions.class)
  @OpenApi.Response(ObjectListResponse.class)
  @GetMapping(params = "userDataViewFallback=true")
  public @ResponseBody ResponseEntity<StreamingJsonRoot<OrganisationUnit>>
      getUserDataViewOrganisationUnitsWithFallback(
          @RequestParam boolean userDataViewFallback,
          @RequestParam Map<String, String> rpParameters,
          OrderParams orderParams,
          HttpServletResponse response,
          @CurrentUser User currentUser)
          throws ForbiddenException, BadRequestException {
    Function<WebOptions, List<UID>> getSpecialFilterMatches =
        options ->
            (currentUser.hasDataViewOrganisationUnit()
                    ? currentUser.getDataViewOrganisationUnits()
                    : organisationUnitService.getOrganisationUnitsAtLevel(1))
                .stream().map(UID::of).toList();
    return getObjectList(
        rpParameters,
        orderParams,
        response,
        UserDetails.fromUser(currentUser),
        getSpecialFilterMatches);
  }

  @OpenApi.Param(name = "fields", value = String[].class)
  @OpenApi.Param(name = "filter", value = String[].class)
  @OpenApi.Params(WebOptions.class)
  @OpenApi.Response(ObjectListResponse.class)
  @GetMapping(params = "levelSorted=true")
  public @ResponseBody ResponseEntity<StreamingJsonRoot<OrganisationUnit>>
      getAllOrganisationUnitsByLevel(
          @RequestParam boolean levelSorted,
          @RequestParam Map<String, String> rpParameters,
          OrderParams orderParams,
          HttpServletResponse response,
          @CurrentUser UserDetails currentUser)
          throws ForbiddenException, BadRequestException {
    return getObjectList(
        rpParameters,
        orderParams,
        response,
        currentUser,
        options ->
            manager.getAll(getEntityClass()).stream()
                .sorted(OrganisationUnitByLevelComparator.INSTANCE)
                .map(UID::of)
                .toList());
  }

  @Override
  @OpenApi.Ignore
  @GetMapping(
      params =
          "getObjectList") // overridden and over-specified to effectively remove the @GetMapping
  public ResponseEntity<StreamingJsonRoot<OrganisationUnit>> getObjectList(
      Map<String, String> rpParameters,
      OrderParams orderParams,
      HttpServletResponse response,
      UserDetails currentUser) {
    return null;
  }

  @OpenApi.Param(name = "fields", value = String[].class)
  @OpenApi.Param(name = "filter", value = String[].class)
  @OpenApi.Param(name = "memberObject", value = String.class)
  @OpenApi.Param(name = "memberCollection", value = String.class)
  @OpenApi.Params(WebOptions.class)
  @OpenApi.Response(ObjectListResponse.class)
  @GetMapping
  public @ResponseBody ResponseEntity<StreamingJsonRoot<OrganisationUnit>> getObjectList(
      @RequestParam(required = false) String query,
      @RequestParam(required = false) Integer level,
      @RequestParam(required = false) Integer maxLevel,
      @RequestParam(required = false) Boolean withinUserHierarchy,
      @RequestParam(required = false) Boolean withinUserSearchHierarchy,
      @RequestParam Map<String, String> rpParameters,
      OrderParams orderParams,
      HttpServletResponse response,
      @CurrentUser User currentUser)
      throws ForbiddenException, BadRequestException {
    if (query == null
        && level == null
        && maxLevel == null
        && withinUserHierarchy == null
        && withinUserSearchHierarchy == null) {

      return super.getObjectList(
          rpParameters, orderParams, response, UserDetails.fromUser(currentUser));
    }
    OrganisationUnitQueryParams params = new OrganisationUnitQueryParams();
    params.setQuery(query);
    params.setLevel(level);
    params.setMaxLevels(maxLevel);

    params.setParents(
        withinUserHierarchy == Boolean.TRUE
            ? currentUser.getOrganisationUnits()
            : withinUserSearchHierarchy == Boolean.TRUE
                ? currentUser.getTeiSearchOrganisationUnitsWithFallback()
                : Set.of());

    return getObjectList(
        rpParameters,
        orderParams,
        response,
        UserDetails.fromUser(currentUser),
        options ->
            organisationUnitService.getOrganisationUnitsByQuery(params).stream()
                .map(UID::of)
                .toList());
  }

  @Override
  protected void getEntityListPostProcess(WebOptions options, List<OrganisationUnit> entities) {
    String memberObject = options.get("memberObject");
    String memberCollection = options.get("memberCollection");
    if (memberObject != null && memberCollection != null) {
      Optional<? extends IdentifiableObject> member = manager.find(memberObject);
      if (member.isPresent()) {
        for (OrganisationUnit unit : entities) {
          Long count =
              organisationUnitService.getOrganisationUnitHierarchyMemberCount(
                  unit, member.get(), memberCollection);

          unit.setMemberCount((count != null ? count.intValue() : 0));
        }
      }
    }
  }

  @GetMapping(
      value = {"", ".geojson"},
      produces = {"application/json+geo", "application/json+geojson"})
  public void getGeoJson(
      @RequestParam(value = "level", required = false) List<Integer> rpLevels,
      @OpenApi.Param({UID[].class, OrganisationUnit.class})
          @RequestParam(value = "parent", required = false)
          List<String> rpParents,
      @RequestParam(value = "properties", required = false, defaultValue = "true")
          boolean rpProperties,
      @CurrentUser User currentUser,
      HttpServletResponse response)
      throws IOException {
    rpLevels = rpLevels != null ? rpLevels : new ArrayList<>();
    rpParents = rpParents != null ? rpParents : new ArrayList<>();

    List<OrganisationUnit> parents =
        new ArrayList<>(manager.getByUid(OrganisationUnit.class, rpParents));

    if (rpLevels.isEmpty()) {
      rpLevels.add(1);
    }

    if (parents.isEmpty()) {
      parents.addAll(organisationUnitService.getRootOrganisationUnits());
    }

    List<OrganisationUnit> organisationUnits =
        organisationUnitService.getOrganisationUnitsAtLevels(rpLevels, parents);

    response.setContentType(APPLICATION_JSON_VALUE);

    try (JsonGenerator generator = new JsonFactory().createGenerator(response.getOutputStream())) {

      generator.writeStartObject();
      generator.writeStringField("type", "FeatureCollection");
      generator.writeArrayFieldStart("features");

      for (OrganisationUnit organisationUnit : organisationUnits) {
        writeFeature(generator, organisationUnit, rpProperties, currentUser);
      }

      generator.writeEndArray();
      generator.writeEndObject();
    }
  }

  private void writeFeature(
      JsonGenerator generator,
      OrganisationUnit organisationUnit,
      boolean includeProperties,
      User user)
      throws IOException {
    if (organisationUnit.getGeometry() == null) {
      return;
    }

    generator.writeStartObject();

    generator.writeStringField("type", "Feature");
    generator.writeStringField("id", organisationUnit.getUid());

    generator.writeObjectFieldStart("geometry");
    generator.writeObjectField("type", organisationUnit.getGeometry().getGeometryType());

    generator.writeFieldName("coordinates");
    generator.writeRawValue(getCoordinatesFromGeometry(organisationUnit.getGeometry()));

    generator.writeEndObject();

    generator.writeObjectFieldStart("properties");

    if (includeProperties) {
      Set<OrganisationUnit> roots = user.getDataViewOrganisationUnitsWithFallback();

      generator.writeStringField("code", organisationUnit.getCode());
      generator.writeStringField("name", organisationUnit.getName());
      generator.writeStringField("level", String.valueOf(organisationUnit.getLevel()));

      if (organisationUnit.getParent() != null) {
        generator.writeStringField("parent", organisationUnit.getParent().getUid());
      }

      generator.writeStringField("parentGraph", organisationUnit.getParentGraph(roots));

      generator.writeArrayFieldStart("groups");

      for (OrganisationUnitGroup group : organisationUnit.getGroups()) {
        generator.writeString(group.getUid());
      }

      generator.writeEndArray();
    }

    generator.writeEndObject();

    generator.writeEndObject();
  }

  @Override
  protected void postCreateEntity(OrganisationUnit entity) {
    versionService.updateVersion(VersionService.ORGANISATIONUNIT_VERSION);
  }

  @Override
  protected void postUpdateEntity(OrganisationUnit entity) {
    versionService.updateVersion(VersionService.ORGANISATIONUNIT_VERSION);
  }

  @Override
  protected void postDeleteEntity(String entityUID) {
    versionService.updateVersion(VersionService.ORGANISATIONUNIT_VERSION);
  }
}
