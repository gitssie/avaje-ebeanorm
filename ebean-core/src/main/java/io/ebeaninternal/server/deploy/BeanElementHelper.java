package io.ebeaninternal.server.deploy;

import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.server.query.STreeType;

public class BeanElementHelper {
  private EntityBeanIntercept ebi;
  private EntityBeanIntercept sbi;

  public BeanElementHelper(STreeType desc, EntityBean bean) {
    this(desc, bean, bean == null ? null : bean._ebean_getIntercept());
  }

  public BeanElementHelper(STreeType desc, EntityBean bean, EntityBeanIntercept ebi) {
    this.ebi = ebi;
    if (desc instanceof BeanDescriptor) {
      if (bean != null) {
        EntityBean elementBean = ((BeanDescriptor<?>) desc).elementBean(bean);
        if (elementBean != null) {
          this.sbi = elementBean._ebean_getIntercept();
        }
      }
    }
  }

  public void setLoaded(){
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

  public boolean isLoadedProperty(BeanProperty prop) {
    if (prop.isCustom()) {
      return sbi == null ? false : sbi.isLoadedProperty(prop.fieldIndex[1]);
    } else {
      return ebi.isLoadedProperty(prop.propertyIndex());
    }
  }

  public boolean isDirtyProperty(BeanProperty prop) {
    if (prop.isCustom()) {
      return sbi == null ? false : sbi.isDirtyProperty(prop.fieldIndex[1]);
    } else {
      return ebi.isDirtyProperty(prop.propertyIndex());
    }
  }
}
