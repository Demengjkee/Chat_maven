package org.server;

import com.mysql.jdbc.Connection;
import org.message.Message;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.json.*;
import javax.json.spi.*;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

@WebServlet(urlPatterns = {"/ChatServlet"}, asyncSupported = true)
public class ChatServlet extends HttpServlet {

    private Integer id = 0;
    private final List<Message> messages = Collections.synchronizedList(new ArrayList<Message>());
    private List<AsyncContext> contexts = new LinkedList<>();
    private final List<HttpSession> sessions = Collections.synchronizedList(new ArrayList<HttpSession>());

    public void init() throws ServletException {
        this.readXML();
        for(Message message : messages) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY HH:mm:ss");
            System.out.println(dateFormat.format(message.getDate()) + " " + message.getUsername() + ": "
                + message.getMessage());
        }
        super.init();
    }

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
            message.setDate(new Date(Long.parseLong(request.getParameter("date"))));
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
    public void destroy() {
        this.writeXML();
        super.destroy();
    }

    public void writeXML() {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();
            Element root = doc.createElement("messages");
            doc.appendChild(root);
            synchronized (messages) {
                for (Message message : messages) {
                    Element msg = doc.createElement("msg");
                    root.appendChild(msg);
                    Element id = doc.createElement("id");
                    id.appendChild(doc.createTextNode(Integer.toString(message.getId())));
                    msg.appendChild(id);
                    Element username = doc.createElement("username");
                    username.appendChild(doc.createTextNode(message.getUsername()));
                    msg.appendChild(username);
                    Element mes = doc.createElement("message");
                    mes.appendChild(doc.createTextNode(message.getMessage()));
                    msg.appendChild(mes);
                    Element date = doc.createElement("date");
                    SimpleDateFormat format = new SimpleDateFormat("dd-MM-YYYY HH:mm:ss");
                    date.appendChild(doc.createTextNode(format.format(message.getDate())));
                    msg.appendChild(date);
                }
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            File f = new File("history.xml");
            StreamResult result = new StreamResult(f);
            transformer.transform(source, result);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void readXML() {
        File fXmlFile = new File("history.xml");
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("msg");
            synchronized (messages) {
                for (int i = 0; i < nList.getLength(); i++) {
                    Node nItem = nList.item(i);
                    if (nItem.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) nItem;
                        Message m = new Message();
                        m.setId(Integer.parseInt(element.getElementsByTagName("id").item(0).getTextContent()));
                        m.setMessage(element.getElementsByTagName("message").item(0).getTextContent());
                        m.setUsername(element.getElementsByTagName("username").item(0).getTextContent());
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY HH:mm:ss");
                        m.setDate(dateFormat.parse(element.getElementsByTagName("date").item(0).getTextContent()));
                        messages.add(m);
                    }

                }
                id = messages.size();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
