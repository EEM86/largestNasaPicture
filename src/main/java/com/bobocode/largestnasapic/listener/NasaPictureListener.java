package com.bobocode.largestnasapic.listener;

import com.bobocode.largestnasapic.dto.NasaPictureRequest;
import com.bobocode.largestnasapic.service.NasaService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NasaPictureListener {

  private final NasaService nasaService;

  @RabbitListener(queues = "largest-picture-command-queue")
  public void processMessages(NasaPictureRequest request) {
    var picBytes = nasaService.findLargestPhoto(request.getSol(), Optional.ofNullable(request.getCamera()));
    nasaService.addPhoto(request.getCommandId(), picBytes);
  }
}
