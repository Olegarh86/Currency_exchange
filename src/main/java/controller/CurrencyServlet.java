package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        CurrencyDao instance = CurrencyDao.getInstance();
        String path = request.getRequestURI();
        path = path.substring(path.lastIndexOf('/') + 1).toUpperCase();
        if (path.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }


        try {
            int currencyId = instance.findIdByCode(path);
            Currency currency = instance.findById(currencyId);
            mapper.writeValue(out, currency);
        } catch (DaoException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
