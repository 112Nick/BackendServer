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
        DAOResponse<User> result = new DAOResponse<>();

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement statement = con.prepareStatement(
                        "INSERT INTO user(login, password)" + " VALUES(?,?)" ,
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setString(1, body.getLogin());
                statement.setString(2, body.getPassword());
                return statement;
            }, keyHolder);
            result.body.setId(keyHolder.getKey().intValue());
            result.status = HttpStatus.CREATED;
        }
        catch (DuplicateKeyException e) {
            result.body = null;
            result.status = HttpStatus.CONFLICT;
        }
        return result;
    }

    public DAOResponse<User> getUserByNickname(String nickname)  {
        DAOResponse<User> result = new DAOResponse<>();
        try {
            final User user =  template.queryForObject(
                    "SELECT * FROM user WHERE login = ?::citext",
                    new Object[]{nickname},  userMapper);
            result.status = HttpStatus.OK;
            result.body = user;
        }
        catch (DataAccessException e) {
            result.status = HttpStatus.NOT_FOUND;
            result.body = null;
        }
        return result;

    }

    public DAOResponse<User> addViewedPage(Integer userID, Integer pageID, String pageTitle)  {
        DAOResponse<User> result = new DAOResponse<>();
        result.body = null;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement statement = con.prepareStatement(
                        "INSERT INTO userpages(userid, pageid, title)" + " VALUES(?,?,?)" ,
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setInt(1, userID);
                statement.setInt(2, pageID);
                statement.setString(3, pageTitle);
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
        String password = res.getString("password");
        return new User(login, password);
    };
}
