package project.controller;


import project.dao.PageDAO;
import project.dao.UserDAO;
import project.model.PageCut;
import project.model.DAOResponse;
import project.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@ResponseBody
@RestController
@RequestMapping("/")
public class UserController {

    private UserDAO userDAO;
    private PageDAO pageDAO;
    private static final String SESSIONKEY = "SessionKey";

    public UserController(UserDAO userDAO, PageDAO pageDAO) {
        this.userDAO = userDAO;
        this.pageDAO = pageDAO;
    }

    @RequestMapping(path = "/", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> baseRequest(HttpSession httpSession) {
        User user = (User) httpSession.getAttribute(SESSIONKEY);
        ResponseEntity response;
        if (user != null) {
            DAOResponse<List<PageCut>> daoResponse = pageDAO.getUsersPages(user.getId());
            if (daoResponse.status == HttpStatus.NOT_FOUND) {
                response = ResponseEntity.status(HttpStatus.NOT_FOUND).body("No pages found");
            } else {
                response =  ResponseEntity.status(HttpStatus.OK).body(daoResponse.body);
            }
        } else {
            response = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User unauthorized");
        }
        return response;
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> createUser(@RequestBody User body, HttpSession httpSession) {
        DAOResponse<User> daoResponse = userDAO.createUser(body);
        ResponseEntity response;
        if (daoResponse.status == HttpStatus.CONFLICT) {
            response =  ResponseEntity.status(HttpStatus.CONFLICT).body("Nickname "+ body.getLogin() + " already exists");
        } else if (!body.getPassword().equals(body.getConfirmPassword())) {
            response =  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password isn't confirmed");
        } else {
            httpSession.setAttribute(SESSIONKEY, body);
            response = ResponseEntity.status(HttpStatus.OK).body("Successfully registered");
        }
        return response;

    }

    @RequestMapping(path = "/login", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> loginUser(@RequestBody User body, HttpSession httpSession) {
        DAOResponse<User> daoResponse = userDAO.getUserByNickname(body.getLogin());
        ResponseEntity response;
        if(daoResponse.status == HttpStatus.NOT_FOUND || !daoResponse.body.getPassword().equals(body.getPassword())) {
            response = ResponseEntity.status(HttpStatus.NOT_FOUND).body("Can't find such user");
        } else {
            httpSession.setAttribute(SESSIONKEY, body);
            response = ResponseEntity.status(HttpStatus.OK).body("Successfully logged in");
        }
        return response;
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> logoutUser(HttpSession httpSession) {
        ResponseEntity response;
        if (httpSession.getAttribute(SESSIONKEY) != null) {
            httpSession.invalidate();
            response = ResponseEntity.status(HttpStatus.OK).body("Successful logout");
        } else {
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unsuccessful logout");
        }
        return response;

    }
}
