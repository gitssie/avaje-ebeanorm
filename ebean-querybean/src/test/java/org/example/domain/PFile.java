package org.example.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class PFile {

  @Id
  long id;
  String name;
}
