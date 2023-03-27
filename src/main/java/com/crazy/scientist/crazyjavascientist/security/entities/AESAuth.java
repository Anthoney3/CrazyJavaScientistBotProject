package com.crazy.scientist.crazyjavascientist.security.entities;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@ToString
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "CJS_AUTH_KEY")
public class AESAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "AUTH_KEY")
    private String auth_key;
}
