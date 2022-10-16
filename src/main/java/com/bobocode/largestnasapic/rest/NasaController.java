package com.bobocode.largestnasapic.rest;

import com.bobocode.largestnasapic.service.NasaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mars/pictures/largest")
public class NasaController {

  private final NasaService nasaService;

  @GetMapping(produces = MediaType.IMAGE_PNG_VALUE)
  public ResponseEntity<byte[]> getLargestPicture(@RequestParam Integer sol,
      @RequestParam(required = false) Optional<String> camera) {
    return ResponseEntity.ok(nasaService.getLargestPhoto(sol, camera).orElseThrow());
  }
}