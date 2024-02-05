package io.wauction.core.channels.presentation;

import io.wauction.core.channels.application.ChannelService;
import io.wauction.core.channels.dto.ChannelRequest;
import io.wauction.core.channels.entity.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

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
        mv.addObject("info",channelService.findOne(id));

        return mv;
    }

    @PostMapping("/channel")
    public String createChannel(ChannelRequest channelRequest) {
        channelService.create(channelRequest);
        return "redirect:/channel/channel";
    }
}
