package org.example.domain.other;

import javax.persistence.Entity;
import javax.persistence.Id;

@SuppressWarnings("unused")
@Entity
public class OtherBean {

  @Id
  long id;
  String name;
}
