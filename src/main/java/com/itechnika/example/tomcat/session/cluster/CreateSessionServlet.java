package com.itechnika.example.tomcat.session.cluster;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/create.do")
public class CreateSessionServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String param = request.getParameter("param");

        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(true);
        session.setAttribute("param", param);

        String sessionId = session.getId();
        out.println(String.format("<p>Session(%s) has been created.</p>", sessionId));
    }
}