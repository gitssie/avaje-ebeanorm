package io.ebeaninternal.server.deploy;

import io.ebean.bean.ElementBean;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.PersistenceContext;
import io.ebean.plugin.Property;
import io.ebeaninternal.api.json.SpiJsonReader;
import io.ebeaninternal.server.cache.CachedBeanData;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BeanPropertyElement<T> extends BeanProperty {
  private BeanDescriptor<T> beanDescriptor;

  public BeanPropertyElement(BeanDescriptor<?> descriptor, DeployBeanProperty deploy) {
    super(descriptor, deploy);

  }

  @Override
  public void initialise(BeanDescriptorInitContext initContext) {
    List<BeanProperty> properties = new ArrayList<>();
    for (Property p : descriptor.allProperties()) {
      BeanProperty bp = (BeanProperty) p;
      if (bp.isCustom()) {
        properties.add(bp);
      }
    }
    if (!properties.isEmpty() && beanDescriptor == null) {
      DeployBeanDescriptor dp = new DeployBeanDescriptor(null, Map.class, null);
      dp.setName(ElementBean.class.getSimpleName());
      BeanProperty[] propertiesIndex = new BeanProperty[properties.size()];
      for (BeanProperty property : properties) {
        propertiesIndex[property.fieldIndex[1]] = property;
      }
      beanDescriptor = new BeanDescriptor(descriptor.owner, dp);
      beanDescriptor.propertiesIndex = propertiesIndex;
    }
  }


  public BeanDescriptor<T> targetDescriptor() {
    return beanDescriptor;
  }

  @Override
  public void setCacheDataValue(EntityBean bean, Object cacheData, PersistenceContext context) {
    if (cacheData == null) {
      return;
    }
    if (cacheData instanceof String) {
      // parse back from string to support optimisation of java object serialisation
      cacheData = scalarType.parse((String) cacheData);
    }
    load(beanDescriptor, bean, (CachedBeanData) cacheData, context);
  }

  @Override
  public Object getCacheDataValueOrig(EntityBeanIntercept ebi) {
    return cacheDataConvert(ebi.getOrigValue(propertyIndex));
  }

  @Override
  public Object getCacheDataValue(EntityBean bean) {
    return cacheDataConvert(bean);
  }

  private Object cacheDataConvert(Object ap) {
    if (ap == null) {
      return null;
    }
    return extract(beanDescriptor, (EntityBean) ap);
  }

  private void load(BeanDescriptor<?> desc, EntityBean bean, CachedBeanData cacheBeanData, PersistenceContext context) {
    // load the non-many properties
    BeanPropertyAssocMany mp;
    for (BeanProperty prop : desc.propertiesIndex) {
      if (!prop.isMany()) {
        loadProperty(bean, cacheBeanData, null, prop, context);
      } else if (prop instanceof BeanPropertyAssocMany) {
        mp = (BeanPropertyAssocMany) prop;
        if (mp.isElementCollection()) {
          loadProperty(bean, cacheBeanData, null, prop, context);
        } else {
          mp.createReferenceIfNull(bean);
        }
      }
    }
    desc.setElementBeanLoadedLazy(bean);
  }

  private void loadProperty(EntityBean bean, CachedBeanData cacheBeanData, EntityBeanIntercept ebi, BeanProperty prop, PersistenceContext context) {
    if (cacheBeanData.isLoaded(prop.name())) {
      Object value = cacheBeanData.getData(prop.name());
      prop.setCacheDataValue(bean, value, context);
    }
  }

  private CachedBeanData extract(BeanDescriptor<?> desc, EntityBean bean) {
    Map<String, Object> data = new LinkedHashMap<>();
    BeanPropertyAssocMany mp;
    for (BeanProperty prop : desc.propertiesIndex) {
      if (!prop.isMany()) {
        data.put(prop.name(), prop.getCacheDataValue(bean));
      } else if (prop instanceof BeanPropertyAssocMany) {
        mp = (BeanPropertyAssocMany) prop;
        if (mp.isElementCollection()) {
          data.put(prop.name(), prop.getCacheDataValue(bean));
        }
      }
    }
    long version = desc.getVersion(bean);
    return new CachedBeanData(null, null, data, version);
  }

  @Override
  public void setValue(EntityBean bean, Object value) {
  }
  @Override
  public void setValueIntercept(EntityBean bean, Object value) {
  }

  @Override
  public void jsonRead(SpiJsonReader readJson, EntityBean bean) throws IOException {
  }
}
