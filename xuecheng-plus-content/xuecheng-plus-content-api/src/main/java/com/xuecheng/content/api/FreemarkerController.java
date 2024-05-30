package com.xuecheng.content.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

// freemarker入门程序
@Controller
public class FreemarkerController {
    @GetMapping("/testfreemarker")
    public ModelAndView test(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name", "小明");
        modelAndView.setViewName("test"); // 根据视图名称＋.ftl找到模板
        return modelAndView;
    }
}
