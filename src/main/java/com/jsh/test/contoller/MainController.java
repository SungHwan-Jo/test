package com.jsh.test.contoller;

import com.jsh.test.service.ClassFindService;
import com.jsh.test.service.OOMService;
import com.jsh.test.service.ThreadSleepService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;

@Controller
public class MainController {
    private final ThreadSleepService threadSleepService;
    private final OOMService oomService;
    private final ClassFindService classFindService;

    public MainController(ThreadSleepService threadSleepService, OOMService oomService, ClassFindService classFindService) {
        this.threadSleepService = threadSleepService;
        this.oomService = oomService;
        this.classFindService = classFindService;
    }

    @GetMapping("/main")
    public String main(Model model, HttpServletRequest request){
        HttpSession session = request.getSession();
        String session_id = session.getId();
        long session_create = session.getCreationTime();
        long session_last = session.getLastAccessedTime();
        int maxInactiveInterval = session.getMaxInactiveInterval();

        model.addAttribute("session_id", session_id);
        model.addAttribute("session_create", new Date(session_create));
        model.addAttribute("session_last", new Date(session_last));
        model.addAttribute("maxInactiveInterval", maxInactiveInterval);


        return "main";
    }

    @GetMapping("/sleep_start")
    public String sleep_start(Model model){
        threadSleepService.threadSleep_start();
        return "redirect:/main";
    }

    @GetMapping("/sleep_stop")
    public String sleep_stop(Model model){
        threadSleepService.threadSleep_stop();
        return "redirect:/main";
    }

    @GetMapping("/oom_start")
    public String oom_start(Model model){
        oomService.oom_start();
        return "redirect:/main";
    }

    @GetMapping("/class_find")
    public String class_find(Model model, HttpServletRequest requeset){
        String findcp = requeset.getParameter("findcp");
        String action = requeset.getParameter("action");
        String resource = requeset.getParameter("resource");
        classFindService.setClassFindService(findcp, action, resource);
        ArrayList<String> arrayList = classFindService.getClassPath();
        model.addAttribute("name", arrayList.get(0));
        model.addAttribute("location", arrayList.get(1));
        model.addAttribute("classloader", arrayList.get(2));
        model.addAttribute("superclass", arrayList.get(3));
        model.addAttribute("interface", arrayList.get(4));
        model.addAttribute("primitive", arrayList.get(5));

        return "main";
    }
}
