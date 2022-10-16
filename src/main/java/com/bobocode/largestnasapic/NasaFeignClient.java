package com.bobocode.largestnasapic;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.http.HttpResponse;
import java.util.Optional;

@FeignClient(url="https://api.nasa.gov", name="nasaClient")
public interface NasaFeignClient {

  @GetMapping("/mars-photos/api/v1/rovers/curiosity/photos")
  JsonNode getLargestPicture(
      @RequestParam Integer sol,
      @RequestParam String api_key,
      @RequestParam(required = false) Optional<String> camera);

}
