package project.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import project.model.Page;
import project.model.PageCut;
import project.model.DAOResponse;

import java.sql.Array;
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
                        "INSERT INTO page(uuid, ownerid, title, ispublic, fieldsnames, fieldsvalues, date)" + " VALUES(?, ?, ?, ?, ?, ?, ?)" ,
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setString(1, body.getUUID());
                statement.setInt(2, body.getOwnerID());
                statement.setString(3, body.getTitle());
                statement.setBoolean(4, body.isPublic());
                statement.setArray(5, con.createArrayOf("TEXT", body.getFieldsNames()));
                statement.setArray(6, con.createArrayOf("TEXT", body.getFieldsValues()));
                statement.setString(7, body.getDate());
                return statement;
            }, keyHolder);
            result.status = HttpStatus.CREATED;
            return result;
        }
        catch (DuplicateKeyException e) {
            System.out.println("3");

            result.status = HttpStatus.CONFLICT;
        }
        return result;

    }

    public DAOResponse<Page> getPageByID(String pageUUID) {
        DAOResponse<Page> result = new DAOResponse<>();
        try {
            final Page foundPage =  template.queryForObject(
                    "SELECT * FROM page WHERE uuid = ?",
                    new Object[]{pageUUID},  pageMapper);

            result.body = foundPage;
            result.status = HttpStatus.OK;
        }
        catch (DataAccessException e) {
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
                                " fieldsnames = ?, " +
                                " fieldsvalues = ? " +
                                "WHERE uuid = ?",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setString(1 , body.getTitle());
                statement.setBoolean(2, body.isPublic());
                statement.setArray(3, con.createArrayOf("TEXT", body.getFieldsNames()));
                statement.setArray(4, con.createArrayOf("TEXT", body.getFieldsValues()));
                statement.setString(5, pageUUID);
                return statement;
            }, keyHolder);
            result.status = HttpStatus.OK;
        } catch(DuplicateKeyException e){
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
            result.status = HttpStatus.NOT_FOUND;
        }
        return result;

    }


    public DAOResponse<List<PageCut>> getUsersPages(Integer userID, String sort, String own, String search) {
        DAOResponse<List<PageCut>> daoResponse = new DAOResponse<>();
        List<Object> tmpObj = new ArrayList<>();
        tmpObj.add(userID);
        String sqlQuery;

        if (sort == null || sort.equals("")) {
            sort = "alphabet";
        }
        if (own == null || own.equals("")) {
            own = "all";
        }

        switch(own) {
            case "me":
                sqlQuery = "SELECT uuid, title, date FROM page WHERE ownerID = ? ";
                break;
            case "others":
                sqlQuery = "SELECT pageUUID AS uuid, title, date FROM userPages WHERE userID = ? ";

                break;
            case "all":
                tmpObj.add(userID);
                sqlQuery = "SELECT uuid, title, date FROM page WHERE ownerID = ?" +
                        "UNION SELECT pageUUID as id, title, date from userPages WHERE userID = ? ";
                break;
            default:
                tmpObj.add(userID);
                sqlQuery = "SELECT uuid, title,  date FROM page WHERE ownerID = ? " +
                        "UNION SELECT pageUUID as id, title, date FROM userPages WHERE userID = ? ";
                break;
        }

        if (search != null && !search.isEmpty()) {
            sqlQuery += "WHERE title LIKE '%?%'";
            tmpObj.add(search);
        }


        switch (sort) {
            case "alphabet":
                sqlQuery += "ORDER BY title";
                break;
            case "date":
                //TODO
                sqlQuery += "ORDER BY date";
                break;
        }

        try {
            final List<PageCut> foundPages =  template.query( sqlQuery,
                    tmpObj.toArray(), pageCutMapper);
            daoResponse.body = foundPages;
            daoResponse.status = HttpStatus.OK;
        }
        catch (DataAccessException e) {
            e.printStackTrace();
            daoResponse.body = null;
            daoResponse.status = HttpStatus.NOT_FOUND;
        }
        return daoResponse;
    }

    public void dropTables() {
        template.update(
                "TRUNCATE page, userpages CASCADE;" //TODO only users when connected
        );
    }


    public static final RowMapper<Page> pageMapper = (res, num) -> {
        String uuid = res.getString("uuid");
        Integer ownerID = res.getInt("ownerid");
        String title = res.getString("title");
        Boolean isPublic = res.getBoolean("ispublic");
        Array fieldsNames = res.getArray("fieldsnames");
        Array fieldsValues = res.getArray("fieldsvalues");
        String date = res.getString("date");

        return new Page(ownerID, title, isPublic, (String[])fieldsNames.getArray(), (String[])fieldsValues.getArray(), date);
    };

    public static final RowMapper<PageCut> pageCutMapper = (res, num) -> {
        String uuid = res.getString("uuid");
        String title = res.getString("title");
        String date = res.getString("date");

        return new PageCut(uuid, title, date);
    };

}
