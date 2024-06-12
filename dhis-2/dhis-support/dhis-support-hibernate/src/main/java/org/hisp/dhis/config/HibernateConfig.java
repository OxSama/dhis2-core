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
package org.hisp.dhis.config;

import static org.hisp.dhis.external.conf.ConfigurationKey.USE_QUERY_CACHE;
import static org.hisp.dhis.external.conf.ConfigurationKey.USE_SECOND_LEVEL_CACHE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.cache.ehcache.internal.EhcacheRegionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hisp.dhis.cache.DefaultHibernateCacheManager;
import org.hisp.dhis.dbms.DbmsManager;
import org.hisp.dhis.dbms.HibernateDbmsManager;
import org.hisp.dhis.external.conf.ConfigurationKey;
import org.hisp.dhis.external.conf.DhisConfigurationProvider;
import org.hisp.dhis.hibernate.DhisPersistenceUnitInfo;
import org.hisp.dhis.hibernate.EntityManagerBeanDefinitionRegistrarPostProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Luciano Fiandesio
 * @author Morten Svanæs
 */
@Configuration
@EnableTransactionManagement
@Slf4j
public class HibernateConfig {

  @Bean
  public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
    return new PersistenceExceptionTranslationPostProcessor();
  }

  @Bean("jpaTransactionManager")
  @DependsOn("entityManagerFactory")
  public JpaTransactionManager jpaTransactionManager(
      @Qualifier("entityManagerFactory") EntityManagerFactory emf) {
    return new JpaTransactionManager(emf);
  }

  @Bean("transactionTemplate")
  @DependsOn("jpaTransactionManager")
  public TransactionTemplate transactionTemplate(
      @Qualifier("jpaTransactionManager") JpaTransactionManager transactionManager) {
    return new TransactionTemplate(transactionManager);
  }

  @Bean
  public DefaultHibernateCacheManager cacheManager(
      @Qualifier("entityManagerFactory") EntityManagerFactory emf) {
    DefaultHibernateCacheManager cacheManager = new DefaultHibernateCacheManager();
    cacheManager.setSessionFactory(emf.unwrap(SessionFactory.class));

    return cacheManager;
  }

  @Bean
  public DbmsManager dbmsManager(
      JdbcTemplate jdbcTemplate,
      DefaultHibernateCacheManager cacheManager,
      EntityManager entityManager) {
    HibernateDbmsManager hibernateDbmsManager = new HibernateDbmsManager();
    hibernateDbmsManager.setCacheManager(cacheManager);
    hibernateDbmsManager.setEntityManager(entityManager);
    hibernateDbmsManager.setJdbcTemplate(jdbcTemplate);
    return hibernateDbmsManager;
  }

  @Bean
  public BeanFactoryPostProcessor entityManagerBeanDefinitionRegistrarPostProcessor() {
    return new EntityManagerBeanDefinitionRegistrarPostProcessor();
  }

  @Bean("entityManagerFactory")
  @DependsOn({"flyway"})
  public EntityManagerFactory entityManagerFactoryBean(
      DhisConfigurationProvider dhisConfig, DataSource dataSource) {
    HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
    adapter.setDatabasePlatform(dhisConfig.getProperty(ConfigurationKey.CONNECTION_DIALECT));
    adapter.setGenerateDdl(shouldGenerateDDL(dhisConfig));

    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(dataSource);
    em.setPersistenceProviderClass(HibernatePersistenceProvider.class);
    em.setJpaVendorAdapter(adapter);
    em.setPersistenceUnitManager(persistenceUnitManager(dataSource));
    em.setJpaProperties(getAdditionalProperties(dhisConfig));
    em.setPackagesToScan("org.hisp.dhis");
    em.setMappingResources(loadResources().toArray(new String[0]));
    em.afterPropertiesSet();

    return em.getObject();
  }

  @Bean
  public DefaultPersistenceUnitManager persistenceUnitManager(DataSource dataSource) {
    DefaultPersistenceUnitManager persistenceUnitManager = new DefaultPersistenceUnitManager();
    persistenceUnitManager.setDefaultDataSource(dataSource);
    persistenceUnitManager.setPackagesToScan("org.hisp.dhis");
    persistenceUnitManager.setMappingResources(loadResources().toArray(new String[0]));
    return persistenceUnitManager;
  }

  /**
   * Returns additional properties to be used by the {@link LocalContainerEntityManagerFactoryBean}
   */
  private Properties getAdditionalProperties(DhisConfigurationProvider dhisConfig) {
    Properties properties = new Properties();
    properties.put(
        "hibernate.current_session_context_class",
        "org.springframework.orm.hibernate5.SpringSessionContext");

    if (dhisConfig.getProperty(USE_SECOND_LEVEL_CACHE).equals("true")) {
      properties.put(AvailableSettings.USE_SECOND_LEVEL_CACHE, "true");
      properties.put(AvailableSettings.CACHE_REGION_FACTORY, EhcacheRegionFactory.class.getName());
      properties.put(AvailableSettings.USE_QUERY_CACHE, dhisConfig.getProperty(USE_QUERY_CACHE));
    }
    properties.setProperty(
        AvailableSettings.DIALECT, dhisConfig.getProperty(ConfigurationKey.CONNECTION_DIALECT));
    properties.setProperty(
        AvailableSettings.HBM2DDL_AUTO, dhisConfig.getProperty(ConfigurationKey.CONNECTION_SCHEMA));
    properties.setProperty(
        AvailableSettings.URL, dhisConfig.getProperty(ConfigurationKey.CONNECTION_URL));

    // TODO: this is anti-pattern and should be turn off
    properties.put("hibernate.allow_update_outside_transaction", "true");

    return properties;
  }

  /**
   * Loads all the hibernate mapping files from the classpath
   *
   * @return Array of Strings representing the mapping files
   */
  private List<String> loadResources() {
    try {
      PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
      //      Resource[] hbmFiles = resolver.getResources("classpath*:org/hisp/dhis/**/*.hbm.xml");

      Resource[] ormFiles = resolver.getResources("classpath*:META-INF/*.orm.xml");
      List<String> list = new ArrayList<>();
      for (Resource resource : ormFiles) {
        String url = resource.getURL().toString();
        list.add(url);
      }

      return list;
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    return List.of();
  }

  class DhisPersistenceUnitManager implements PersistenceUnitManager {
    private final DhisConfigurationProvider dhisConfig;
    private final DataSource dataSource;

    public DhisPersistenceUnitManager(DhisConfigurationProvider dhisConfig, DataSource dataSource) {
      this.dhisConfig = dhisConfig;
      this.dataSource = dataSource;
    }

    @Override
    public PersistenceUnitInfo obtainDefaultPersistenceUnitInfo() throws IllegalStateException {
      return DhisPersistenceUnitInfo.builder()
          .nonJtaDataSource(dataSource)
          .jtaDataSource(dataSource)
          .persistenceProviderClassName("org.hibernate.jpa.HibernatePersistenceProvider")
          .persistenceUnitName("dhis")
          .properties(getAdditionalProperties(dhisConfig))
          .validationMode(ValidationMode.AUTO)
          .transactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL)
          .sharedCacheMode(SharedCacheMode.ENABLE_SELECTIVE)
          .mappingFileNames(loadResources())
          .build();
    }

    @Override
    public PersistenceUnitInfo obtainPersistenceUnitInfo(String persistenceUnitName)
        throws IllegalArgumentException, IllegalStateException {
      return obtainDefaultPersistenceUnitInfo();
    }

    /**
     * Returns additional properties to be used by the {@link
     * LocalContainerEntityManagerFactoryBean}
     */
    private Properties getAdditionalProperties(DhisConfigurationProvider dhisConfig) {
      Properties properties = new Properties();
      properties.put(
          "hibernate.current_session_context_class",
          "org.springframework.orm.hibernate5.SpringSessionContext");

      if (dhisConfig.getProperty(USE_SECOND_LEVEL_CACHE).equals("true")) {
        properties.put(AvailableSettings.USE_SECOND_LEVEL_CACHE, "true");
        properties.put(
            AvailableSettings.CACHE_REGION_FACTORY, EhcacheRegionFactory.class.getName());
        properties.put(AvailableSettings.USE_QUERY_CACHE, dhisConfig.getProperty(USE_QUERY_CACHE));
      }
      properties.setProperty(
          AvailableSettings.DIALECT, dhisConfig.getProperty(ConfigurationKey.CONNECTION_DIALECT));
      properties.setProperty(
          AvailableSettings.HBM2DDL_AUTO,
          dhisConfig.getProperty(ConfigurationKey.CONNECTION_SCHEMA));

      // TODO: this is anti-pattern and should be turn off
      properties.put("hibernate.allow_update_outside_transaction", "true");

      return properties;
    }

    /**
     * Loads all the hibernate mapping files from the classpath
     *
     * @return Array of Strings representing the mapping files
     */
    private List<String> loadResources() {
      try {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        //      Resource[] hbmFiles =
        // resolver.getResources("classpath*:org/hisp/dhis/**/*.hbm.xml");

        Resource[] ormFiles = resolver.getResources("classpath:*.orm.xml");

        List<String> list = new ArrayList<>();
        for (Resource resource : ormFiles) {
          String url = resource.getURL().toString();
          list.add(url);
        }

        for (Resource resource : ormFiles) {
          String url = resource.getURL().toString();
          list.add(url);
        }
        return list;
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }
      return List.of();
    }
  }

  /**
   * If return true, hibernate will generate the DDL for the database. This is used by h2-test.
   * @param dhisConfig {@link DhisConfigurationProvider
   * @return TRUE if connection.schema is not set to none
   */
  private boolean shouldGenerateDDL(DhisConfigurationProvider dhisConfig) {
    return "update".equals(dhisConfig.getProperty(ConfigurationKey.CONNECTION_SCHEMA));
  }
}
