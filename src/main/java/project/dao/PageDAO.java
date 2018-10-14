package project.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import project.model.Page;
import project.model.DAOResponse;
import project.model.User;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

@Service
public class PageDAO {

    private final JdbcTemplate template;

    public PageDAO(JdbcTemplate template) {
        this.template = template;
    }

    public DAOResponse<Page> createPage(Page body)  {
        DAOResponse<Page> result = new DAOResponse<>();
        result.body = null;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement statement = con.prepareStatement(
                        "INSERT INTO page(uuid, ownerid, title, ispublic, isstatic, fieldsnames, fieldsvalues, date)" +
                                " VALUES(?, ?, ?, ?, ?, ?, ?, ?)" ,
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setString(1, body.getUuid());
                statement.setInt(2, body.getOwnerID());
                statement.setString(3, body.getTitle());
                statement.setBoolean(4, body.isPublic());
                statement.setBoolean(5, body.isStatic());
                statement.setArray(6, con.createArrayOf("TEXT", body.getFieldsNames()));
                statement.setArray(7, con.createArrayOf("TEXT", body.getFieldsValues()));
                statement.setString(8, body.getDate());
                return statement;
            }, keyHolder);
            result.status = HttpStatus.CREATED;
            return result;
        }
        catch (DuplicateKeyException e) {
            e.printStackTrace();
            result.status = HttpStatus.CONFLICT;
        }
        return result;

    }

    public DAOResponse<Page> getPageByID(String pageUUID) {
        DAOResponse<Page> result = new DAOResponse<>();
        try {
            final Page foundPage =  template.queryForObject(
                    "SELECT * FROM page WHERE uuid = ?",
                    new Object[]{pageUUID},  Mappers.pageMapper);
            result.body = foundPage;
            result.status = HttpStatus.OK;
        }
        catch (DataAccessException e) {
            e.printStackTrace();
            result.body = null;
            result.status = HttpStatus.NOT_FOUND;
        }
        return result;

    }


    public DAOResponse<Page> editPage(Page body, String pageUUID) {
        DAOResponse<Page> result = new DAOResponse<>();
        result.body = null;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement statement = con.prepareStatement(
                        "UPDATE page SET " +
                                " title = ?," +
                                " ispublic = ?, " +
                                " isstatic = ?, " +
                                " fieldsnames = ?, " +
                                " fieldsvalues = ? " +
                                "WHERE uuid = ?",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setString(1 , body.getTitle());
                statement.setBoolean(2, body.isPublic());
                statement.setBoolean(3, body.isStatic());
                statement.setArray(4, con.createArrayOf("TEXT", body.getFieldsNames()));
                statement.setArray(5, con.createArrayOf("TEXT", body.getFieldsValues()));
                statement.setString(6, pageUUID);
                return statement;
            }, keyHolder);
            result.status = HttpStatus.OK;
        } catch(DuplicateKeyException e){
            e.printStackTrace();
            result.status = HttpStatus.CONFLICT;
        }
        return result;
    }

    public DAOResponse<Page> deletePage(String pageUUID) {
        DAOResponse<Page> result = new DAOResponse<>();
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                    PreparedStatement statement = con.prepareStatement(
                            "DELETE FROM page WHERE uuid = ?",
                            PreparedStatement.RETURN_GENERATED_KEYS);
                    statement.setString(1 , pageUUID);
                    return statement;
                    }, keyHolder);
            result.status = HttpStatus.OK;
        }
        catch (Exception e) {
            e.printStackTrace();
            result.status = HttpStatus.NOT_FOUND;
        }
        return result;

    }

    public DAOResponse<Page> deletePageFromViewers(String pageUUID) {
        DAOResponse<Page> result = new DAOResponse<>();
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement statement = con.prepareStatement(
                        "DELETE FROM userpages WHERE pageuuid = ?",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setString(1 , pageUUID);
                return statement;
            }, keyHolder);
            result.status = HttpStatus.OK;
        }
        catch (Exception e) {
            e.printStackTrace();
            result.status = HttpStatus.NOT_FOUND;
        }
        return result;
    }

    public DAOResponse<User> addViewedPage(Integer userID, String pageID)  {
        DAOResponse<User> result = new DAOResponse<>();
        result.body = null;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement statement = con.prepareStatement(
                        "INSERT INTO userpages(userid, pageuuid)" + " VALUES(?, ?)" ,
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setInt(1, userID);
                statement.setString(2, pageID);
                return statement;
            }, keyHolder);
            result.status = HttpStatus.CREATED;
        }
        catch (DuplicateKeyException e) {
            result.status = HttpStatus.CONFLICT;
        }
        return result;
    }


    public DAOResponse<List<Page>> getUsersPages(Integer userID, String sort, String own, String search) {
        DAOResponse<List<Page>> daoResponse = new DAOResponse<>();
        List<Object> tmpObj = new ArrayList<>();
        tmpObj.add(userID);
        tmpObj.add(userID);
        String sqlQuery;

        if (sort == null || sort.equals("")) {
            sort = "a-z";
        }
        if (own == null || own.equals("")) {
            own = "all";
        }

        switch(own) {
            case "me":
                sqlQuery = "SELECT uuid, ownerid, title, ispublic, isstatic, template, fieldsnames, fieldsvalues, date, " +
                        "CASE WHEN ownerid = ? THEN true ELSE false END AS ismine " +
                        "FROM page WHERE ownerid = ?";
                break;
            case "others":
                sqlQuery = "SELECT uuid, ownerid, title, ispublic, isstatic, template, fieldsnames, fieldsvalues, date, " +
                        "CASE WHEN ownerid = ? THEN true ELSE false END AS ismine " +
                        "FROM userpages JOIN page ON pageuuid = uuid WHERE userid = ? ";

                break;
            case "all":
                tmpObj.add(userID);
                tmpObj.add(userID);
                sqlQuery = "SELECT * FROM (SELECT uuid, ownerid, title, ispublic, isstatic, template, fieldsnames, fieldsvalues, date, " +
                        "CASE WHEN ownerid = ? THEN true ELSE false END AS ismine " +
                        "FROM page WHERE ownerid = ? " +
                        "UNION SELECT uuid, ownerid, title, ispublic, isstatic, template, fieldsnames, fieldsvalues, date, " +
                        "CASE WHEN ownerid = ? THEN true ELSE false END AS ismine " +
                        "FROM userpages JOIN page ON pageuuid = uuid WHERE userid = ?) as h";
                break;
            default:
                tmpObj.add(userID);
                tmpObj.add(userID);
                sqlQuery = "SELECT * FROM (SELECT uuid, ownerid, title, ispublic, isstatic, template, fieldsnames, fieldsvalues, date, " +
                        "CASE WHEN ownerid = ? THEN true ELSE false END AS ismine  " +
                        "FROM page WHERE ownerid = ? " +
                        "UNION SELECT uuid, ownerid, title, ispublic, isstatic, template, fieldsnames, fieldsvalues, date, " +
                        "CASE WHEN ownerid = ? THEN true ELSE false END AS ismine " +
                        "FROM userpages JOIN page ON pageuuid = uuid WHERE userid = ?) as h";
                break;
        }

        if (search != null && !search.isEmpty()) {
            sqlQuery += " WHERE title LIKE '%" + search + "%'";
        }

        switch (sort) {
            case "a-z":
                sqlQuery += " ORDER BY title";
                break;
            case "z-a":
                sqlQuery += " ORDER BY title DESC";
                break;
            case "date":
                sqlQuery += " ORDER BY date DESC";
                break;
        }

        try {
            daoResponse.body = template.query( sqlQuery,
                    tmpObj.toArray(), Mappers.pageFullMapper);
            daoResponse.status = HttpStatus.OK;
        }
        catch (DataAccessException e) {
            e.printStackTrace();
            daoResponse.body = null;
            daoResponse.status = HttpStatus.NOT_FOUND;
        }
        return daoResponse;
    }

    ///////////////////////////////
    public void dropTables() {
        template.update(
                "TRUNCATE page, userpages CASCADE;" //TODO only users when connected
        );
    }
    ///////////////////////////////

}
