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
                        "INSERT INTO page(ownerid, title, ispubluc, fieldsnames, fieldsvalues)" + " VALUES(?, ?, ?, ?, ?)" ,
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setInt(1, body.getOwnerID());
                statement.setString(2, body.getTitle());
                statement.setBoolean(3, body.isPublic());
                statement.setArray(4, con.createArrayOf("TEXT", body.getFieldsNames()));
                statement.setArray(5, con.createArrayOf("TEXT", body.getFieldsValues()));
                return statement;
            }, keyHolder);
            result.status = HttpStatus.CREATED;
            return result;
        }
        catch (DuplicateKeyException e) {
            result.status = HttpStatus.CONFLICT;
        }
        return result;

    }

    public DAOResponse<Page> getPageByID(Integer pageID) {
        DAOResponse<Page> result = new DAOResponse<>();
        try {
            final Page foundPage =  template.queryForObject(
                    "SELECT * FROM page WHERE id = ?",
                    new Object[]{pageID},  pageMapper);

            result.body = foundPage;
            result.status = HttpStatus.OK;
        }
        catch (DataAccessException e) {
            result.body = null;
            result.status = HttpStatus.NOT_FOUND;
        }
        return result;

    }


    public DAOResponse<Page> editPage(Page body, Integer pageID) {
        DAOResponse<Page> result = new DAOResponse<>();
        result.body = null;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement statement = con.prepareStatement(
                        "UPDATE page SET " +
                                " title = COALESCE (?, title)," +
                                " ispublic = COALESCE(?, ispublic) " +
                                " fieldsnames = COALESCE(?, fieldsnames) " +
                                " fieldsvalues = COALESCE(?, fieldsvalues) " +
                                "WHERE id = ?",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setString(1 , body.getTitle());
                statement.setBoolean(2, body.isPublic());
                statement.setArray(3, con.createArrayOf("TEXT", body.getFieldsNames()));
                statement.setArray(4, con.createArrayOf("TEXT", body.getFieldsValues()));
                statement.setInt(5, pageID);
                return statement;
            }, keyHolder);
            result.status = HttpStatus.OK;
        } catch(DuplicateKeyException e){
            result.status = HttpStatus.CONFLICT;
        }
        return result;
    }

    public DAOResponse<Page> deletePage(Integer pageID) {
        DAOResponse<Page> result = new DAOResponse<>();
        try {
                template.queryForObject(
                    "DELETE FROM page WHERE id = ?", new Object[]{pageID},  pageMapper);
                result.status = HttpStatus.OK;
        }
        catch (DataAccessException e) {
            result.status = HttpStatus.NOT_FOUND;
        }
        return result;

    }


    public DAOResponse<List<PageCut>> getUsersPages(Integer userID) {
        DAOResponse<List<PageCut>> daoResponse = new DAOResponse<>();
        List<Object> tmpObj = new ArrayList<>();
        tmpObj.add(userID);

        try {
            final List<PageCut> foundPages =  template.query( "SELECT id, title FROM page WHERE ownerid = ?",
                    tmpObj.toArray(), pageСutMapper);
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



    public static final RowMapper<Page> pageMapper = (res, num) -> {
        Integer ownerID = res.getInt("ownerid");
        String title = res.getString("title");
        Boolean isPublic = res.getBoolean("ispublic");
        Array fieldsNames = res.getArray("fieldsnames");
        Array fieldsValues = res.getArray("fieldsvalues");
        return new Page(ownerID, title, isPublic, (String[])fieldsNames.getArray(), (String[])fieldsValues.getArray());
    };

    public static final RowMapper<PageCut> pageСutMapper = (res, num) -> {
        Integer ID = res.getInt("id");
        String title = res.getString("title");
        return new PageCut(ID, title);
    };
}
