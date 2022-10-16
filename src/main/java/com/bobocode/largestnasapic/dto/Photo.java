package com.bobocode.largestnasapic.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Photo {

  private Integer id;
  private Integer sol;
  @JsonProperty(value = "img_src")
  private String imgSrc;

}
