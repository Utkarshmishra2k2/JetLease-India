package com.jetlease.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "routes")
@Getter
@Setter
public class Route {
    @Id
    private String code;
    private String city;
    private String airport;
    private double lat;
    private double lon;
}
