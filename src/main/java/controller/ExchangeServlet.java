package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dao.CurrencyDao;
import dao.ExchangeRateDao;
import dto.CurrencyDto;
import dto.CurrencyRequestDto;
import dto.Dto;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.Exchange;
import lombok.extern.slf4j.Slf4j;
import service.Service;

import java.io.IOException;
import java.io.PrintWriter;
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
        String amountString = request.getParameter(AMOUNT_PARAMETER);

        validateInputParameters(baseCode, targetCode, amountString);
        BigDecimal amount = new BigDecimal(amountString);

        Service exchange = new Exchange(instanceCurrency, instanceExchangeRate);
        CurrencyDto currencyDtoBase = new CurrencyRequestDto(baseCode);
        CurrencyDto currencyDtoTarget = new CurrencyRequestDto(targetCode);
        Dto result = exchange.convert(currencyDtoBase, currencyDtoTarget, amount);
        log.info(DONE_RESULT_IS, result);
        PrintWriter out = response.getWriter();
        mapper.writeValue(out, result);
    }
}