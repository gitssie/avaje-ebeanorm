package org.example.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class Alias {

  @Id
  long id;
  @Version
  long version;
}
