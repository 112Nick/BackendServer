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


//    @RequestMapping(path = "/login/yandex/{code}", method = RequestMethod.GET, produces = "application/json")
//    public void loginUser(@PathVariable("code") String code, HttpSession httpSession) {
//        DAOResponse<User> daoResponse = userDAO.getUserByNickname(body.getLogin());
//        ResponseEntity response;
//        if(daoResponse.status == HttpStatus.NOT_FOUND || !daoResponse.body.getPassword().equals(body.getPassword())) {
//            response = ResponseEntity.status(HttpStatus.NOT_FOUND).body("Can't find such user");
//        } else {
//            httpSession.setAttribute(SESSION_KEY, daoResponse.body);
//            response = ResponseEntity.status(HttpStatus.OK).body("Successfully logged in");
//        }
//        return response;
//        try{
//            sendPost(code);
//            URL url = new URL("https://velox-server.herokuapp.com/qr/create");
//            HttpURLConnection con = (HttpURLConnection) url.openConnection();
//            con.setRequestMethod("POST");
//            con.setRequestProperty("Content-Type", "application/json");
//            int status = con.getResponseCode();
//            List<Pair<String,String>> params = new ArrayList<>();
//            params.add(new Pair<>("title", "xxx"));

//            BufferedReader in = new BufferedReader(
//                    new InputStreamReader(con.getInputStream()));
//            String inputLine;
//            StringBuilder content = new StringBuilder();
//            while ((inputLine = in.readLine()) != null) {
//                content.append(inputLine);
//            }
//
//            in.close();
//            con.disconnect();
//            System.out.println(status);
//            System.out.println(content.toString());

//        }catch (Exception e) {
//            e.printStackTrace();
//        }


//    }

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


//    private void sendPost(String code) throws Exception {
//
//
////        String url = "https://example.com";
////        URL obj = new URL(url);
//        URL url = new URL("https://oauth.yandex.ru/token");
//
//        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
//
//        con.setRequestMethod("POST");
////        con.setRequestProperty("User-Agent", USER_AGENT);
//        con.setRequestProperty("Content-Type", "application/json");
//
////        con.setRequestProperty("Accept-Language", "UTF-8");
//
//        con.setDoOutput(true);
//
//        String params = "{\"qwe\":\"xxx\"}";
//        System.out.println(params);
//        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(con.getOutputStream());
//        outputStreamWriter.write(params);
//        outputStreamWriter.flush();
//
//        int responseCode = con.getResponseCode();
//        System.out.println("\nSending 'POST' request to URL : " + url);
////        System.out.println("Post parameters : " + urlParameters);
//        System.out.println("Response Code : " + responseCode);
//
//        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
//        String inputLine;
//        StringBuffer response = new StringBuffer();
//
//        while ((inputLine = in.readLine()) != null) {
//            response.append(inputLine);
//        }
//        in.close();
//
//        System.out.println(response.toString());
//
//    }

}
