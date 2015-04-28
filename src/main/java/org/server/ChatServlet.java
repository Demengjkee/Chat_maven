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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.json.*;
import javax.json.spi.*;
import javax.servlet.http.HttpSession;

@WebServlet(urlPatterns = {"/ChatServlet"}, asyncSupported = true)
public class ChatServlet extends HttpServlet {
    private Integer id = 0;
    private final ArrayList<Message> messages = new ArrayList<>();
    private List<AsyncContext> contexts = new LinkedList<>();
    private final List<HttpSession> sessions = new ArrayList<>();


    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

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

        List<AsyncContext> asyncContexts = new ArrayList<>(this.contexts);
        this.contexts.clear();
        Message message = new Message();
        message.setType(request.getParameter("type"));

        if(message.getType().equals("add")) {
            message.setUsername(request.getParameter("username"));
            message.setMessage(request.getParameter("message"));
            message.setId(this.id);
            this.id++;
            messages.add(message);
            JsonProvider jsonProvider = JsonProvider.provider();
            JsonObject respMessage = jsonProvider.createObjectBuilder()
                    .add("id", message.getId())
                    .add("username", message.getUsername())
                    .add("message", message.getMessage())
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

        if(message.getType().equals("log")) {
            request.getSession().setAttribute("username", request.getParameter("username"));
            List<String> usernames = new ArrayList<>();
            for(HttpSession session : sessions) {
                usernames.add((String)session.getAttribute("username"));
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

    }
}
