package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dao.CurrencyDao;
import dao.ExchangeRateDao;
import dao.util.DBConnector;
import dto.ExchangeResponseDto;
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
import java.sql.Connection;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {
    private final ObjectMapper mapper = new ObjectMapper();
    private final DBConnector connector = new DBConnector();
    private final Connection connection = connector.getConnection();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        ExchangeRateDao instance = ExchangeRateDao.getInstance();
        CurrencyDao instanceCurrency = CurrencyDao.getInstance();
        BigDecimal convertedAmount;
        BigDecimal rate = null;
        String from = request.getParameter("from");
        String to = request.getParameter("to");
        int fromInt = instanceCurrency.findIdByCode(connection, from);
        Currency baseCurrency = instanceCurrency.findById(connection, fromInt);
        int toInt = instanceCurrency.findIdByCode(connection, to);
        Currency targetCurrency = instanceCurrency.findById(connection, toInt);
        BigDecimal amount = new BigDecimal(request.getParameter("amount"));

        if (from == null || to == null || (amount.compareTo(BigDecimal.ZERO) <= 0)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        FindExchangeRateByIdDto exchangeRateDto = new FindExchangeRateByIdDto(from, to);
        ExchangeRate exchangeRate = null;
        try {
            exchangeRate = instance.findRate(connection, exchangeRateDto);
            rate = exchangeRate.getRate();
        } catch (DaoException e) {
            try {
                exchangeRateDto = new FindExchangeRateByIdDto(to, from);
                exchangeRate = instance.findRate(connection, exchangeRateDto);
                rate = BigDecimal.ONE.divide(exchangeRate.getRate(), 6, RoundingMode.HALF_UP);
            } catch (DaoException ex) {
                try {
                    exchangeRateDto = new FindExchangeRateByIdDto("USD", from);
                    ExchangeRate exchangeRateFrom = instance.findRate(connection, exchangeRateDto);
                    exchangeRateDto = new FindExchangeRateByIdDto("USD", to);
                    ExchangeRate exchangeRateTo = instance.findRate(connection, exchangeRateDto);
                    rate = exchangeRateTo.getRate().divide(exchangeRateFrom.getRate(), 6, RoundingMode.HALF_EVEN);
                } catch (Exception exc) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            }
        }

        convertedAmount = amount.multiply(rate).setScale(2, RoundingMode.HALF_EVEN);
        ExchangeResponseDto exchangeResponseDto = new ExchangeResponseDto(
                baseCurrency, targetCurrency, rate, amount, convertedAmount);
        mapper.writeValue(out, exchangeResponseDto);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}