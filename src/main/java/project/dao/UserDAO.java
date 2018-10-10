package project.dao;


import project.model.DAOResponse;
import project.model.User;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;

@Service
public class UserDAO {

    private final JdbcTemplate template;

    public UserDAO(JdbcTemplate template) {
        this.template = template;
    }


    public DAOResponse<User> createUser(User body)  {
        DAOResponse<User> result = new DAOResponse<>(new User(), HttpStatus.CREATED);

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement statement = con.prepareStatement(
                        "INSERT INTO \"user\"(login, email, token)" + " VALUES(?, ?, ?) returning id;" ,
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setString(1, body.getLogin());
                statement.setString(2, body.getDefaultEmail());
                statement.setString(3, body.getToken());
                return statement;
            }, keyHolder);

//            User user = null;
//            try {
//                 user =  template.queryForObject(
//                    "SELECT * FROM user WHERE login = ?",
//                    new Object[]{body.getLogin()},  userMapper);
//            } catch (DataAccessException e) {
//                result.status = HttpStatus.NOT_FOUND;
//            }

            result.body.setId(keyHolder.getKey().intValue());
//            if (result == null) {
//                System.out.println("result");
//            } else {
//                if (result.body == null) {
//                    System.out.println("result.body");
//                } else {
////                    if (user == null) {
////                        System.out.println("user");
////                    } else  {
////                        if (user.getId() == null) {
////                            System.out.println("user");
//                        }
//                    }
//                }
//            }
//            result.body.setId(user.getId());
            result.status = HttpStatus.CREATED;
        }
        catch (DuplicateKeyException e) {
            result.body = null;
            result.status = HttpStatus.CONFLICT;
        }
        return result;
    }

//    public DAOResponse<User> getUserByNickname(String nickname)  {
//        DAOResponse<User> result = new DAOResponse<>();
//        try {
//            final User user =  template.queryForObject(
//                    "SELECT * FROM user WHERE login = ?::citext",
//                    new Object[]{nickname},  userMapper);
//            result.status = HttpStatus.OK;
//            result.body = user;
//        }
//        catch (DataAccessException e) {
//            result.status = HttpStatus.NOT_FOUND;
//            result.body = null;
//        }
//        return result;
//
//    }


    public DAOResponse<Integer> getUserID(String email) {
        DAOResponse<Integer> result = new DAOResponse<>();
        try {
            final User user =  template.queryForObject(
                    "SELECT * FROM user WHERE email = ?",
                    new Object[]{email},  userMapper);

            result.body = user.getId();
            result.status = HttpStatus.OK;
        }
        catch (DataAccessException e) {
            result.body = null;
            result.status = HttpStatus.NOT_FOUND;
        }
        return result;

    }

    public DAOResponse<User> addViewedPage(Integer userID, String pageID, String pageTitle, String date)  {
        DAOResponse<User> result = new DAOResponse<>();
        result.body = null;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement statement = con.prepareStatement(
                        "INSERT INTO userpages(userid, pageuuid, title, date)" + " VALUES(?, ?, ?, ?)" ,
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setInt(1, userID);
                statement.setString(2, pageID);
                statement.setString(3, pageTitle);
                statement.setString(4, date);
                return statement;
            }, keyHolder);
            result.status = HttpStatus.CREATED;
        }
        catch (DuplicateKeyException e) {
            result.status = HttpStatus.CONFLICT;
        }
        return result;
    }

    public static final RowMapper<User> userMapper = (res, num) -> {
        Integer id = res.getInt("id");
        String login = res.getString("login");
        String email = res.getString("email");
        String token = res.getString("token");
        return new User(id, login, email, token);
    };
}
