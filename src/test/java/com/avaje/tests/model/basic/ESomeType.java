package com.avaje.tests.model.basic;

import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ESomeType {

    @Id
    Integer id;
    
    Currency currency;
    
    Locale locale;
    
    TimeZone timeZone;

    public String toString() {
        return id+" "+locale+" "+currency+" "+timeZone;
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }
   
}
