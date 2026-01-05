package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

@WebServlet(value = "/currencies", name= "CurrenciesServlet")
public class CurrenciesServlet extends HttpServlet {
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Currency> currencies = CurrencyDao.getInstance().findAll();
        PrintWriter out = response.getWriter();
        mapper.writeValue(out, currencies);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        CurrencyDao currency = CurrencyDao.getInstance();
        PrintWriter out = response.getWriter();
        String name = request.getParameter("name");
        String code = request.getParameter("code").toUpperCase();
        String sign = request.getParameter("sign");
        if (code.isEmpty() || name.isEmpty() || sign.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            Currency saved = currency.save(new Currency(name, code, sign));
            response.setStatus(HttpServletResponse.SC_CREATED);
            mapper.writeValue(out, saved);
        } catch (DaoException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
    }
}
