package com.bobocode.largestnasapic.rest;

import com.bobocode.largestnasapic.dto.NasaPictureRequest;
import com.bobocode.largestnasapic.service.NasaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mars/pictures/largest")
public class NasaController {

  private final NasaService nasaService;

  @PostMapping
  public ResponseEntity<String> getPhotoLocation(@RequestBody NasaPictureRequest request) {
    var commandId = nasaService.accept(request);
    final URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
        .pathSegment(commandId)
        .build()
        .toUri();
    return ResponseEntity
        .status(HttpStatus.ACCEPTED)
        .location(location)
        .build();
  }

  @GetMapping(value = "/{commandId}", produces = MediaType.IMAGE_PNG_VALUE)
  public ResponseEntity<byte[]> getLargestPicture(@PathVariable String commandId) {
    return ResponseEntity.ok(nasaService.getPhotoById(commandId));
  }

  @ExceptionHandler(NoSuchElementException.class)
  @ResponseBody
  public ResponseEntity<?> handlePicturesNotFound(NoSuchElementException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ex.getMessage());
  }
}