package io.ebeaninternal.server.deploy.parse;

import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.server.deploy.BeanDescriptorMap;
import io.ebeaninternal.server.deploy.generatedproperty.GeneratedPropertyFactory;
import io.ebeaninternal.server.deploy.parse.tenant.XEntity;


/**
 * Read the deployment annotations for the bean.
 */
public class XReadAnnotations {

  private final ReadAnnotationConfig readConfig;
  private final TenantDeployCreateProperties createProperties;

  public XReadAnnotations(GeneratedPropertyFactory generatedPropFactory, String asOfViewSuffix, String versionsBetweenSuffix, DatabaseConfig config, TenantDeployCreateProperties createProperties) {
    this.readConfig = new ReadAnnotationConfig(generatedPropFactory, asOfViewSuffix, versionsBetweenSuffix, config);
    this.createProperties = createProperties;
  }

  /**
   * Read the initial non-relationship annotations included Id and EmbeddedId.
   * <p>
   * We then have enough to create BeanTables which are used in readAssociations
   * to resolve the relationships etc.
   * </p>
   */
  public void readInitial(DeployBeanInfo<?> info) {
    try {
      new XAnnotationClass(info, readConfig).parse();
      new XAnnotationFields(info, readConfig, createProperties).parse();
    } catch (RuntimeException e) {
      throw new RuntimeException("Error reading annotations for " + info, e);
    }
  }

  /**
   * Read and process the associated relationship annotations.
   * <p>
   * These can only be processed after the BeanTables have been created
   * </p>
   * <p>
   * This uses the factory as a call back to get the BeanTable for a given
   * associated bean.
   * </p>
   */
  public void readAssociations(DeployBeanInfo<?> info, BeanDescriptorMap factory) {
    try {
      new XAnnotationAssocOnes(info, readConfig, factory).parse();
      new XAnnotationAssocManys(info, readConfig, factory).parse();
    } catch (RuntimeException e) {
      throw new RuntimeException("Error reading annotations for " + info, e);
    }
  }
}
