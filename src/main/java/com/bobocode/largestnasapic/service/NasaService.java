package com.bobocode.largestnasapic.service;

import com.bobocode.largestnasapic.dto.NasaPictureRequest;
import com.bobocode.largestnasapic.dto.Photo;
import com.bobocode.largestnasapic.dto.PhotoWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.net.http.HttpResponse.BodyHandlers.ofByteArray;
import static java.net.http.HttpResponse.BodyHandlers.ofString;

@Service
@Slf4j
@RequiredArgsConstructor
public class NasaService {

  @Value(value = "${nasa.url}")
  private String NASA_URL;

  private Map<String, byte[]> photoStorage = new ConcurrentHashMap<>();

  private final ObjectMapper objectMapper = new ObjectMapper();

  private final RabbitTemplate rabbitTemplate;

  public String accept(NasaPictureRequest request) {
    var commandId = RandomStringUtils.random(5);
    request.setCommandId(commandId);
    rabbitTemplate.convertAndSend("nasa-pictures-exchange", "", request);
    return commandId;
  }

  @SneakyThrows
  @Cacheable("max_pic")
  public byte[] findLargestPhoto(int sol, Optional<String> camera) {
    var request = createRequest(sol, camera);
    var httpClient = HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).build();
    log.info("Sending request to: {}", request.uri().toString());
    var response = httpClient.send(request, ofString());
    var photoWrapper = objectMapper.readValue(response.body(), PhotoWrapper.class);
    SimpleImmutableEntry<String, Long> res = findMaxPictureUrlToSize(httpClient, photoWrapper);
    log.info("Sending request to: {}", res.getKey());
    var maxPicture = httpClient.send(createRequest(URI.create(res.getKey())), ofByteArray());
    return maxPicture.body();
  }

  public void addPhoto(String id, byte[] photo) {
    photoStorage.put(id, photo);
  }

  public byte[] getPhotoById(String commandId) {
    var pic = photoStorage.get(commandId);
    if (pic == null) {
      throw new NoSuchElementException("No picture found");
    }
      return pic;
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
        .queryParamIfPresent("camera", camera)
        .build().toUri();

    return createRequest(uri);
  }

  private HttpRequest createRequest(URI uri) {
    return HttpRequest.newBuilder(uri).GET().build();
  }
}
