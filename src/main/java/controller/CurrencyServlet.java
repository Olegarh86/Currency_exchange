package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import dao.CurrencyDao;
import exception.DaoException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Currency;

@WebServlet("/api/currency/*")
public class CurrencyServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String path = req.getRequestURI();
        path = path.substring(path.lastIndexOf('/') + 1).toUpperCase();
        if (path.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        CurrencyDao instance = CurrencyDao.getInstance();
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/json");

        try {
            int currencyId = instance.findIdByCode(path);
            Optional<Currency> currency = instance.findById(currencyId);
            out.print(currency);
        } catch (DaoException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
