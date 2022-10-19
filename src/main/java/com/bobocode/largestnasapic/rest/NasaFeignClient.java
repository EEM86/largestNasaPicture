package com.bobocode.largestnasapic.rest;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(url = "http://localhost:8080", name = "tarasService")
public interface NasaFeignClient {

  @Cacheable("taras_picture_cache")
  @GetMapping("/mars/pictures/largest")
  byte[] getLargest(@RequestParam Integer sol, @RequestParam(required = false) String camera);
}
