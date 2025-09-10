package io.ebeaninternal.server.deploy;

import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.server.query.STreeType;

public class BeanElementHelper {
  private BeanDescriptor<?> desc;
  private EntityBeanIntercept ebi;
  private EntityBeanIntercept sbi;


  public BeanElementHelper(STreeType desc, EntityBean bean) {
    this(desc, bean, bean == null ? null : bean._ebean_getIntercept());
  }

  public BeanElementHelper(STreeType desc, EntityBean bean, EntityBeanIntercept ebi) {
    this.ebi = ebi;
    if (desc instanceof BeanDescriptor<?>) {
      this.desc = (BeanDescriptor<?>) desc;
      if (bean != null) {
        EntityBean elementBean = this.desc.elementBean(bean);
        if (elementBean != null) {
          this.sbi = elementBean._ebean_getIntercept();
        }
      }
    }
  }

  public void setLoaded() {
    ebi.setLoaded();
  }

  public void setLoadedLazy() {
    ebi.setLoadedLazy();
    if (sbi != null) {
      sbi.setLoadedLazy();
    }
  }

  public void setDisableLazyLoad(boolean disableLazyLoad) {
    ebi.setDisableLazyLoad(disableLazyLoad);
    if (sbi != null) {
      sbi.setDisableLazyLoad(disableLazyLoad);
    }
  }

  public void setFullyLoadedBean(boolean fullyLoadedBean) {
    ebi.setFullyLoadedBean(fullyLoadedBean);
    if (sbi != null) {
      sbi.setFullyLoadedBean(fullyLoadedBean);
    }
  }

  public void setNewBeanForUpdate() {
    ebi.setNewBeanForUpdate();
    if (sbi != null) {
      sbi.setNewBeanForUpdate();
    }
  }

  public BeanProperty beanProperty(String propName) {
    return desc == null ? null : desc.beanProperty(propName);
  }

  public boolean isLoadedProperty(String propName) {
    BeanProperty prop = desc == null ? null : desc.beanProperty(propName);
    return prop == null ? false : isLoadedProperty(prop);
  }

  public boolean isLoadedProperty(BeanProperty prop) {
    if (prop.isCustom()) {
      return sbi == null ? false : sbi.isLoadedProperty(prop.fieldIndex);
    } else {
      return ebi.isLoadedProperty(prop.propertyIndex());
    }
  }

  public boolean isLoadedProperty(int propertyIndex, int fieldIndex) {
    if (fieldIndex >= 0) {
      return sbi == null ? false : sbi.isLoadedProperty(fieldIndex);
    } else {
      return ebi.isLoadedProperty(propertyIndex);
    }
  }

  public boolean isDirtyProperty(String propName) {
    BeanProperty prop = desc == null ? null : desc.beanProperty(propName);
    return prop == null ? false : isDirtyProperty(prop);
  }

  public boolean isDirtyProperty(BeanProperty prop) {
    if (prop.isCustom()) {
      return sbi == null ? false : sbi.isDirtyProperty(prop.fieldIndex);
    } else {
      return ebi.isDirtyProperty(prop.propertyIndex());
    }
  }

  public boolean isDirtyProperty(int propertyIndex, int fieldIndex) {
    if (fieldIndex >= 0) {
      return sbi == null ? false : sbi.isDirtyProperty(fieldIndex);
    } else {
      return ebi.isDirtyProperty(propertyIndex);
    }
  }
}
