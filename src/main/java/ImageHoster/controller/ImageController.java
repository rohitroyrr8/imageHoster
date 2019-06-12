package ImageHoster.controller;

import ImageHoster.model.Comment;
import ImageHoster.model.Image;
import ImageHoster.model.User;
import ImageHoster.service.CommentService;
import ImageHoster.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Controller
public class ImageController {

    @Autowired
    private ImageService imageService;

    @Autowired
    private CommentService commentService;

    /**
     * This method displays all the images in the user home page after successful login
     *
     * @param model: instead of model class
     * @param session : to verify if user is logged-in or not
     */
    @RequestMapping("images")
    public String getUserImages(Model model, HttpSession session) {
        if(session.getAttribute("loggeduser") == null) {
            return "redirect:/users/login";
        }
        User loggedInUser = (User) session.getAttribute("loggeduser");
        List<Image> images = imageService.getAllImages(loggedInUser);
        model.addAttribute("images", images);
        return "images";
    }

    /**
     * This method displays all the details of a image a well as user can edit/delete/comment on that image
     *
     * @param id : id of a image
     * @param model: instead of model class
     * @param session : to verify if user is logged-in or not
     */
    @RequestMapping("/images/{id}")
    public String showImage(@PathVariable("id") Integer id, Model model, HttpSession session) {
        Image image = imageService.getImageById(id);
        model.addAttribute("image", image);
        if(image == null) {
            return "redirect:/";
        }
        List<Comment> comments = commentService.getComments(image);
        model.addAttribute("comments", comments);

        if(session.getAttribute("editError") != null) {
            model.addAttribute("editError", session.getAttribute("editError"));
            session.removeAttribute("editError");
        }
        if(session.getAttribute("deleteError") != null) {
            model.addAttribute("deleteError", session.getAttribute("deleteError"));
            session.removeAttribute("deleteError");
        }
        System.out.println(comments);
        return "images/image";
    }

    /**
     * This method display image upload page template when hitting '/images/upload'
     * path and incoming request is of  type 'GET'
     * @param session : to verify if user is logged-in or not
     */

    @RequestMapping("/images/upload")
    public String newImage(HttpSession session) {
        if(session.getAttribute("loggeduser") == null) {
            return "users/login";
        }
        return "images/upload";
    }

    //This controller method is called when the request pattern is of type 'images/upload' and also the incoming request is of POST type
    //The method receives all the details of the image to be stored in the database, and now the image will be sent to the business logic to be persisted in the database
    //After you get the imageFile, set the user of the image by getting the logged in user from the Http Session
    //Convert the image to Base64 format and store it as a string in the 'imageFile' attribute
    //Set the date on which the image is posted
    //After storing the image, this method directs to the logged in user homepage displaying all the images

    /**
     * This method upload image to the systsm only when hitting '/images/upload'
     * path and incoming request is of  type 'POST'
     * @param session : to verify if user is logged-in or not
     * @param file : image file to upload to server
     * @throws : IOException
     */
    @RequestMapping(value = "/images/upload", method = RequestMethod.POST)
    public String createImage(@RequestParam("file") MultipartFile file, Image newImage, HttpSession session) throws IOException {
        if(session.getAttribute("loggeduser") == null) {
            return "users/login";
        }
        User user = (User) session.getAttribute("loggeduser");
        newImage.setUser(user);
        String uploadedImageData = convertUploadedFileToBase64(file);
        newImage.setImageFile(uploadedImageData);
        newImage.setDate(new Date());
        imageService.uploadImage(newImage);
        return "redirect:/images";
    }

    //This controller method is called when the request pattern is of type 'editImage'
    //This method fetches the image with the corresponding id from the database and adds it to the model with the key as 'image'
    //The method then returns 'images/edit.html' file wherein you fill all the updated details of the image
    /**
     * This method display image edit  page template when hitting '/editImage?imageId=image_id'
     * path and incoming request is of  type 'GET'
     * @param imageId : unique id of image
     * @param session : to verify if user is logged-in or not
     */
    @RequestMapping(value = "/editImage")
    public String editImage(@RequestParam("imageId") Integer imageId, Model model, HttpSession session) {
        if(session.getAttribute("loggeduser") == null) {
            return "users/login";
        }
        Image image = imageService.getImage(imageId);
        //to check whether image owner is same as logged-in user
        User loggedInUser = (User) session.getAttribute("loggeduser");
        if(loggedInUser.getId() == image.getUser().getId()) {
            model.addAttribute("image", image);
            return "images/edit";
        }
        String error = "Only the owner of the image can edit the image.";
        session.setAttribute("editError", error);
        return "redirect:/images/" + image.getId();
    }

    /**
     * This controller method is called when the request pattern is of type 'images/edit' and also the incoming request is of PUT type
     * path and incoming request is of  type 'GET'
     * @param imageId : unique id of image
     * @param updatedImage : image that we need to update
     * @param session : to verify if user is logged-in or not
     *
     */
    @RequestMapping(value = "/editImage", method = RequestMethod.PUT)
    public String editImageSubmit(@RequestParam("file") MultipartFile file, @RequestParam("imageId") Integer imageId, Image updatedImage, HttpSession session) throws IOException {
        if(session.getAttribute("loggeduser") == null) {
            return "users/login";
        }

        Image image = imageService.getImage(imageId);
        String updatedImageData = convertUploadedFileToBase64(file);

        if (updatedImageData.isEmpty())
            updatedImage.setImageFile(image.getImageFile());
        else {
            updatedImage.setImageFile(updatedImageData);
        }

        updatedImage.setId(imageId);
        User user = (User) session.getAttribute("loggeduser");
        updatedImage.setUser(user);
        updatedImage.setDate(new Date());

        imageService.updateImage(updatedImage);
        return "redirect:/images/" + updatedImage.getId();
    }

    /**
     * This controller method is called when the request pattern is of type 'deleteImage'
     * and also the incoming request is of DELETE type
     *
     * @param imageId : unique id of image
     * @param session : to verify if user is logged-in or not
     *
     */
    @RequestMapping(value = "/deleteImage", method = RequestMethod.DELETE)
    public String deleteImageSubmit(@RequestParam(name = "imageId") Integer imageId, HttpSession session) {
        //to check whether user is logged-in or not
        if(session.getAttribute("loggeduser") == null) {
            return "users/login";
        }
        Image image = imageService.getImage(imageId);
        User loggedInUser = (User) session.getAttribute("loggeduser");
        //to check whether image owner is same as logged-in user
        if(loggedInUser.getId() == image.getUser().getId()) {
            imageService.deleteImage(imageId);
            return "redirect:/images";
        }
        String error = "Only the owner of the image can delete the image.";
        session.setAttribute("deleteError", error);
        return "redirect:/images/" + image.getId();
    }

    /**
     * This method is user to covert image to base64 byte code
     *
     * @param file: iamge file that need to convert
     * @return string of base64 encoding string
     * @throws IOException
     *
     */
    private String convertUploadedFileToBase64(MultipartFile file) throws IOException {
        return Base64.getEncoder().encodeToString(file.getBytes());
    }
}
