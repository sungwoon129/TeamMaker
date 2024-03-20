package io.wauction.core.channels.presentation;

import io.wauction.core.channels.application.ChannelService;
import io.wauction.core.channels.application.CreateAuctionRoomService;
import io.wauction.core.channels.dto.ChannelRequest;
import io.wauction.core.channels.dto.ChannelResponse;
import io.wauction.core.channels.entity.Channel;
import io.wauction.core.common.dto.CommonResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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
    private final CreateAuctionRoomService createAuctionRoomService;


    @GetMapping("/")
    public ModelAndView getListView() {

        List<Channel> channelList = channelService.findAll();
        List<ChannelResponse> list = channelList.stream().map(Channel::toResponseDto).toList();
        ModelAndView mv = new ModelAndView("index");
        mv.addObject("list",list);

        return mv;
    }

    @GetMapping("/channel/{id}")
    public ModelAndView getDetailView(@PathVariable Long id, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("channel/channel");
        Channel channel = channelService.findOne(id);
        ChannelResponse channelResponse = channel.toResponseDto();
        mv.addObject("channel",channelResponse);

        Cookie cookie = new Cookie("rname", channelResponse.getClientRole().getName());
        cookie.setMaxAge(7200);
        cookie.setPath("/");
        response.addCookie(cookie);

        return mv;
    }

    @ResponseBody
    @PostMapping("/channel")
    public ResponseEntity<CommonResponse<Long>> createChannel(@RequestBody ChannelRequest channelRequest) throws URISyntaxException {
        Long channelId = createAuctionRoomService.createAuctionRoom(channelRequest);
        return new ResponseEntity<>(new CommonResponse<>(channelId),HttpStatus.CREATED);
    }
}
