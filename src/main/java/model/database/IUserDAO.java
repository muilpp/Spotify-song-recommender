package model.database;

import java.util.List;

public interface IUserDAO {

    /**
     * Method to CREATE a new user in the database
     * 
     * @param userName
     * @param accessToken
     * @param refreshToken
     * @return true if user persisted, false otherwise
     */
    public boolean addUser(String userName, String accessToken, String refreshToken);

    /**
     * Method to UPDATE user token
     * 
     * @param userName
     * @param oldToken
     * @param newToken
     * @return true if user updated, false otherwise
     */
    public boolean updateUserAccessToken(String userName, String oldToken, String newToken);

    /**
     * Method to DELETE an employee from the records
     * 
     * @param userName
     * @return true if user is deleted, false otherwise
     */
    public boolean deleteUser(String userName);

    /**
     * Method to READ all users
     * @return list of users in database
     */
    public List<User> getUsers();
}