package model.database;

import java.util.List;
import java.util.logging.Logger;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class UserDAO implements IUserDAO {
    private final static Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    @Override
    public boolean addUser(String userName, String accessToken, String refreshToken) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        Integer userID = 0;
        try {
            tx = session.beginTransaction();
            User user = new User(userName, accessToken, refreshToken);
            userID = (Integer) session.save(user);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return userID > 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<User> getUsers() {
        Session session = HibernateUtil.getSessionFactory().openSession();

        return (List<User>) session.createQuery("from User ").list();
    }

    @Override
    public boolean updateUserAccessToken(String userName, String oldToken, String newToken) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.getTransaction().begin();

        Query query = session
                .createQuery("update User set access_token = :newToken where access_token = :oldToken");
        query.setParameter("oldToken", oldToken);
        query.setParameter("newToken", newToken);

        int rowCount = query.executeUpdate();
        LOGGER.info("Updated -> " + rowCount);
        session.getTransaction().commit();

        return rowCount > 0;
    }

    @Override
    public boolean deleteUser(String userName) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.getTransaction().begin();

        Query query = session.createQuery("delete User where user_name = :userName");
        query.setParameter("userName", userName);
        int rowCount = query.executeUpdate();

        LOGGER.info("Deleted -> " + rowCount);
        session.getTransaction().commit();

        return rowCount > 0;
    }
}