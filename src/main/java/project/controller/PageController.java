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
import java.util.UUID;

@ResponseBody
@RestController
@CrossOrigin({"http://127.0.0.1:8000", "*"})
@RequestMapping("/qr")
public class PageController {

    public static final String APPLICATION_JSON = "application/json";
    private PageDAO pageDAO;
    private UserDAO userDAO;
    private static final String SESSION_KEY = "SessionKey";

    public PageController(PageDAO pageDAO, UserDAO userDAO) {
        this.pageDAO = pageDAO;
        this.userDAO = userDAO;
    }

    @RequestMapping(path = "/create", method = RequestMethod.POST, produces = APPLICATION_JSON)
    public ResponseEntity<?> createPage(@RequestBody Page body, HttpSession httpSession) {
//        User user = (User) httpSession.getAttribute(SESSION_KEY);
//        body.setOwnerID(user.getId());
        System.out.println("1");
        UUID uuid = UUID.randomUUID();
        body.setUUID(uuid.toString());
        body.setOwnerID(1);
        ResponseEntity response;
        DAOResponse<Page> daoResponse = pageDAO.createPage(body);
        if (daoResponse.status == HttpStatus.CREATED) {
            response = ResponseEntity.status(HttpStatus.CREATED).body(body.getUUID());
        } else {
            response = ResponseEntity.status(HttpStatus.FORBIDDEN).body("Something went wrong");
        }
        return response;
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getPage(@PathVariable("id") String pageUUID, HttpSession httpSession) {
//        User user = (User) httpSession.getAttribute(SESSION_KEY);
        ResponseEntity response;
        DAOResponse<Page> daoResponse = pageDAO.getPageByID(pageUUID);
        Page requestedPage = daoResponse.body;
//        if (requestedPage != null) {
//            if (requestedPage.isPublic() || requestedPage.getOwnerID() == user.getId()) {
//                if (requestedPage.getOwnerID() != user.getId()) {
//                    userDAO.addViewedPage(user.getId(),pageID,requestedPage.getTitle());
//                }
//                response =  ResponseEntity.status(HttpStatus.OK).body(requestedPage);
//            } else {
//                response =  ResponseEntity.status(HttpStatus.FORBIDDEN).body("Requested page is private");
//            }
//        } else {
//            response = ResponseEntity.status(HttpStatus.NOT_FOUND).body("NOT_FOUND");
//
//        }
        if (requestedPage != null) {
            if (requestedPage.isPublic()) {
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
//        User user = (User) httpSession.getAttribute(SESSION_KEY);
        ResponseEntity response;
        DAOResponse<Page> daoResponse = pageDAO.getPageByID(pageUUID);
        Page requestedPage = daoResponse.body;
//        if (requestedPage != null && requestedPage.getOwnerID() == user.getId()) {
        if (requestedPage != null ) {
            daoResponse = pageDAO.editPage(body, pageUUID);
            if (daoResponse.status == HttpStatus.OK) {
                response =  ResponseEntity.status(HttpStatus.OK).body("Successfully edited");
            } else {
                response = ResponseEntity.status(HttpStatus.FORBIDDEN).body("Something went wrong");
            }
        } else {
            response =  ResponseEntity.status(HttpStatus.NOT_FOUND).body("NOT_FOUND");

        }
        return response;
    }

    @RequestMapping(path = "/{id}/delete", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> deleteуPage(@PathVariable("id") String pageUUID, HttpSession httpSession) {
//        User user = (User) httpSession.getAttribute(SESSION_KEY);
        ResponseEntity response;
        DAOResponse<Page> daoResponse = pageDAO.getPageByID(pageUUID);
        Page requestedPage = daoResponse.body;
//        if (requestedPage != null && requestedPage.getOwnerID() == user.getId()) {
        if (requestedPage != null) {
            daoResponse = pageDAO.deletePage(pageUUID);
            if (daoResponse.status == HttpStatus.OK) {
                response = ResponseEntity.status(HttpStatus.OK).body("Successfully edited");

            } else {
                response = ResponseEntity.status(HttpStatus.NOT_FOUND).body("Page not found");
            }
        } else {
            response = ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        return response;
    }
}
