package com.mengcraft.economy.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import java.util.UUID;

/**
 * Created on 16-3-21.
 */
@Entity
@Table(name = "famc_economy")
public class User {


    private Integer id;


    //2017.01.29 更新主键为NAME
    @Id
    private String username;

    private double balance;
    private Integer hidden;
    private UUID uuid;

    public Integer getId() {return this.id;}
    public void setId(Integer id) {this.id = id;}

    public String getUsername() {
        return this.username;
    }
    public void setUsername(String name) {
        this.username = name;
    }

    public double getBalance() {return this.balance;}
    public void setBalance(double value) {
        this.balance = value;
    }

    public Integer getHidden() {return this.hidden;}
    public void setHidden(Integer hidden) {this.hidden = hidden;}

    public  UUID getUuid() {return this.uuid;}
    public void setUuid(UUID id) {this.uuid = id;}
}
