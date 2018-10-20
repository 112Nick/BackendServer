package project.controller;


import com.google.gson.Gson;
import project.dao.PageDAO;
import project.dao.UserDAO;
import project.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
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
        System.out.println("pages");

        User user = (User) httpSession.getAttribute(SESSION_KEY);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message("UserYa isn't authorized"));
        }
        DAOResponse<List<Page>> daoResponse = pageDAO.getUsersPages(user.getId().intValue(), sort, own, search);
        if (daoResponse.status == HttpStatus.NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("No pages found, check filters or create new one"));
        }
        return ResponseEntity.status(HttpStatus.OK).body(daoResponse.body);
    }

    @RequestMapping(path = "/getuser", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getUser(HttpSession httpSession) {
        final User user = (User) httpSession.getAttribute(SESSION_KEY);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message("UserYa isn't authorized"));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new Message(user.getDefault_email()));
    }

    @RequestMapping(path = "/login/yandex", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> loginUserYandex(@RequestBody Token token, HttpSession httpSession) {
        System.out.println("login");
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
                UserYa user = g.fromJson(content.toString(), UserYa.class);
                System.out.println(user.getDefault_email());
                user.setToken(token.getToken());
                DAOResponse<User> daoResponse = userDAO.createUser(user);
                if (daoResponse.status == HttpStatus.CONFLICT) {
                    DAOResponse<Integer> daoResponse1 = userDAO.getUserID(user.getDefault_email());
                    user.setId(BigDecimal.valueOf(daoResponse1.body));
                    httpSession.setAttribute(SESSION_KEY, user);
                    return ResponseEntity.status(HttpStatus.OK).body(new Message("UserYa isn't new"));
                }
                user.setId(daoResponse.body.getId());
                httpSession.setAttribute(SESSION_KEY, user);
                return ResponseEntity.status(HttpStatus.CREATED).body(new Message("Successfully registered"));
            }
            con.disconnect();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message("Try another service to login 1"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message("Try another service to login 2"));
        }
    }


    @RequestMapping(path = "/login/google", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> loginUserGoogle(@RequestBody Token token, HttpSession httpSession) {
        System.out.println("login");
        System.out.println(token.getToken());
        try{
            URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=" + token.getToken());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            System.out.println(url);
            con.setRequestMethod("GET");
//            con.setRequestProperty("Content-Type", "application/json");
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
                UserGoogle user = g.fromJson(content.toString(), UserGoogle.class);
                System.out.println(user.getEmail());
                user.setToken(token.getToken());
                DAOResponse<User> daoResponse = userDAO.createUser(user);
                if (daoResponse.status == HttpStatus.CONFLICT) {
                    DAOResponse<Integer> daoResponse1 = userDAO.getUserID(user.getEmail());
                    user.setId(BigDecimal.valueOf(daoResponse1.body));
                    httpSession.setAttribute(SESSION_KEY, user);
                    return ResponseEntity.status(HttpStatus.OK).body(new Message("Successfully authorized"));
                }
                user.setId(daoResponse.body.getId());
                httpSession.setAttribute(SESSION_KEY, user);
                return ResponseEntity.status(HttpStatus.CREATED).body(new Message("Successfully registered"));
            }
            con.disconnect();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message("Try another service to login 1"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message("Try another service to login 2"));
        }
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> logoutUser(HttpSession httpSession) {
        if (httpSession.getAttribute(SESSION_KEY) != null) {
            httpSession.invalidate();
            return ResponseEntity.status(HttpStatus.OK).body(new Message("Successful logout"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message("Unsuccessful logout"));
    }

    ///////////////////////////////////////////
    @RequestMapping(path = "/dropdb", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> clearTables(HttpSession httpSession) {
        ResponseEntity response;
        pageDAO.dropTables();
        response = ResponseEntity.status(HttpStatus.OK).body("Successful droped");
        return response;

    }
    //////////////////////////////////////////
}
