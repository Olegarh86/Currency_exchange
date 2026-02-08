package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.JdbcCurrencyDao;
import dao.JdbcExchangeRateDao;
import dto.*;
import exception.BadRequestException;
import exception.NotFoundException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import mapper.CurrencyMapper;
import model.Currency;
import model.ExchangeRate;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;

import static util.Validator.*;

@Slf4j
public class ExchangeRateServlet extends HttpServlet {
    private static final String INSTANCE_CURRENCY = "instanceCurrency";
    private static final String INSTANCE_EXCHANGE_RATE = "instanceExchangeRate";
    private static final String UPDATED_SUCCESSFULLY = "ExchangeRate updated successfully: {} - {} rate: {}";
    private static final String EXCHANGE_RATE = "Exchange rate: ";
    private static final String SPACE = " ";
    private static final char SEPARATOR = '/';
    private static final int INDEX_START_RATE = 5;
    private static final int CODE_LENGTH = 3;
    private JdbcExchangeRateDao instanceExchangeRate;
    private JdbcCurrencyDao instanceCurrency;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init() {
        ServletContext servletContext = getServletContext();
        this.instanceCurrency = (JdbcCurrencyDao) servletContext.getAttribute(INSTANCE_CURRENCY);
        this.instanceExchangeRate = (JdbcExchangeRateDao) servletContext.getAttribute(INSTANCE_EXCHANGE_RATE);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Codes codes = getCodes(request);
        String baseCode = codes.baseCode();
        String targetCode = codes.targetCode();
        validateCode(baseCode);
        validateCode(targetCode);

        ExchangeRate result = instanceExchangeRate.findByCodes(baseCode, targetCode).
                orElseThrow(() -> new NotFoundException(EXCHANGE_RATE + baseCode + SPACE + targetCode));
        response.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(response.getWriter(), CurrencyMapper.INSTANCE.exchangeRateToDto(result));
    }

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String rate = getStringRate(request);
        Codes codes = getCodes(request);
        String baseCode = codes.baseCode();
        String targetCode = codes.targetCode();

        validateInputParameters(baseCode, targetCode, rate);
        BigDecimal newRate = new BigDecimal(rate);
        ExchangeRate result = update(new ExchangeRateRequestDto(baseCode, targetCode, newRate));

        response.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(response.getWriter(), CurrencyMapper.INSTANCE.exchangeRateToDto(result));
    }

    private static String getStringRate(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder body = new StringBuilder();
        String line;
        if ((line = reader.readLine()) != null) {
            body.append(line);
            return body.substring(INDEX_START_RATE, body.length());
        }
        throw new BadRequestException(EXCHANGE_RATE);
    }

    private static Codes getCodes(HttpServletRequest request) {
        String codes = request.getPathInfo();
        codes = codes.substring(codes.lastIndexOf(SEPARATOR) + 1).toUpperCase();
        validateCodesLength(codes);

        String baseCode = codes.substring(0, CODE_LENGTH).toUpperCase();
        String targetCode = codes.substring(CODE_LENGTH).toUpperCase();
        return new Codes(baseCode, targetCode);
    }

    private ExchangeRate update(ExchangeRateRequestDto exchangeRateRequestDto) {
        Currency baseCurrency = instanceCurrency.findByCode(exchangeRateRequestDto.baseCode()).
                orElseThrow(() -> new NotFoundException(exchangeRateRequestDto.baseCode()));
        Currency targetCurrency = instanceCurrency.findByCode(exchangeRateRequestDto.targetCode()).
                orElseThrow(() -> new NotFoundException(exchangeRateRequestDto.targetCode()));

        instanceExchangeRate.updateRate(CurrencyMapper.INSTANCE.
                currenciesWithRateToExchangeRate(baseCurrency, targetCurrency, exchangeRateRequestDto.rate()));

        log.info(UPDATED_SUCCESSFULLY, exchangeRateRequestDto.baseCode(),
                exchangeRateRequestDto.targetCode(),
                exchangeRateRequestDto.rate());

        return instanceExchangeRate.findByCodes(exchangeRateRequestDto.baseCode(), exchangeRateRequestDto.targetCode()).
                orElseThrow(() -> new NotFoundException(
                        exchangeRateRequestDto.baseCode() + " " +  exchangeRateRequestDto.targetCode()));
    }
}
