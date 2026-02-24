package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.JdbcCurrencyDao;
import dao.JdbcExchangeRateDao;
import dto.ExchangeDto;
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
    private static final String BASE_CODE_PARAMETER = "from";
    private static final String TARGET_CODE_PARAMETER = "to";
    private static final String AMOUNT_PARAMETER = "amount";
    private static final String DONE_RESULT_IS = "Exchange is done, result is: {}";
    private final JdbcCurrencyDao instanceCurrency = new JdbcCurrencyDao();
    private final JdbcExchangeRateDao instanceExchangeRate = new JdbcExchangeRateDao();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String baseCode = request.getParameter(BASE_CODE_PARAMETER);
        String targetCode = request.getParameter(TARGET_CODE_PARAMETER);
        String amountString = request.getParameter(AMOUNT_PARAMETER);

        validateInputParameters(baseCode, targetCode, amountString);
        BigDecimal amount = new BigDecimal(amountString);

        Service exchange = new Exchange(instanceCurrency, instanceExchangeRate);
        ExchangeDto result = exchange.convert(baseCode, targetCode, amount);
        log.info(DONE_RESULT_IS, result);
        mapper.writeValue(response.getWriter(), result);
    }
}