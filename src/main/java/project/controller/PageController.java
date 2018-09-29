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

@ResponseBody
@RestController
@RequestMapping("/qr")
public class PageController {

    private PageDAO pageDAO;
    private UserDAO userDAO;
    private static final String SESSIONKEY = "SessionKey";

    public PageController(PageDAO pageDAO, UserDAO userDAO) {

        this.pageDAO = pageDAO;
        this.userDAO = userDAO;
    }

    @RequestMapping(path = "/create", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> createPage(@RequestBody Page body, HttpSession httpSession) {
        User user = (User) httpSession.getAttribute(SESSIONKEY);
        body.setOwnerID(user.getId());
        ResponseEntity response;
        DAOResponse<Page> daoResponse = pageDAO.createPage(body);
        if (daoResponse.status == HttpStatus.CREATED) {
            response = ResponseEntity.status(HttpStatus.CREATED).body("Page created");
        } else {
            response = ResponseEntity.status(HttpStatus.FORBIDDEN).body("Something went wrong");
        }
        return response;
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getPage(@PathVariable("id") Integer pageID, HttpSession httpSession) {
        User user = (User) httpSession.getAttribute(SESSIONKEY);
        ResponseEntity response;
        DAOResponse<Page> daoResponse = pageDAO.getPageByID(pageID);
        Page requestedPage = daoResponse.body;
        if (requestedPage != null) {
            if (requestedPage.isPublic() || requestedPage.getOwnerID() == user.getId()) {
                if (requestedPage.getOwnerID() != user.getId()) {
                    userDAO.addViewedPage(user.getId(),pageID,requestedPage.getTitle());
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
    public ResponseEntity<?> editPagePost(@RequestBody Page body, @PathVariable("id") Integer pageID, HttpSession httpSession) {
        User user = (User) httpSession.getAttribute(SESSIONKEY);
        ResponseEntity response;
        DAOResponse<Page> daoResponse = pageDAO.getPageByID(pageID);
        Page requestedPage = daoResponse.body;
        if (requestedPage != null && requestedPage.getOwnerID() == user.getId()) {
            daoResponse = pageDAO.editPage(body, pageID);
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
    public ResponseEntity<?> deleteуPage(@PathVariable("id") Integer pageID, HttpSession httpSession) {
        User user = (User) httpSession.getAttribute(SESSIONKEY);
        ResponseEntity response;
        DAOResponse<Page> daoResponse = pageDAO.getPageByID(pageID);
        Page requestedPage = daoResponse.body;
        if (requestedPage != null && requestedPage.getOwnerID() == user.getId()) {
            daoResponse = pageDAO.deletePage(pageID);
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
