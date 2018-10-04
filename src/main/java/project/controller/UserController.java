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
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;

@ResponseBody
@RestController
@CrossOrigin({"http://127.0.0.1:8000", "*"})
@RequestMapping("/")
public class UserController {

    private UserDAO userDAO;
    private PageDAO pageDAO;
    private static final String SESSION_KEY = "SessionKey";

    public UserController(UserDAO userDAO, PageDAO pageDAO) {
        this.userDAO = userDAO;
        this.pageDAO = pageDAO;
    }

    @RequestMapping(path = "/", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> baseRequest(HttpSession httpSession,
                                         @RequestParam(value = "sort", required = false) String sort,
                                         @RequestParam(value = "own", required = false) String own,
                                         @RequestParam(value = "search", required = false) String search) {
//        User user = (User) httpSession.getAttribute(SESSION_KEY);
        ResponseEntity response;
        System.out.println(LocalDateTime.now().toString());
//        if (user != null) {
            DAOResponse<List<PageCut>> daoResponse = pageDAO.getUsersPages(1, sort, own, search);
            if (daoResponse.status == HttpStatus.NOT_FOUND) {
                response = ResponseEntity.status(HttpStatus.NOT_FOUND).body("No pages found");
            } else {
                response =  ResponseEntity.status(HttpStatus.OK).body(daoResponse.body);
            }
//        } else {
//            response = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User unauthorized");
//        }
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
            body.setId(daoResponse.body.getId());
            httpSession.setAttribute(SESSION_KEY, body);
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
            httpSession.setAttribute(SESSION_KEY, daoResponse.body);
            response = ResponseEntity.status(HttpStatus.OK).body("Successfully logged in");
        }
        return response;
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> logoutUser(HttpSession httpSession) {
        ResponseEntity response;
        if (httpSession.getAttribute(SESSION_KEY) != null) {
            httpSession.invalidate();
            response = ResponseEntity.status(HttpStatus.OK).body("Successful logout");
        } else {
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unsuccessful logout");
        }
        return response;

    }

    @RequestMapping(path = "/dropdb", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> clearTables(HttpSession httpSession) {
        ResponseEntity response;
        pageDAO.dropTables();

//        if (httpSession.getAttribute(SESSION_KEY) != null) {
//            httpSession.invalidate();
            response = ResponseEntity.status(HttpStatus.OK).body("Successful droped");
//        } else {
//            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unsuccessful logout");
//        }
        return response;

    }
}
