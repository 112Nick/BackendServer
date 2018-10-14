package project.dao;

import org.springframework.jdbc.core.RowMapper;
import project.model.Page;
import project.model.User;

import java.sql.Array;

public class Mappers {

    public static final RowMapper<Page> pageMapper = (res, num) -> {
        String uuid = res.getString("uuid");
        Integer ownerID = res.getInt("ownerid");
        String title = res.getString("title");
        Boolean isPublic = res.getBoolean("ispublic");
        Boolean isStatic = res.getBoolean("isstatic");
        Array fieldsNames = res.getArray("fieldsnames");
        Array fieldsValues = res.getArray("fieldsvalues");
        String date = res.getString("date");

        return new Page(uuid, ownerID, title, isPublic, isStatic, true, (String[])fieldsNames.getArray(), (String[])fieldsValues.getArray(), date);
    };

    public static final RowMapper<Page> pageFullMapper = (res, num) -> {
        String uuid = res.getString("uuid");
        Integer ownerID = res.getInt("ownerid");
        String title = res.getString("title");
        Boolean isPublic = res.getBoolean("ispublic");
        Boolean isStatic = res.getBoolean("isstatic");
        Boolean isMine = res.getBoolean("ismine");
        Array fieldsNames = res.getArray("fieldsnames");
        Array fieldsValues = res.getArray("fieldsvalues");
        String date = res.getString("date");

        return new Page(uuid, ownerID, title, isPublic, isStatic, isMine, (String[])fieldsNames.getArray(), (String[])fieldsValues.getArray(), date);
    };

    public static final RowMapper<User> userMapper = (res, num) -> {
        Integer id = res.getInt("id");
        String login = res.getString("login");
        String email = res.getString("email");
        String token = res.getString("token");
        return new User(id, login, email, token);
    };
}
