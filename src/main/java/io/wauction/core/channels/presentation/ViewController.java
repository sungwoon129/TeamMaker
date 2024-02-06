package io.wauction.core.channels.presentation;

import io.wauction.core.channels.application.ChannelService;
import io.wauction.core.channels.dto.ChannelRequest;
import io.wauction.core.channels.entity.Channel;
import io.wauction.core.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.net.URISyntaxException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final ChannelService channelService;


    @GetMapping("/list")
    public ModelAndView getListView() {

        List<Channel> channelList = channelService.findAll();
        ModelAndView mv = new ModelAndView("index");
        mv.addObject("list",channelList);

        return mv;
    }

    @GetMapping("/channel/{id}")
    public ModelAndView getDetailView(@PathVariable Long id) {
        ModelAndView mv = new ModelAndView("channel/channel");
        mv.addObject("channel",channelService.findOne(id));

        return mv;
    }

    @ResponseBody
    @PostMapping("/channel")
    public ResponseEntity<CommonResponse<Long>> createChannel(@RequestBody ChannelRequest channelRequest) throws URISyntaxException {
        Long channelId = channelService.create(channelRequest);
        return new ResponseEntity<>(new CommonResponse<>(channelId),HttpStatus.CREATED);
    }
}
