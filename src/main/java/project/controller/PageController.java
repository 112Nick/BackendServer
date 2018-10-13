package project.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.dao.PageDAO;
import project.dao.UserDAO;
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

//    public static final String APPLICATION_JSON = "";
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
        ResponseEntity response;
        if (user == null) {
            response = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("123");
        } else {
            body.setOwnerID(user.getId()); //NullPointer
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
                response = ResponseEntity.status(HttpStatus.CREATED).body(body);
            } else {
                response = ResponseEntity.status(HttpStatus.FORBIDDEN).body("Something went wrong");
            }
        }
        return response;
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getPage(@PathVariable("id") String pageUUID, HttpSession httpSession) {
        User user = (User) httpSession.getAttribute(SESSION_KEY);
        if (user == null) {
            user = new User();
        }
        ResponseEntity response;
        DAOResponse<Page> daoResponse = pageDAO.getPageByID(pageUUID);
        Page requestedPage = daoResponse.body;
        if (requestedPage != null) {
            if (requestedPage.isPublic() || requestedPage.getOwnerID() == user.getId()) {
                if (requestedPage.getOwnerID() != user.getId()) {
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    Instant instant = timestamp.toInstant();
                    userDAO.addViewedPage(user.getId(), pageUUID, requestedPage.getTitle(), instant.toString());
                }
                response =  ResponseEntity.status(HttpStatus.OK).body(requestedPage);
            } else {
                response =  ResponseEntity.status(HttpStatus.FORBIDDEN).body("Requested page is private");
            }
        } else {
            response = ResponseEntity.status(HttpStatus.NOT_FOUND).body("NOT_FOUND");

        }
        return response;
    }


    @RequestMapping(path = "/{id}/edit", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> editPagePost(@RequestBody Page body, @PathVariable("id") String pageUUID, HttpSession httpSession) {
        User user = (User) httpSession.getAttribute(SESSION_KEY);
        ResponseEntity response;
        System.out.println("edit");
        DAOResponse<Page> daoResponse = pageDAO.getPageByID(pageUUID);
        Page requestedPage = daoResponse.body;
        if (user == null) {
            response = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("123");
        } else {
            if (requestedPage != null) {
                if (requestedPage.getOwnerID() == user.getId()) {
                    if (body.getTitle().equals("")) {
                        body.setTitle("Unnamed");
                    }
                    daoResponse = pageDAO.editPage(body, pageUUID);
                    if (daoResponse.status == HttpStatus.OK) {
                        response =  ResponseEntity.status(HttpStatus.OK).body("Successfully edited");
                    } else {
                        response = ResponseEntity.status(HttpStatus.CONFLICT).body("Something went wrong");
                    }
                } else {
                    response = ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to edit this page");

                }
            } else {
                response =  ResponseEntity.status(HttpStatus.NOT_FOUND).body("NOT_FOUND");
            }
        }
        return response;
    }

    @RequestMapping(path = "/{id}/delete", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<?> deletePage(@PathVariable("id") String pageUUID, HttpSession httpSession) {
        User user = (User) httpSession.getAttribute(SESSION_KEY);
        ResponseEntity response;
        DAOResponse<Page> daoResponse = pageDAO.getPageByID(pageUUID);
        Page requestedPage = daoResponse.body;
        if (user == null) {
            response = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("123");
        } else {
            if (requestedPage != null) {
                if (requestedPage.getOwnerID() == user.getId()) {
                    daoResponse = pageDAO.deletePage(pageUUID);
                    if (daoResponse.status == HttpStatus.OK) {
                        pageDAO.deletePageFromViewers(pageUUID);
                        response =  ResponseEntity.status(HttpStatus.OK).body("Successfully deleted");
                    } else {
                        response = ResponseEntity.status(HttpStatus.NOT_FOUND).body("Something went wrong");
                    }
                } else {
                    response = ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to edit this page");

                }
            } else {
                response =  ResponseEntity.status(HttpStatus.NOT_FOUND).body("NOT_FOUND");
            }
        }

        return response;
    }
}
