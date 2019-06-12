package ImageHoster.repository;

import ImageHoster.model.Image;
import ImageHoster.model.User;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.util.List;

//The annotation is a special type of @Component annotation which describes that the class defines a data repository
@Repository
public class ImageRepository {

    //Get an instance of EntityManagerFactory from persistence unit with name as 'imageHoster'
    @PersistenceUnit(unitName = "imageHoster")
    private EntityManagerFactory emf;


    //The method receives the Image object to be persisted in the database
    //Creates an instance of EntityManager
    //Starts a transaction
    //The transaction is committed if it is successful
    //The transaction is rolled back in case of unsuccessful transaction
    public Image uploadImage(Image newImage) {

        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            em.persist(newImage);
            transaction.commit();
        } catch (Exception e) {
            System.out.println(e);
            transaction.rollback();
        }
        return newImage;
    }

    //The method creates an instance of EntityManager
    //Executes JPQL query to fetch all the images from the database
    //Returns the list of all the images fetched from the database
    public List<Image> getAllImages() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Image> query = em.createQuery("SELECT i from Image i", Image.class);
        List<Image> resultList = query.getResultList();
        return resultList;
    }

    public List<Image> getAllImages(User user) {
        EntityManager em = emf.createEntityManager();
        try{
            TypedQuery<Image> typedQuery = em.createQuery("SELECT i from Image i where i.user =:user", Image.class);
            typedQuery.setParameter("user", user);
            List<Image> resultList =  typedQuery.getResultList();
            return resultList;
        }catch (Exception e) {
            return null;
        }
    }

    public Image getImageById(Integer imageid) {
        EntityManager em = emf.createEntityManager();
        try{
            return  em.find(Image.class, imageid);
        }catch(Exception e) {
          return null;
        }
    }

    //The method creates an instance of EntityManager
    //Executes JPQL query to fetch the image from the database with corresponding title
    //Returns the image in case the image is found in the database
    //Returns null if no image is found in the database
    public Image getImageByTitle(String title) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Image> typedQuery = em.createQuery("SELECT i from Image i where i.title =:title", Image.class).setParameter("title", title);
            return typedQuery.getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public Image getImage(Integer imageId) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Image> typedQuery = em.createQuery("SELECT i from Image i where i.id =:imageId", Image.class).setParameter("imageId", imageId);
        Image image = typedQuery.getSingleResult();
        return image;
    }

    //The method receives the Image object to be updated in the database
    //Creates an instance of EntityManager
    //Starts a transaction
    //The transaction is committed if it is successful
    //The transaction is rolled back in case of unsuccessful transaction
    public void updateImage(Image updatedImage) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            em.merge(updatedImage);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        }
    }

    public void deleteImage(Integer imageId) {
     EntityManager em = emf.createEntityManager();
     EntityTransaction et = em.getTransaction();
        try{
            et.begin();
            Image image = em.find( Image.class, imageId);
            em.remove(image);
            et.commit();

        }catch(Exception e) {
            et.rollback();
        }
    }

}
