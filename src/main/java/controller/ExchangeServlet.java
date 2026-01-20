package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dao.CurrencyDao;
import dao.ExchangeRateDao;
import dto.ExchangeResponseDto;
import exception.BadRequestException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.Exchange;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

import static dao.util.Validator.validateInputParameters;

@Slf4j
public class ExchangeServlet extends HttpServlet {
    private static final String INSTANCE_CURRENCY = "instanceCurrency";
    private static final String INSTANCE_EXCHANGE_RATE = "instanceExchangeRate";
    private static final String BASE_CODE_PARAMETER = "from";
    private static final String TARGET_CODE_PARAMETER = "to";
    private static final String AMOUNT_PARAMETER = "amount";
    private CurrencyDao instanceCurrency;
    private ExchangeRateDao instanceExchangeRate;
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public void init() {
        ServletContext servletContext = getServletContext();
        this.instanceCurrency = (CurrencyDao) servletContext.getAttribute(INSTANCE_CURRENCY);
        this.instanceExchangeRate = (ExchangeRateDao) servletContext.getAttribute(INSTANCE_EXCHANGE_RATE);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String baseCode = request.getParameter(BASE_CODE_PARAMETER);
        String targetCode = request.getParameter(TARGET_CODE_PARAMETER);
        BigDecimal amount = new BigDecimal(request.getParameter(AMOUNT_PARAMETER));
        try {
            validateInputParameters(baseCode, targetCode, amount);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }

        Exchange exchange = new Exchange(instanceCurrency, instanceExchangeRate);
        ExchangeResponseDto result = exchange.convert(baseCode, targetCode, amount);
        log.info("Exchange is done, result is: {}", result);
        PrintWriter out = response.getWriter();
        mapper.writeValue(out, result);
    }
}