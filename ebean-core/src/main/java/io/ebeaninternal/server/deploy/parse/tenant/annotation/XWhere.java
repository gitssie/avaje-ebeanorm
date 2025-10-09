package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import io.ebean.annotation.Platform;
import io.ebean.annotation.Where;

import java.lang.annotation.Annotation;

public class XWhere implements Where {
  private String clause = "";
  private Platform[] platforms = new Platform[0];

  @Override
  public String clause() {
    return clause;
  }

  @Override
  public Platform[] platforms() {
    return platforms;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return Where.class;
  }

  public String getClause() {
    return clause;
  }

  public void setClause(String clause) {
    this.clause = clause;
  }

  public Platform[] getPlatforms() {
    return platforms;
  }

  public void setPlatforms(Platform[] platforms) {
    this.platforms = platforms;
  }
}
