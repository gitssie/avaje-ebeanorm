package com.avaje.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class EVanillaCollectionDetail {

    @Id
    Integer id;
    
    String something;
    
    public EVanillaCollectionDetail() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSomething() {
        return something;
    }

    public void setSomething(String something) {
        this.something = something;
    }
    
}
