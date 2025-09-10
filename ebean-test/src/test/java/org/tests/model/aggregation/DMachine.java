package org.tests.model.aggregation;

import io.ebean.Model;
import io.ebean.annotation.Aggregation;
import io.ebean.annotation.Formula;
import io.ebean.bean.Computed;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
public class DMachine extends Model {

  @Id
  private long id;

  private String name;

  @ManyToOne
  private DOrg organisation;

  @Version
  private long version;

  @OneToMany(mappedBy = "machine")
  private List<DMachineStats> machineStats;

  @OneToMany(mappedBy = "machine")
  private List<DMachineAuxUseAgg> auxUseAggs;

  @Aggregation("sum(machineStats.cost)")
  private Computed<BigDecimal> sumCost;

  @Formula(select = "_b${ta}.total_amount", join = "join (select machine_id, sum(cost * 2) as total_amount " +
    "from d_machine_stats group by machine_id) as _b${ta} on _b${ta}.machine_id = ${ta}.id")
  private Computed<BigDecimal> sumCost2;

  private Computed<BigDecimal> value;

  public DMachine() {
  }

  public DMachine(DOrg organisation, String name) {
    this.organisation = organisation;
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DOrg getOrganisation() {
    return organisation;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public List<DMachineStats> getMachineStats() {
    return machineStats;
  }

  public void setMachineStats(List<DMachineStats> machineStats) {
    this.machineStats = machineStats;
  }

  public List<DMachineAuxUseAgg> getAuxUseAggs() {
    return auxUseAggs;
  }

  public void setAuxUseAggs(List<DMachineAuxUseAgg> auxUseAggs) {
    this.auxUseAggs = auxUseAggs;
  }

  public Computed<BigDecimal> getSumCost() {
    return sumCost;
  }

  public void setSumCost(Computed<BigDecimal> sumCost) {
    this.sumCost = sumCost;
  }

  public Computed<BigDecimal> getValue() {
    return value;
  }

  public void setValue(Computed<BigDecimal> value) {
    this.value = value;
  }

  public Computed<BigDecimal> getSumCost2() {
    return sumCost2;
  }

  public void setSumCost2(Computed<BigDecimal> sumCost2) {
    this.sumCost2 = sumCost2;
  }
}
