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
package org.hisp.dhis.configuration;

import static org.hisp.dhis.external.conf.ConfigurationKey.ANALYTICS_CITUS_EXTENSION;

import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.external.conf.DhisConfigurationProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Component responsible for exposing citus extension settings. Can hold settings living in
 * configuration files (ie. dhis.conf) or in system settings.
 *
 * @author giuseppe nespolino
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CitusSettings {

  private final DhisConfigurationProvider dhisConfigurationProvider;

  @Qualifier("analyticsJdbcTemplate")
  private final JdbcTemplate jdbcTemplate;

  private boolean citusExtension;

  @PostConstruct
  public void init() {
    citusExtension = isCitusExtensionEnabled();
  }

  /**
   * Checks if Citus is installed and enabled, and applied to the current database.
   *
   * @return true if the Citus extension is fully available, false otherwise.
   */
  public boolean isCitusEnabled() {
    return citusExtension;
  }

  public boolean isCitusExtensionEnabled() {
    if (isCitusDisabledByConfig()) {
      log.info("Citus extension is disabled in dhis.conf");
      return false;
    }
    return isCitusExtensionInstalledAndCreated();
  }

  /**
   * @return true if the Citus extension is disabled in dhis configuration file.
   */
  private boolean isCitusDisabledByConfig() {
    return !dhisConfigurationProvider.isEnabled(ANALYTICS_CITUS_EXTENSION);
  }

  /**
   * Returns true if the citus extension is installed and created in the database whose jdbcTemplate
   * refers to.
   *
   * @return true if the Citus extension is installed and created.
   */
  private boolean isCitusExtensionInstalledAndCreated() {
    List<PgExtension> installedExtensions = getPostgresInstalledCitusExtensions();

    logFoundExtensions(installedExtensions);

    return installedExtensions.stream().anyMatch(this::isCitusEnabled);
  }

  /**
   * Logs the found citus extensions in the database.
   *
   * @param installedExtensions the list of citus extensions found in the database
   */
  private void logFoundExtensions(List<PgExtension> installedExtensions) {
    if (!installedExtensions.isEmpty()) {
      log.info(
          "Found citus extensions in the database: {}",
          StringUtils.join(installedExtensions, ", "));
      return;
    }
    log.info(
        """
        Citus extension software is not installed in the database.
        You need to install it first: https://docs.citusdata.com/en/latest/installation/multi_node.html
        """);
  }

  /**
   * Returns the list of citus extensions installed, along with version, in the database whose
   * jdbcTemplate refers to.
   *
   * @return the list of Citus extensions installed.
   */
  private List<PgExtension> getPostgresInstalledCitusExtensions() {
    return jdbcTemplate.query(
        "select name, installed_version from pg_available_extensions where lower(name) like 'citus%'",
        new DataClassRowMapper<>(PgExtension.class));
  }

  /**
   * Returns true if the provided citus extension is enabled (version is not null).
   *
   * @param pgExtension the {@link PgExtension} object.
   * @return true if the provided citus extension is enabled.
   */
  private boolean isCitusEnabled(PgExtension pgExtension) {
    boolean isCitusExtensionEnabled =
        Optional.of(pgExtension)
            .map(PgExtension::installedVersion)
            .filter(StringUtils::isNotBlank)
            .isPresent();

    if (!isCitusExtensionEnabled) {
      log.info(
          """
          Citus extension was not created in the DHIS database.
          You need to run 'CREATE EXTENSION citus;' after the creation of the database""");
      return false;
    }

    log.info("Citus extension detected in the DHIS database");
    return true;
  }

  /** Represents a Citus extension installed in the database. */
  public record PgExtension(String name, String installedVersion) {}
}
