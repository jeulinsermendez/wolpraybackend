package org.wolpray.backend.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cat.proven.wolprayproject.models.persist.DBConnect;
import org.cat.proven.wolprayproject.models.pojo.User;
import org.cat.proven.wolprayproject.models.persist.UserDao;

/**
 *
 * @author Lewis
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/loginservlet"})
public class LoginServlet extends HttpServlet {

    private UserDao userDao;

    @Override
    public void init() throws ServletException {
        try {
            DBConnect.loadDriver();
            userDao = new UserDao();
        } catch (ClassNotFoundException ex) {
            System.out.println("Error con la conexión de la base de datos");
        }
        
        
        
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
        String action = request.getParameter("action");

        if (action != null) {
            switch (action) {
                case "login":
                    login(request, response);
                    break;
                case "valemail":
                    validateEmail(request, response);
                    break;
                case "logout":
                    logoutUser(request, response);
                    break;
            }
        }

    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
        String action = request.getParameter("action");

        if (action != null) {
            switch (action) {
                case "registrer":
                    addUser(request, response);
                    break;

            }
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private void login(HttpServletRequest request, HttpServletResponse response) {
        String mail = request.getParameter("mail");
        String password = request.getParameter("pass");

        if (mail != null || password != null) {
            User user = userDao.findUserByEmail(mail);
            if (user != null) {
                if (user.getPassword().equals(password)) {
                    if (userDao.validateSession(user)) {
                        HttpSession session = request.getSession(true);

                        session.setAttribute("user", user);
                        writeRequest(response, user, 1);
                    } else {
                        writeRequest(response, "Oops, parece que quieres entrar dos veces a la plataforma", 0);
                    }
                } else {
                    writeRequest(response, "Contraseña incorrecta", -1);
                }

            } else {
                writeRequest(response, "No nos consta este correo electrónico en la base de datos...", -2);
            }

        } else {
            writeRequest(response, "Ha ocurrido un error inesperado...", -3);
        }
    }

    private void addUser(HttpServletRequest request, HttpServletResponse response) {

        String name = request.getParameter("n");
        String mail = request.getParameter("m");
        String password = request.getParameter("p");
        String streetName = request.getParameter("sna");
        String streetNum = request.getParameter("snu");
        String postalCode = request.getParameter("cp");
        String phone = request.getParameter("ph");
        String city = request.getParameter("ct");
        String birthdate = request.getParameter("brd");

        if (name != null && password != null && streetName != null && streetNum != null
                && postalCode != null && phone != null && city != null && birthdate != null) {
            int stNumbe = Integer.valueOf(streetNum);
            User user = new User(name, mail, password, streetName, stNumbe, postalCode, phone, city, birthdate, "client");
            int isAdded = validateUser(user);
            switch (isAdded) {
                case -3:
                    writeRequest(response, "No puedes registrarte con este nombre de usuario porque ya está siendo usado", -3);
                    break;
                case -2:
                    writeRequest(response, "No puedes registrarte con este correo electrónico porque ya está siendo usado", -2);
                    break;
                case -1:
                    writeRequest(response, "No puedes registrarte con este teléfono porqe ya esta siendo usado", -1);
                    break;
                case 0:
                    writeRequest(response, "Tienes que ser mayor de edad", 0);
                    break;
                case 1:

                    writeRequest(response, "¡Te has registrado con éxito!", 1);

                    break;
                default:
                    writeRequest(response, "Un error insperado ha ocurrido", -3);
                    break;
            }
        } else {
            writeRequest(response, "Un error insperado ha ocurrido", -4);

        }

    }

    /**
     * Validates whether the user can be added to the database.
     *
     * @param user: User to validate.
     * @return -2 if the username already exists in the database, -1 if the
     * email already exists in the database, 0 if the phone already exists in
     * the database, 1 if is added.
     */
    private int validateUser(User user) {
        int result;
        User oldUser = userDao.findUserByUserName(user.getUserName());
        if (oldUser != null) {
            result = -3;
        } else {
            oldUser = userDao.findUserByEmail(user.getMail());
            if (oldUser != null) {
                result = -2;
            } else {
                oldUser = userDao.findUserByPhone(user.getPhone());
                if (oldUser != null) {
                    result = -1;
                } else {
                    int yearsOld = getYearsOld(user.getBirthDate());

                    if (yearsOld >= 18) {
                        result = userDao.addUser(user);
                    } else {
                        result = 0;
                    }
                }
            }
        }
        return result;
    }

    private void validateEmail(HttpServletRequest request, HttpServletResponse response) {
        String mail = request.getParameter("mail");

        if (mail != null) {
            User user = userDao.findUserByEmail(mail);
            if (user == null) {
                writeRequest(response, "", 1);
            } else {
                writeRequest(response, "Este correo electrónico ya existe en la base de datos", 0);
            }
        }
    }

    private void writeRequest(HttpServletResponse response, Object data, int code) {
        try {
            RequestResult result = new RequestResult(data, code);
            response.getWriter().write(new Gson().toJson(result));
        } catch (IOException e) {
        }
    }

    private void logoutUser(HttpServletRequest request, HttpServletResponse response) {

        String id = request.getParameter("id");
        if (id != null) {
            try {
                int ids = Integer.parseInt(id);
                User user = userDao.findUserById(ids);
                if (user != null) {
                    boolean isLogout = userDao.logoutUser(user);
                    if (isLogout) {
                        request.getSession().invalidate();
                        writeRequest(response, "Bye", 1);
                    } else {
                        writeRequest(response, "An error occurred while trying to log out", 0);
                    }

                } else {
                    writeRequest(response, "An error occurred while trying to log out", 0);
                }
            } catch (Exception e) {
            }
        }
    }

    private int getYearsOld(String birthDate) {
        String split[] = birthDate.split("-");
        int yearInt = Integer.parseInt(split[0]);
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int yearsOld = currentYear - yearInt;
        return yearsOld;
    }

}
