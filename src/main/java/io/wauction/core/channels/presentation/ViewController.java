package io.wauction.core.channels.presentation;

import io.wauction.core.channels.application.ChannelService;
import io.wauction.core.channels.dto.ChannelRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/channels")
public class ViewController {

    private final ChannelService channelService;


    @GetMapping("/list")
    public ModelAndView getListView() {
        ModelAndView mv = new ModelAndView(("/channel/list"));
        mv.addObject("list",channelService.findAll());

        return mv;
    }

    @GetMapping("/{id}")
    public ModelAndView getDetailView(@PathVariable Long id) {
        ModelAndView mv = new ModelAndView(("/channel/channel"));
        mv.addObject("info",channelService.findOne(id));

        return mv;
    }

    @PostMapping("/channel")

    public String createChannel(ChannelRequest channelRequest) {
        channelService.create(channelRequest);
        return "redirect:/channel/channel";
    }
}
