package pet.skillbox.sitesearchengine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DefaultController {

    @Autowired
    public DefaultController() {
    }

    @RequestMapping("/admin")
    public String index(Model model) {
        return "index";
    }
}