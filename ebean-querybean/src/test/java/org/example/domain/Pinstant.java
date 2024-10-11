package org.example.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Pinstant {

  @Id
  long id;
  String name;
}
