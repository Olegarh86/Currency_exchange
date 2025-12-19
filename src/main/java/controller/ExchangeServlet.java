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
import java.math.RoundingMode;
import java.util.Optional;

@WebServlet("/api/exchange")
public class ExchangeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ExchangeRateDao instance = ExchangeRateDao.getInstance();
        CurrencyDao instanceCurrency = CurrencyDao.getInstance();
        BigDecimal convertedAmount;
        BigDecimal rate = null;
        PrintWriter out = response.getWriter();
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/json;charset=UTF-8");
        String from = request.getParameter("from");
        String to = request.getParameter("to");
        int fromInt = instanceCurrency.findIdByCode(from);
        Currency baseCurrency = instanceCurrency.findById(fromInt);
        int toInt = instanceCurrency.findIdByCode(to);
        Currency targetCurrency = instanceCurrency.findById(toInt);
        BigDecimal amount = new BigDecimal(request.getParameter("amount"));

        if (from == null || to == null || (amount.compareTo(BigDecimal.ZERO) <= 0)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        FindExchangeRateByIdDto exchangeRateDto = new FindExchangeRateByIdDto(from, to);
        ExchangeRate exchangeRate = null;
        try {
            exchangeRate = instance.findRate(exchangeRateDto);
            rate = exchangeRate.getRate();
        } catch (DaoException e) {
            try {
                exchangeRateDto = new FindExchangeRateByIdDto(to, from);
                exchangeRate = instance.findRate(exchangeRateDto);
                rate = BigDecimal.ONE.divide(exchangeRate.getRate(), 6, RoundingMode.HALF_UP);
            } catch (DaoException ex) {
                try {
                    exchangeRateDto = new FindExchangeRateByIdDto("USD", from);
                    ExchangeRate exchangeRateFrom = instance.findRate(exchangeRateDto);
                    exchangeRateDto = new FindExchangeRateByIdDto("USD", to);
                    ExchangeRate exchangeRateTo = instance.findRate(exchangeRateDto);
                    rate = exchangeRateTo.getRate().divide(exchangeRateFrom.getRate(), 6, RoundingMode.HALF_EVEN);
                } catch (Exception exc) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            }
        }

        convertedAmount = amount.multiply(rate).setScale(2, RoundingMode.HALF_EVEN);

        out.println("baseCurrency : \n" + baseCurrency);
        out.println("targetCurrency : \n" + targetCurrency);
        out.println("rate : \n" + rate);
        out.println("amount : " + amount);
        out.println("convertedAmount : " + convertedAmount);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}