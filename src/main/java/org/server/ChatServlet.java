package org.server;




import org.message.Message;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.Date;
import javax.json.*;
import javax.json.spi.*;
import javax.servlet.http.HttpSession;
import java.sql.*;


@WebServlet(urlPatterns = {"/ChatServlet"}, asyncSupported = true)
public class ChatServlet extends HttpServlet {

    private Integer id = 0;
    private final List<Message> messages = Collections.synchronizedList(new ArrayList<Message>());
    private List<AsyncContext> contexts = new LinkedList<>();
    private final List<HttpSession> sessions = Collections.synchronizedList(new ArrayList<HttpSession>());
    private Connection con = null;
    private PreparedStatement pst = null;
    private Statement st = null;
    private ResultSet rs = null;

    public void init() throws ServletException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/CHAT_DB", "root", "root");
            st = con.createStatement();
            rs = st.executeQuery("SELECT COUNT(id) FROM messages;");
            if(rs.next()) {
                if(rs.getInt(1) == 0) {
                    return;
                }
            }
            rs = st.executeQuery("SELECT id FROM messages ORDER BY id DESC LIMIT 1;");
            if(rs.next()) {
                this.id = rs.getInt(1) + 1;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }


        super.init();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {


        response.addHeader("Access-Control-Allow_Methods", "*");
        if(!sessions.contains(request.getSession())) {
            sessions.add(request.getSession());
        }


        response.setCharacterEncoding("UTF-8");
        final AsyncContext asyncContext = request.startAsync(request, response);
        asyncContext.setTimeout(Integer.MAX_VALUE - 1);
        contexts.add(asyncContext);

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.addHeader("Access-Control-Allow_Methods", "*");
        List<AsyncContext> asyncContexts = new ArrayList<>(this.contexts);
        this.contexts.clear();
        Message message = new Message();
        message.setType(request.getParameter("type"));
        if(message.getType().equals("add")) {
            message.setDate(new Date(Long.parseLong(request.getParameter("date"))));
            message.setUsername(request.getParameter("username"));
            message.setMessage(request.getParameter("message"));
            message.setId(this.id);
            this.id++;
            try {
                pst = con.prepareStatement("INSERT INTO messages VALUES (?, ?, ?)");
                pst.setInt(1, message.getId());
                pst.setString(2, message.getUsername());
                pst.setString(3, message.getMessage());
                pst.executeUpdate();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }

            messages.add(message);
            JsonProvider jsonProvider = JsonProvider.provider();
            JsonObject respMessage = jsonProvider.createObjectBuilder()
                    .add("id", message.getId())
                    .add("username", message.getUsername())
                    .add("message", message.getMessage())
                    .add("type", message.getType())
                    .add("date", message.getDate().getTime())
                    .build();
            for(AsyncContext asyncContext : asyncContexts) {
                try(PrintWriter writer = asyncContext.getResponse().getWriter()) {
                    writer.print(respMessage);
                    writer.flush();
                    asyncContext.complete();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        if(message.getType().equals("log")) {
            request.getSession().setAttribute("username", request.getParameter("username"));
            List<String> usernames = new ArrayList<>();
            synchronized (sessions) {
                for (HttpSession session : sessions) {
                    usernames.add((String) session.getAttribute("username"));
                }
            }
            JsonProvider jsonProvider = JsonProvider.provider();
            JsonObject respMessage = jsonProvider.createObjectBuilder()
                    .add("usernames", usernames.toString())
                    .add("type", message.getType())
                    .build();
            for(AsyncContext asyncContext : asyncContexts) {
                try(PrintWriter writer = asyncContext.getResponse().getWriter()) {
                    writer.print(respMessage);
                    writer.flush();
                    asyncContext.complete();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.addHeader("Access-Control-Allow_Methods", "*");
        List<AsyncContext> asyncContexts = new ArrayList<>(this.contexts);
        this.contexts.clear();
        System.out.println(123);
        System.out.println(request.getParameter("id"));
        for(AsyncContext asyncContext : asyncContexts) {
            try(PrintWriter writer = asyncContext.getResponse().getWriter()) {
                writer.print(1);
                writer.flush();
                asyncContext.complete();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
       /* int delID = Integer.parseInt(request.getParameter("id"));
        System.out.println(delID);
        response.getWriter().print(delID);*/

    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.addHeader("Access-Control-Allow_Methods", "PUT, DELETE, POST, GET, OPTIONS");
    }

    public void destroy() {
        try {
            if (con != null) {
                con.close();
            }
            if (pst != null) {
                pst.close();
            }
            if (st != null) {
                st.close();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        super.destroy();
    }


}
