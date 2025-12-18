package controller;

import dao.CurrencyDao;
import exception.DaoException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Currency;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/currencies")
public class CurrenciesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Currency> currencies = CurrencyDao.getInstance().findAll();
        PrintWriter out = response.getWriter();
        response.setContentType("text/json");
        for (Currency currency : currencies) {
            out.println(currency);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        CurrencyDao currency = CurrencyDao.getInstance();
        PrintWriter out = response.getWriter();
        String code = request.getParameter("code").toUpperCase();
        String name = request.getParameter("name");
        String sign = request.getParameter("sign");
        if (code.isEmpty() || name.isEmpty() || sign.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            Currency saved = currency.save(new Currency(code, name, sign));
            response.setContentType("text/json");
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.println(saved);
        } catch (DaoException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
    }
}
