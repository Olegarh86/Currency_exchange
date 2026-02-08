package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.JdbcCurrencyDao;
import dao.JdbcExchangeRateDao;
import dto.ExchangeDto;
import exception.NotFoundException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.Exchange;
import lombok.extern.slf4j.Slf4j;
import service.Service;

import java.io.IOException;
import java.math.BigDecimal;

import static util.Validator.validateInputParameters;

@Slf4j
public class ExchangeServlet extends HttpServlet {
    private static final String INSTANCE_CURRENCY = "instanceCurrency";
    private static final String INSTANCE_EXCHANGE_RATE = "instanceExchangeRate";
    private static final String BASE_CODE_PARAMETER = "from";
    private static final String TARGET_CODE_PARAMETER = "to";
    private static final String AMOUNT_PARAMETER = "amount";
    private static final String DONE_RESULT_IS = "Exchange is done, result is: {}";
    private static final String RATE_NOT_FOUND = "Exchange rate not found. Add exchange rate and try again.";
    private JdbcCurrencyDao instanceCurrency;
    private JdbcExchangeRateDao instanceExchangeRate;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init() {
        ServletContext servletContext = getServletContext();
        this.instanceCurrency = (JdbcCurrencyDao) servletContext.getAttribute(INSTANCE_CURRENCY);
        this.instanceExchangeRate = (JdbcExchangeRateDao) servletContext.getAttribute(INSTANCE_EXCHANGE_RATE);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String baseCode = request.getParameter(BASE_CODE_PARAMETER);
        String targetCode = request.getParameter(TARGET_CODE_PARAMETER);
        String amountString = request.getParameter(AMOUNT_PARAMETER);

        validateInputParameters(baseCode, targetCode, amountString);
        BigDecimal amount = new BigDecimal(amountString);

        Service exchange = new Exchange(instanceCurrency, instanceExchangeRate);
        ExchangeDto result = exchange.convert(baseCode, targetCode, amount).
                orElseThrow(() -> new NotFoundException(RATE_NOT_FOUND));
        log.info(DONE_RESULT_IS, result);
        mapper.writeValue(response.getWriter(), result);
    }
}