/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wolpray.backend.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cat.proven.wolprayproject.models.pojo.Club;
import org.cat.proven.wolprayproject.models.pojo.Promotion;
import org.cat.proven.wolprayproject.models.pojo.User;
import org.cat.proven.wolprayproject.models.persist.ClubDao;
import org.cat.proven.wolprayproject.models.persist.DBConnect;
import org.cat.proven.wolprayproject.models.persist.PromotionDao;
import org.cat.proven.wolprayproject.models.persist.ReservationDao;
import org.cat.proven.wolprayproject.models.persist.UserDao;
import org.cat.proven.wolprayproject.models.pojo.Reservation;

/**
 *
 * @author Lewis
 */
@WebServlet(name = "UserServlet", urlPatterns = {"/userservlet"})
public class UserServlet extends HttpServlet {

    private ClubDao clubDao;
    private PromotionDao promotionDao;
    private UserDao userDao;
    private ReservationDao reservationDao;

    @Override
    public void init() throws ServletException {
        
         try {
            DBConnect.loadDriver();
            clubDao = new ClubDao();
            promotionDao = new PromotionDao();
            userDao = new UserDao();
            reservationDao = new ReservationDao();
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
        try {
            String action = request.getParameter("action");
            if (action != null) {
                switch (action) {
                    case "list_all_clubs":
                        listAllClubs(response);
                        break;
                    case "list_club_by_name":
                        listByName(request, response);
                        break;
                    case "list_club_by_city":
                        listByCity(request, response);
                        break;
                    case "list_club_by_ambience":
                        listByAmbience(request, response);
                        break;
                    case "list_club_by_dress":
                        listByDressCode(request, response);
                        break;
                    case "list_club_by_cp":
                        listByCp(request, response);
                        break;
                    case "list_all_promotions":
                        listAllPromotions(request, response);
                        break;
                    case "list_promotion_by_club":
                        listPromotionsByClub(request, response);
                        break;
                    case "find_email":
                        findByEmail(request, response);
                        break;
                }
            } else {
                PrintWriter out = response.getWriter();
                RequestResult result = new RequestResult("Esta acción no es válida", -1);
                out.write(new Gson().toJson(result));
            }

        } catch (IOException e) {
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
        try {
            String action = request.getParameter("action");

            switch (action) {
                case "modify":
                    modifyUser(request, response);
                    break;
                case "remove":
                    removeUser(request, response);
                    break;
                default:
                    PrintWriter out = response.getWriter();
                    RequestResult result = new RequestResult("Esta acción no es válida", -1);
                    out.write(new Gson().toJson(result));
                    break;
            }

        } catch (IOException e) {
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

    private void listAllClubs(HttpServletResponse response) {

        List<Club> list = clubDao.findAllClubs();

        if (list != null) {

            writeRequest(response, list, 1);

        } else {
            writeRequest(response, "Ha ocurrido un error al tratar de buscar los clubs", 0);
        }

    }

    private void listByName(HttpServletRequest request, HttpServletResponse response) {

        String name = request.getParameter("name");
        List<Club> result = new ArrayList<>();

        if (name != null) {
            List<Club> list = clubDao.findAllClubs();
            if (list != null) {
                for (Club club : list) {
                    if (club.getName().toLowerCase().contains(name.toLowerCase())) {
                        result.add(club);
                    }
                }
                writeRequest(response, result, 1);

            } else {
                writeRequest(response, "Este club no existe en la base de datos", 0);
            }
        } else {
            writeRequest(response, "Ha ocurrido un error inesperado", -1);
        }
    }

    private void listByCity(HttpServletRequest request, HttpServletResponse response) {

        String city = request.getParameter("city");

        if (city != null) {
            List<Club> list = clubDao.findClubsBycity(city);

            if (list != null) {
                writeRequest(response, list, 1);
            } else {
                writeRequest(response, "No hay clubs en esta ciudad", 0);
            }
        } else {
            writeRequest(response, "Ha ocurrido un error inesperado", -1);
        }
    }

    private void listByDressCode(HttpServletRequest request, HttpServletResponse response) {

        String dressCode = request.getParameter("dress");

        if (dressCode != null) {
            List<Club> list = clubDao.findClubsByDressCode(dressCode);

            if (list != null) {
                writeRequest(response, list, 1);
            } else {
                writeRequest(response, "No existen clubs con este código de vestimenta", 0);
            }
        } else {
            writeRequest(response, "Ha ocurrido un error inesperado", -1);
        }
    }

    private void listByCp(HttpServletRequest request, HttpServletResponse response) {

        String cp = request.getParameter("cp");

        if (cp != null) {
            List<Club> list = clubDao.findClubsByPostalCode(cp);

            if (list != null) {
                writeRequest(response, list, 1);
            } else {
                writeRequest(response, "No existen clubs en este código postal", 0);
            }
        } else {
            writeRequest(response, "Ha ocurrido un error inesperado", -1);
        }
    }

    private void modifyUser(HttpServletRequest request, HttpServletResponse response) {
        RequestResult result;
        String name = request.getParameter("");
        //TODO
    }

    private void removeUser(HttpServletRequest request, HttpServletResponse response) {
        String id = request.getParameter("id");

        if (id != null) {
            try {
                int ids = Integer.parseInt(id);
                User user = userDao.findUserById(ids);
                if (user != null) {
                    boolean isRemoved;
                    List<Reservation> reservationList = reservationDao.findReservationsByUser(user);
                    if (reservationList != null) {
                        isRemoved = reservationDao.removeReservations(reservationList);
                        if (isRemoved) {

                            isRemoved = deleteUser(user);
                            if (isRemoved) {
                                writeRequest(response, "El usuario se ha eliminado correctamente", 1);

                            } else {
                                writeRequest(response, "Ha ocurrido un error inesperado con la base de datos", 0);
                            }
                        }
                    } else {
                        isRemoved = deleteUser(user);

                        if (isRemoved) {
                            writeRequest(response, "Ha ocurrido un error inesperado con la base de datos", 0);
                        } else {
                            writeRequest(response, "El usuario se ha eliminado correctamente", 1);
                        }

                    }
                } else {
                    writeRequest(response, "Este usuario no existe en la base de datos", -1);
                }
            } catch (Exception e) {
            }
        }
    }

    private void listAllPromotions(HttpServletRequest request, HttpServletResponse response) {
        List<Promotion> list = promotionDao.findAllPromotions();

        if (list != null) {
            writeRequest(response, list, 1);
        } else {
            writeRequest(response, "Problemas al listar las promociones", 0);
        }

    }

    private void listPromotionsByClub(HttpServletRequest request, HttpServletResponse response) {

        String id = request.getParameter("id");

        if (id != null) {
            try {
                int ids = Integer.parseInt(id);
                Club club = clubDao.findClubById(ids);
                if (club != null) {
                    List<Promotion> list = promotionDao.findPromotionsByClub(club);
                    if (list != null) {
                        writeRequest(response, list, 1);
                    } else {
                        writeRequest(response, "Este club no tiene promociones activas en este momento", -0);
                    }
                } else {
                    writeRequest(response, "Este club no existe en la base de datos", -1);
                }
            } catch (Exception e) {
                writeRequest(response, "El id solo acepta carácteres numéricos", -2);
            }
        } else {
            writeRequest(response, "Ha ocurrido un error inesperado", -3);
        }
    }

    private void writeRequest(HttpServletResponse response, Object data, int code) {
        try {
            RequestResult result = new RequestResult(data, code);
            response.getWriter().write(new Gson().toJson(result));
        } catch (IOException e) {
        }
    }

    private void listByAmbience(HttpServletRequest request, HttpServletResponse response) {
        String ambience = request.getParameter("amb");

        if (ambience != null) {
            List<Club> list = clubDao.findClubsByAmbience(ambience);

            if (list != null) {
                writeRequest(response, list, 1);
            } else {
                writeRequest(response, "No hay clubs con este ambiente", 0);
            }
        } else {
            writeRequest(response, "Ha ocurrido un error inesperado", -1);
        }
    }

    private void findByEmail(HttpServletRequest request, HttpServletResponse response) {
        String email = request.getParameter("mail");

        if (email != null) {
            User user = userDao.findUserByEmail(email);

            if (user != null) {
                writeRequest(response, user, 1);
            } else {
                writeRequest(response, "Este usuario no existe", 0);
            }
        } else {
            writeRequest(response, "Error en el parámetro", -1);
        }
    }

    private boolean deleteUser(User user) {
        boolean isRemoved = userDao.deleteLogin(user);

        if (isRemoved) {
            int ifDeleted = userDao.removeUser(user);

            if (ifDeleted == 1) {
                isRemoved = true;
            } else {
                isRemoved = false;
            }
        } else {
            isRemoved = false;
        }
        return isRemoved;
    }

}
