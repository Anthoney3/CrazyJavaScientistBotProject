package com.crazy.scientist.crazyjavascientist.security.entities;

import javax.persistence.*;
import lombok.*;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CJS_CONFIG")
public class CJSConfigEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "SHORTNAME")
  private String short_name;

  @Column(name = "KEY_VALUE")
  private String key_value;

  @Column(name = "STATUS")
  private String status;

  public CJSConfigEntity(String short_name, String key_value, String status) {
    this.short_name = short_name;
    this.key_value = key_value;
    this.status = status;
  }
}
