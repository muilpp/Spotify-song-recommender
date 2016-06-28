package model;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import controller.webservice.RecommendAPI;
import model.database.IUserDAO;
import model.database.User;
import model.database.UserDAO;
import model.webservice_data.Token;

public class UserSongsJob implements Job{
    private final static Logger LOGGER = Logger.getLogger(UserSongsJob.class.getName());

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        IUserDAO userDAO = new UserDAO();
        ISpotifyAPI spotifyAPI = new SpotifyAPI();
        RecommendAPI recsAPI = new RecommendAPI();

        List<User> userList = userDAO.getUsers();
        LOGGER.info("user list size -> " + userList.size());

        for (User user : userList) {
            LOGGER.info("update user -> " + user.getUserName());
            Token userToken = spotifyAPI.refreshToken(user.getRefreshToken());
            recsAPI.getRecommendations(userToken);

            try {
                //wait 10s between each user
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
}