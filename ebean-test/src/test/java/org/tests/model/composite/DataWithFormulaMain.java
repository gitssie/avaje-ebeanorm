package org.tests.model.composite;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import java.util.List;
import java.util.UUID;

@Entity
public class DataWithFormulaMain {

  @Id
  private UUID id;

  private String title;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "main")
  private List<DataWithFormula> metaData;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<DataWithFormula> getMetaData() {
    return metaData;
  }

  public void setMetaData(List<DataWithFormula> metaData) {
    this.metaData = metaData;
  }
}
