package org.example.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import org.example.domain.other.OtherBean;

@SuppressWarnings("unused")
@Entity
public class OtherMain {

  @Id
  long id;

  @ManyToOne
  OtherBean other;

}
