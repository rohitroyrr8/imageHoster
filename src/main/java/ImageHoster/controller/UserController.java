package ImageHoster.controller;

import ImageHoster.model.Image;
import ImageHoster.model.User;
import ImageHoster.model.UserProfile;
import ImageHoster.service.ImageService;
import ImageHoster.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Controller
public class UserController {

    @Autowired
    private UserService userService;

    private Pattern pattern;
    private Matcher matcher;

    @Autowired
    private ImageService imageService;

    /**
     * to load up register page template
     *
     * @param model: instaced of model class
     * @param session : to implement password validation
     */
    @RequestMapping("users/registration")
    public String registration(Model model, HttpSession session) {
        User user = new User();
        UserProfile profile = new UserProfile();
        user.setProfile(profile);
        model.addAttribute("User", user);
        if(session.getAttribute("passwordTypeError") != null) {
            model.addAttribute("passwordTypeError", session.getAttribute("passwordTypeError"));
            session.removeAttribute("passwordTypeError");
        }
        return "users/registration";
    }

    /**
     * to register user after validating password
     *
     * @param user: instance of user class with user fileds
     * @param session : to implement password validation
     */
    @RequestMapping(value = "users/registration", method = RequestMethod.POST)
    public String registerUser(User user, HttpSession session) {
        final String PASSWORD_PATTERN = "((?=.*[a-z])(?=.*\\d)(?=.*[@#$%!]).{3,20})";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(user.getPassword());
        //to check if password matches above pattern
        if(matcher.matches()) {
            System.out.println("password is valid");
            userService.registerUser(user);
            return "redirect:/users/login";
        } else {
            System.out.println("password not valid");
            String error = "Password must contain atleast 1 alphabet, 1 number & 1 special character";
            session.setAttribute("passwordTypeError", error);
            return "redirect:/users/registration";
        }

    }

    /**
     * to load up login page template , if user already loggedIn, then redirect to home page
     *
     * @param session : to implement password validation
     */
    @RequestMapping("users/login")
    public String login(HttpSession session) {
        if(session.getAttribute("loggeduser") != null) {
            return "redirect:/images";
        }
        return "users/login";
    }

    /**
     * validaing user' username and password with one in database
     *
     * @param user: contains user fields
     * @param model: instance of model class
     * @param session : to implement password validation
     */
    @RequestMapping(value = "users/login", method = RequestMethod.POST)
    public String loginUser(User user, HttpSession session, Model model) {
        User existingUser = userService.login(user);
        if (existingUser != null) {
            session.setAttribute("loggeduser", existingUser);
            session.setAttribute("isLoggedIn", "true");
            return "redirect:/images";
        } else {
            String error = "Incorrect username or password. Try again..";
            model.addAttribute("loginError", error);
            return "users/login";
        }
    }

    /**
     * session is invalidated, All the images are fetched from the database and returned back to index.html page
     *
     * @param model: instance of model class
     * @param session : to implement password validation
     */
    @RequestMapping(value = "users/logout", method = RequestMethod.POST)
    public String logout(Model model, HttpSession session) {
        //destoring all the userdata from session
        session.invalidate();
        //fetching images from databse and return to index.html page
        List<Image> images = imageService.getAllImages();
        model.addAttribute("images", images);
        return "redirect:/";
    }
}
