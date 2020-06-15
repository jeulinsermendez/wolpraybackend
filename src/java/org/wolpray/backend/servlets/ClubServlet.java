package org.wolpray.backend.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cat.proven.wolprayproject.models.pojo.User;
import org.cat.proven.wolprayproject.models.persist.ClubDao;
import org.cat.proven.wolprayproject.models.persist.DBConnect;
import org.cat.proven.wolprayproject.models.persist.ProductDao;
import org.cat.proven.wolprayproject.models.persist.PromotionDao;
import org.cat.proven.wolprayproject.models.persist.ReservationDao;
import org.cat.proven.wolprayproject.models.persist.UserDao;
import org.cat.proven.wolprayproject.models.pojo.Club;
import org.cat.proven.wolprayproject.models.pojo.Reservation;
import org.cat.proven.wolprayproject.models.pojo.Table;

/**
 *
 * @author Lewis
 */
@WebServlet(name = "ClubServlet", urlPatterns = {"/clubservlet"})
public class ClubServlet extends HttpServlet {

    private ClubDao clubDao;
    private ProductDao productDao;
    private PromotionDao promotionDao;
    private UserDao userDao;
    private HashMap<Integer, Reservation> reservatons;
    private ReservationDao reservationDao;

    @Override
    public void init() throws ServletException {
        try {
            DBConnect.loadDriver();
            clubDao = new ClubDao();
            userDao = new UserDao();
            reservationDao = new ReservationDao();
            productDao = new ProductDao();
            promotionDao = new PromotionDao();
        } catch (ClassNotFoundException ex) {
            System.out.println("Conexión en la base de datos");
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
                    case "list_avai_tab":
                        showTables(request, response);
                        break;
                    case "list_by_name":

                        break;
                    case "list_by_city":

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
            if (action != null) {
                switch (action) {
                    case "add_reserv":
                        addTableToList(request, response);
                        break;
                    case "rem_reserv":
                        removeReservation(request, response);
                        break;
                    case "clear_reserv":
                        clearReservation(request, response);
                        break;
                    case "confirm_reserv":
                        confirmReservations(request, response);
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
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private void addTableToList(HttpServletRequest request, HttpServletResponse response) {
        String userId = request.getParameter("userid");
        String clubId = request.getParameter("clubid");
        String tableJson = request.getParameter("table");

        Gson g = new Gson();
        
        if (tableJson != null && clubId != null && userId != null) {
            try {
                
                RequestResult list = g.fromJson(tableJson, RequestResult.class);
                Table table = g.fromJson(tableJson, Table.class);
                int clubIds = Integer.parseInt(clubId);
                int userIds = Integer.parseInt(userId);
                Reservation reservation = new Reservation(table.getId(), clubIds, userIds, new Date(System.currentTimeMillis()));
                if (addToList(reservation)) {
                    writeRequest(response, "se ha añadido correctamente", 1);
                    System.out.println(reservation.toString());
                } else {
                    writeRequest(response, "No se ha añadido correctamente", 1);
                }

            } catch (Exception e) {
            }
        }

    }

    private boolean addToList(Reservation reservation) {
        boolean isAdded = false;
        if (reservatons == null) {
            reservatons = new HashMap<>();
        }
        if (!reservatons.containsKey(reservation.getTableId())) {
            reservatons.put(reservation.getTableId(), reservation);
            isAdded = true;
        }
        return isAdded;
    }

    private void confirmReservations(HttpServletRequest request, HttpServletResponse response) {

        if (reservatons != null) {
            if (reservatons.size() > 0) {
                List<Reservation> list = new ArrayList();

                Iterator<Entry<Integer, Reservation>> it = reservatons.entrySet().iterator();
                while (it.hasNext()) {
                    list.add(it.next().getValue());
                }
                if (reservationDao.addReservation(list)) {
                    writeRequest(response, "Has reservado " + list.size() + " mesas", 1);
                    reservatons.clear();
                    System.out.println("Has reservado " + list.size() + " mesas");
                } else {
                    writeRequest(response, "No has seleccionado ninguna mesa", 0);
                    System.out.println("No has seleccionado ninguna mesa");
                }
            }

        }
    }

    private void removeReservation(HttpServletRequest request, HttpServletResponse response) {
        String tableId = request.getParameter("tableid");

        if (tableId != null) {
            try {
                if (reservatons != null) {
                    int tableIds = Integer.parseInt(tableId);
                    reservatons.remove(tableIds);
                    writeRequest(response, "Se ha eliminado la reserva correctamente", 1);
                } else {
                    writeRequest(response, "No has seleccionado ninguna reserva", 0);
                }
            } catch (Exception e) {
                writeRequest(response, "", -1);
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

    private void showTables(HttpServletRequest request, HttpServletResponse response) {
        String clubId = request.getParameter("clubid");
        if (clubId != null) {
            try {
                int clubIds = Integer.parseInt(clubId);
                Club club = clubDao.findClubById(clubIds);
                if (club != null) {

                    writeRequest(response, club.getTableList(), 1);
                } else {
                    writeRequest(response, "Este club no existe", 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
                writeRequest(response, "Saltó la excepcion", 0);
            }
        }
    }

    private List<Reservation> getReservationList(Club club, User user, List<Table> tablelist) {
        List<Reservation> result = new ArrayList<>();
        for (Table table : tablelist) {
            Reservation reservation = new Reservation(table.getId(), club.getId(), user.getId(), getCourrentDate());
            result.add(reservation);
           
        }
        return result;
    }

    private Date getCourrentDate() {
        Date date = new Date(Calendar.getInstance().getTime().getTime());
        return date;
    }

    private void clearReservation(HttpServletRequest request, HttpServletResponse response) {
        
    }
}
