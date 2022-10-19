package com.bobocode.largestnasapic.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mars/pictures/largest")
@Slf4j
public class NasaController {

  private final NasaFeignClient nasaFeignClient;

  @GetMapping(produces = MediaType.IMAGE_PNG_VALUE)
  public byte[] getLargestPictureProxy(@RequestParam Integer sol,
      @RequestParam(required = false) String camera) {
    return nasaFeignClient.getLargest(sol, camera);
  }
}