package io.ebeaninternal.server.deploy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ebeaninternal.server.core.DefaultServer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;

import static org.assertj.core.api.Assertions.assertThat;

public class BeanDescriptorTenantTest extends BaseTest{

  @Test
  public void createReference() {
    DefaultServer server = (DefaultServer) spiEbeanServer();
//    BeanDescriptorTenantManager descriptorManager = server.getBeanDescriptorManager();
//    descriptorManager.descMap.put(CustomEntityBean.class.getName(),descriptorManager.descMap.get(Customer.class.getName()));

//    descriptorManager.registerEntity(Customer.class);
    BeanDescriptor<Customer> customerDesc = server.descriptor(Customer.class);
//    customerDesc.get
    Customer bean = customerDesc.createReference(null, false, 42, null);
    assertThat(bean.getId()).isEqualTo(42);
    Assertions.assertThat(server().beanState(bean).isReadOnly()).isFalse();
  }

}
