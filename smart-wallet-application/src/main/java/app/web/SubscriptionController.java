package app.web;

import app.user.model.User;
import app.user.service.UserService;
import org.hibernate.internal.build.AllowNonPortable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/subscriptions")
public class SubscriptionController {
    private final UserService userService;
    @Autowired
    public SubscriptionController(UserService userService) {
        this.userService = userService;
    }
@GetMapping
    public String getUpgradePage(){
        return "upgrade";
    }

    @GetMapping("/history")
    public ModelAndView getUserSubscriptions(){
        User user = userService.getById(UUID.fromString("edfe1051-71bc-4def-ab3f-1e4bbe8d9bfd"));

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("subscription-history");
        modelAndView.addObject("user", user);
        return modelAndView;
    }
}
