package project.controller;


import com.google.gson.Gson;
import project.dao.PageDAO;
import project.dao.UserDAO;
import project.model.PageCut;
import project.model.DAOResponse;
import project.model.Token;
import project.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
        User user = (User) httpSession.getAttribute(SESSION_KEY);
        ResponseEntity response;
        if (user != null) {
            DAOResponse<List<PageCut>> daoResponse = pageDAO.getUsersPages(user.getId(), sort, own, search);
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


    @RequestMapping(path = "/getuser", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getUser(HttpSession httpSession) {
        ResponseEntity response;
        final User user = (User) httpSession.getAttribute(SESSION_KEY);
        if (user != null) {
            response = ResponseEntity.status(HttpStatus.OK).body(user);
        } else {
            response = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UNAUTHORIZED");
        }
        return response;
    }



    @RequestMapping(path = "/login/yandex", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> loginUser(@RequestBody Token token, HttpSession httpSession) {
        ResponseEntity response;
        try{
            URL url = new URL("https://login.yandex.ru/info?format=json");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "OAuth " + token.getToken());
            Integer status = con.getResponseCode();
            if (status == 200) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                Gson g = new Gson();
                System.out.println(content.toString());
                User user = g.fromJson(content.toString(), User.class);
                System.out.println(user.getDefault_email());
                user.setToken(token.getToken());
                DAOResponse<User> daoResponse = userDAO.createUser(user);
                if (daoResponse.status == HttpStatus.CONFLICT) {
                    response =  ResponseEntity.status(HttpStatus.CONFLICT).body("User exists");
                    DAOResponse<Integer> daoResponse1 = userDAO.getUserID(user.getDefault_email());
//                    if (user == null) {
//                        System.out.println("user");
//                    }
//                    if (daoResponse1 == null ) {
//                            System.out.println("dao");
//                    }
//                    if (daoResponse1.body == null) {
//                        System.out.println("body");
//                    }
                    user.setId(daoResponse1.body);
                    httpSession.setAttribute(SESSION_KEY, user);
                } else {
                    user.setId(daoResponse.body.getId());
                    httpSession.setAttribute(SESSION_KEY, user);
                    response = ResponseEntity.status(HttpStatus.OK).body("Successfully registered");
                }

            } else {
                response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Try another service to login 1");
            }

            con.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Try another service to login 2");
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
        response = ResponseEntity.status(HttpStatus.OK).body("Successful droped");
        return response;

    }
}
