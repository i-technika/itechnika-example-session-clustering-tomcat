package com.itechnika.example.tomcat.session.cluster;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/check.do")
public class CheckSessionServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        if (session != null) {
            String sessionId = session.getId();
            String param = (String)session.getAttribute("param");
            out.println(String.format("<p>Session Id: %s<br/>Parameter: %s</p>.", sessionId, param));
        } else {
            out.println("<p>Session does not exist.</p>");
        }
    }
}
