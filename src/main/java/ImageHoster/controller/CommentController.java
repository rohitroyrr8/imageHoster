package ImageHoster.controller;

import ImageHoster.model.Comment;
import ImageHoster.model.Image;
import ImageHoster.model.User;
import ImageHoster.service.CommentService;
import ImageHoster.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;
import java.util.Date;

@Controller
public class CommentController {
    @Autowired
    private ImageService imageService;

    @Autowired
    private CommentService commentService;

    @RequestMapping(value = "/image/{id}/{title}/comments", method = RequestMethod.POST)
    public String create(@PathVariable("id") Integer imageId, Model model, HttpSession session, Comment comment) {
        if(session.getAttribute("loggeduser") == null) {
            return "redirect:/users/login";
        }
        User user = (User) session.getAttribute("loggeduser");
        Image image = imageService.getImage(imageId);
        Comment temp = new Comment();
        comment.setId(0);
        comment.setUser(user);
        comment.setImage(image);
        comment.setDate(new Date());
        System.out.println(comment.getId());
        commentService.addComment(comment);
        return "redirect:/images/" + image.getId();
    }
}
