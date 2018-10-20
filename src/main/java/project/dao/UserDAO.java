package project.dao;


import project.model.DAOResponse;
import project.model.User;
import project.model.UserYa;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.PreparedStatement;

@Service
public class UserDAO {

    private final JdbcTemplate template;

    public UserDAO(JdbcTemplate template) {
        this.template = template;
    }


    public DAOResponse<User> createUser(User body)  {
        DAOResponse<User> result = new DAOResponse<>(new UserYa(), HttpStatus.CREATED);

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement statement = con.prepareStatement(
                        "INSERT INTO \"user\"(login, email, token)" + " VALUES(?, ?, ?) returning id;" ,
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setString(1, body.getLogin());
                statement.setString(2, body.getDefault_email());
                statement.setString(3, body.getToken());
                return statement;
            }, keyHolder);

            result.body.setId(BigDecimal.valueOf(keyHolder.getKey().intValue()));
            result.status = HttpStatus.CREATED;
        }
        catch (DuplicateKeyException e) {
            result.body = null;
            result.status = HttpStatus.CONFLICT;
        }
        return result;
    }

    public DAOResponse<Integer> getUserID(String email) {
        DAOResponse<Integer> result = new DAOResponse<>();
        try {
            final UserYa user =  template.queryForObject(
                    "SELECT * FROM \"user\" WHERE email = ?",
                    new Object[]{email},  Mappers.userMapper);

            result.body = user.getId().intValue();
            result.status = HttpStatus.OK;
        }
        catch (DataAccessException e) {
            result.body = null;
            result.status = HttpStatus.NOT_FOUND;
        }
        return result;

    }
}
