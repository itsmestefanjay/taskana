package pro.taskana.common.rest;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import pro.taskana.RestConfiguration;
import pro.taskana.TaskanaEngineConfiguration;
import pro.taskana.common.rest.ldap.LdapConfiguration;

/** Configuration class for all rest tests. */
@Import({RestConfiguration.class, LdapConfiguration.class})
public class TestConfiguration {

  @Bean
  public DataSource dataSource() {
    return TaskanaEngineConfiguration.createDefaultDataSource();
  }
}
