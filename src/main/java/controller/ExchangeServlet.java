package controller;

import dao.CurrencyDao;
import dao.ExchangeRateDao;
import dto.FindExchangeRateByIdDto;
import exception.DaoException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Currency;
import model.ExchangeRate;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@WebServlet("/api/exchange")
public class ExchangeServlet  extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ExchangeRateDao instance = ExchangeRateDao.getInstance();
        CurrencyDao instanceCurrency = CurrencyDao.getInstance();
        PrintWriter out = response.getWriter();
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/json;charset=UTF-8");
        String from = request.getParameter("from");
        String to = request.getParameter("to");
        BigDecimal amount = new BigDecimal(request.getParameter("amount"));

        if (from != null && to != null && (amount.compareTo(BigDecimal.ZERO) > 0)) {
            FindExchangeRateByIdDto exchangeRateDto = new FindExchangeRateByIdDto(from,to);
            try {
                List<ExchangeRate> exchangeRates = instance.findPair(exchangeRateDto);
                ExchangeRate exchangeRate = exchangeRates.getFirst();
                BigDecimal convertedAmount = amount.multiply(exchangeRate.getRate());

                out.println(exchangeRate);
                out.println("  amount : " + amount);
                out.println("  convertedAmount : " + convertedAmount);

            } catch (DaoException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}