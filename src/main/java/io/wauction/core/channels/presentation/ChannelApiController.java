package io.wauction.core.channels.presentation;

import io.wauction.core.channels.application.ChannelService;
import io.wauction.core.channels.dto.ChannelResponse;
import io.wauction.core.channels.entity.Channel;
import io.wauction.core.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
public class ChannelApiController {

    private final ChannelService channelService;

    @GetMapping("/api/channel/{id}")
    public ResponseEntity<CommonResponse<ChannelResponse>> getChannelData(@PathVariable Long id) {
        Channel channel = channelService.findOne(id);
        return ResponseEntity.ok().body(new CommonResponse<>(channel.toResponseDto()));
    }

}
