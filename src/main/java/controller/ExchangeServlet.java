package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dto.ExchangeResponseDto;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.Exchange;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

import static dao.util.Validator.validateInputParameters;

@WebServlet(value = "/exchange", name = "ExchangeServlet")
public class ExchangeServlet extends HttpServlet {
    private static final String BASE_CODE_PARAMETER = "from";
    private static final String TARGET_CODE_PARAMETER = "to";
    private static final String AMOUNT_PARAMETER = "amount";
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String baseCode = request.getParameter(BASE_CODE_PARAMETER);
        String targetCode = request.getParameter(TARGET_CODE_PARAMETER);
        BigDecimal amount = new BigDecimal(request.getParameter(AMOUNT_PARAMETER));
        validateInputParameters(baseCode, targetCode, amount);

        Exchange exchange = new Exchange();
        ExchangeResponseDto result = exchange.convert(baseCode, targetCode, amount);
        PrintWriter out = response.getWriter();
        mapper.writeValue(out, result);
    }
}