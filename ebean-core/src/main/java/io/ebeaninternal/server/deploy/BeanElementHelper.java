package io.ebeaninternal.server.deploy;

import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.server.query.STreeType;

public class BeanElementHelper {
  private EntityBeanIntercept ebi;

  public BeanElementHelper(STreeType desc, EntityBean bean) {
    if (desc instanceof BeanDescriptor) {
      if (bean != null) {
        EntityBean elementBean = ((BeanDescriptor<?>) desc).elementBean(bean);
        if (elementBean != null) {
          ebi = elementBean._ebean_getIntercept();
        }
      }
    }
  }


  public void setLoadedLazy() {
    if (ebi != null) {
      ebi.setLoadedLazy();
    }
  }

  public void setDisableLazyLoad(boolean disableLazyLoad) {
    if (ebi != null) {
      ebi.setDisableLazyLoad(disableLazyLoad);
    }
  }

  public void setFullyLoadedBean(boolean fullyLoadedBean) {
    if (ebi != null) {
      ebi.setFullyLoadedBean(fullyLoadedBean);
    }
  }

  public void setNewBeanForUpdate() {
    if (ebi != null) {
      ebi.setNewBeanForUpdate();
    }
  }

  public boolean isLoadedProperty(BeanProperty prop) {
    return ebi == null ? false : ebi.isLoadedProperty(prop.fieldIndex[1]);
  }

  public boolean isDirtyProperty(BeanProperty prop) {
    return ebi == null ? false : ebi.isDirtyProperty(prop.fieldIndex[1]);
  }
}
