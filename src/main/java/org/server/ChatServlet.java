package org.server;

import org.message.Message;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.json.*;
import javax.json.spi.*;
import javax.servlet.http.HttpSession;

/**
 * Created by demeng on 29.03.15.
 */
@WebServlet("/ChatServlet")
public class ChatServlet extends HttpServlet {
    private Integer id = 0;
    private final ArrayList<Message> messages = new ArrayList<>();
    private final ArrayList<HttpSession> sessions = new ArrayList<>();



    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (!sessions.contains(request.getSession())) {
            sessions.add(request.getSession());
        }

        System.out.println("it works GET");
        String reqLine = request.getQueryString();
        JsonProvider jsonProvider = JsonProvider.provider();
        PrintWriter writer = response.getWriter();
        StringTokenizer st = new StringTokenizer(reqLine, "=&");
        st.nextToken();
        String type = st.nextToken();
        if (type.equals("connect")) {
            ArrayList<JsonObject> respArray = new ArrayList<>();
            JsonArray jsonValues = jsonProvider.createArrayBuilder().build();
            if (messages.size() > 15) {
                for (int i = messages.size() - 15; i > messages.size(); i++) {
                    JsonObject respMessage = jsonProvider.createObjectBuilder()
                            .add("id", messages.get(i).getId())
                            .add("username", messages.get(i).getUsername())
                            .add("message", messages.get(i).getMessage())
                            .build();
                    jsonValues.add(respMessage);
                    //respArray.add(respMessage);
                }

                writer.print(jsonValues);
            } else {
                for (Message msg : messages) {
                    JsonObject respMessage = jsonProvider.createObjectBuilder()
                            .add("id", msg.getId())
                            .add("username", msg.getUsername())
                            .add("message", msg.getMessage())
                            .build();
                    writer.print(respMessage);
                }
                writer.flush();
            }
        }

        if(type.equals("checkServer")) {
            writer.print(true);
        }


    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("it works POST");

        String readerTmp;
        Message message = new Message();
        BufferedReader reader = request.getReader();
        readerTmp = reader.readLine();
        StringTokenizer st = new StringTokenizer(readerTmp, "=&");
        st.nextToken();
        message.setType(st.nextToken());
        if(message.getType().equals("add")) {
            st.nextToken();
            message.setMessage(st.nextToken());
            st.nextToken();
            message.setUsername(st.nextToken());

            message.setId(id);
            messages.add(message);
            id++;
            JsonProvider jsonProvider = JsonProvider.provider();
            PrintWriter writer = response.getWriter();
            JsonObject respMessage = jsonProvider.createObjectBuilder()
                    .add("id", message.getId())
                    .add("username", message.getUsername())
                    .add("message", message.getMessage())
                    .build();
            writer.print(respMessage);
            //TODO: make all sessions make GET queries
            for(HttpSession session : sessions) {

            }
        }

        if(message.getType().equals("log")) {
            st.nextToken();
            request.getSession().setAttribute("username", st.nextToken());
            PrintWriter writer = response.getWriter();
            writer.print(true);
        }
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }
}
