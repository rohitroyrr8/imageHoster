package ImageHoster.repository;

import ImageHoster.model.Comment;
import ImageHoster.model.Image;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.util.List;

@Repository
public class CommentRepository {

    @PersistenceUnit(unitName = "imageHoster")
    private EntityManagerFactory emf;

    public void addComment(Comment comment) {
        EntityManager manager = emf.createEntityManager();
        EntityTransaction transaction = manager.getTransaction();

        try{
            transaction.begin();
            manager.merge(comment);
            transaction.commit();
        }catch (Exception e) {
            System.out.println(e);
            transaction.rollback();
        }
    }

    public List<Comment> getComments(Image image) {
        try{
            EntityManager manager = emf.createEntityManager();
            TypedQuery<Comment> query = manager.createQuery("SELECT i from Comment i where i.image =:image ", Comment.class);
            query.setParameter("image", image);
            List<Comment> resultList = query.getResultList();
            return resultList;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}
