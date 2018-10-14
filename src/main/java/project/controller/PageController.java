package project.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.dao.PageDAO;
import project.dao.UserDAO;
import project.model.Message;
import project.model.Page;
import project.model.DAOResponse;
import project.model.User;

import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@ResponseBody
@RestController
@CrossOrigin({"http://127.0.0.1:8000", "*"})
@RequestMapping("/qr")
public class PageController {

    private PageDAO pageDAO;
    private UserDAO userDAO;
    private static final String SESSION_KEY = "SessionKey";

    public PageController(PageDAO pageDAO, UserDAO userDAO) {
        this.pageDAO = pageDAO;
        this.userDAO = userDAO;
    }

    @RequestMapping(path = "/create", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> createPage(@RequestBody Page body, HttpSession httpSession) {
        User user = (User) httpSession.getAttribute(SESSION_KEY);
        if (user == null) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message("User isn't authorized"));
        }
        body.setOwnerID(user.getId());
        UUID uuid = UUID.randomUUID();
        body.setUuid(uuid.toString());
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Instant instant = timestamp.toInstant();
        body.setDate(instant.toString());
        body.setOwnerID(user.getId());
        if (body.getTitle().equals("")) {
            body.setTitle("Unnamed");
        }
        DAOResponse<Page> daoResponse = pageDAO.createPage(body);
        if (daoResponse.status == HttpStatus.CREATED) {
            return  ResponseEntity.status(HttpStatus.CREATED).body(body);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message("Something went wrong"));
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getPage(@PathVariable("id") String pageUUID, HttpSession httpSession) {
        User user = (User) httpSession.getAttribute(SESSION_KEY);
        if (user == null) {
            user = new User();
        }
        DAOResponse<Page> daoResponse = pageDAO.getPageByID(pageUUID);
        Page requestedPage = daoResponse.body;
        if (requestedPage != null) {
            if (requestedPage.isPublic() || requestedPage.getOwnerID() == user.getId()) {
                if (requestedPage.getOwnerID() != user.getId()) {
                    pageDAO.addViewedPage(user.getId(), pageUUID);
                }
                requestedPage.setOwnerID(0);
                return ResponseEntity.status(HttpStatus.OK).body(requestedPage);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message("Requested page is private"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Requested page isn't found"));
    }


    @RequestMapping(path = "/{id}/edit", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> editPagePost(@RequestBody Page body, @PathVariable("id") String pageUUID, HttpSession httpSession) {
        User user = (User) httpSession.getAttribute(SESSION_KEY);
        DAOResponse<Page> daoResponse = pageDAO.getPageByID(pageUUID);
        Page requestedPage = daoResponse.body;
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message("User isn't authorized"));
        }
        if (requestedPage != null) {
            if (requestedPage.getOwnerID() == user.getId()) {
                if (body.getTitle().equals("")) {
                    body.setTitle("Unnamed");
                }
                daoResponse = pageDAO.editPage(body, pageUUID);
                if (daoResponse.status == HttpStatus.OK) {
                    return ResponseEntity.status(HttpStatus.OK).body(new Message("Successfully edited"));
                }
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message("Something went wrong"));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message("You are not allowed to edit this page"));
        }
        return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Requested page isn't found"));
    }

    @RequestMapping(path = "/{id}/delete", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<?> deletePage(@PathVariable("id") String pageUUID, HttpSession httpSession) {
        User user = (User) httpSession.getAttribute(SESSION_KEY);
        DAOResponse<Page> daoResponse = pageDAO.getPageByID(pageUUID);
        Page requestedPage = daoResponse.body;
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message("User isn't authorized"));
        }
        if (requestedPage != null) {
            if (requestedPage.getOwnerID() == user.getId()) {
                daoResponse = pageDAO.deletePage(pageUUID);
                if (daoResponse.status == HttpStatus.OK) {
                    pageDAO.deletePageFromViewers(pageUUID);
                    return ResponseEntity.status(HttpStatus.OK).body(new Message("Successfully deleted"));
                }
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Something went wrong"));
            }
            daoResponse = pageDAO.deletePageFromViewers(pageUUID);
            if (daoResponse.status == HttpStatus.OK) {
                return ResponseEntity.status(HttpStatus.OK).body(new Message("Successfully deleted"));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message("You are not allowed to edit this page"));
        }
        return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Requested page isn't found"));
    }
}
