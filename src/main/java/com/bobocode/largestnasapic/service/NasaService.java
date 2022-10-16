package com.bobocode.largestnasapic.service;

import com.bobocode.largestnasapic.NasaFeignClient;
import com.bobocode.largestnasapic.dto.Photo;
import com.bobocode.largestnasapic.dto.PhotoWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.Optional;

import static java.net.http.HttpResponse.BodyHandlers.ofByteArray;
import static java.net.http.HttpResponse.BodyHandlers.ofString;

@Service
@Slf4j
public class NasaService {

  @Value(value = "${nasa.url}")
  private String NASA_URL;

  @Value(value = "${nasa.api.key}")
  private String API_KEY;

  @Autowired
  private NasaFeignClient nasaFeignClient;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @SneakyThrows
  @Cacheable("max_pic")
  public Optional<byte[]> getLargestPhoto(int sol, Optional<String> camera) {
    var request = createRequest(sol, camera);
    var httpClient = HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).build();
    log.info("Sending request to: {}", request.uri().toString());
    var response = nasaFeignClient.getLargestPicture(sol, API_KEY, camera);
    final JsonNode photos = response.get("photos");
    final ObjectMapper objectMapper = new ObjectMapper();
    final PhotoWrapper photoWrapper = objectMapper.readValue(photos.asText(), PhotoWrapper.class);
//    var response = httpClient.send(request, ofString());
//    var photoWrapper = objectMapper.readValue(response.body(), PhotoWrapper.class);
    SimpleImmutableEntry<String, Long> res = findMaxPictureUrlToSize(httpClient, photoWrapper);
    log.info("Sending request to: {}", res.getKey());
    var maxPicture = httpClient.send(createRequest(URI.create(res.getKey())), ofByteArray());
    return Optional.of(maxPicture.body());
  }

  private SimpleImmutableEntry<String, Long> findMaxPictureUrlToSize(HttpClient httpClient, PhotoWrapper photoWrapper) {
    return photoWrapper.getPhotos()
        .parallelStream()
        .map(Photo::getImgSrc)
        .map(img -> getImgToLength(httpClient, img))
        .max(Entry.comparingByValue())
        .orElseThrow();
  }

  private AbstractMap.SimpleImmutableEntry<String, Long> getImgToLength(HttpClient httpClient, String img) {
    try {
      var request = HttpRequest.newBuilder()
          .uri(URI.create(img))
          .method(HttpMethod.HEAD.name(), BodyPublishers.noBody())
          .build();
      log.debug("Sending HEAD request to {}", request.uri().toString());
      var headResponse = httpClient.send(request, ofString());
      var length = headResponse.headers().firstValueAsLong(HttpHeaders.CONTENT_LENGTH).orElse(0);
      return new AbstractMap.SimpleImmutableEntry<>(img, length);
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
    return new AbstractMap.SimpleImmutableEntry<>("noContent", 0L);
  }

  private HttpRequest createRequest(int sol, Optional<String> camera) {
    var uri = UriComponentsBuilder.fromHttpUrl(NASA_URL)
        .queryParam("sol", sol)
        .queryParam("api_key", API_KEY)
        .queryParamIfPresent("camera", camera)
        .build().toUri();

    return createRequest(uri);
  }

  private HttpRequest createRequest(URI uri) {
    return HttpRequest.newBuilder(uri).GET().build();
  }
}
