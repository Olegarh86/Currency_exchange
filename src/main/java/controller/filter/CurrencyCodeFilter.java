package controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;

import java.io.IOException;

@WebFilter(servletNames = {"CurrenciesServlet"})
public class CurrencyCodeFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//        Set<Currency> allCurrencies = Currency.getAvailableCurrencies();
//        Set<String> allCodes = allCurrencies.stream().map(Currency::getCurrencyCode).collect(Collectors.toSet());
//        Set<String> allNames = allCurrencies.stream().map(Currency::getDisplayName).collect(Collectors.toSet());
//        Set<String> allSymbols = allCurrencies.stream().map(Currency::getSymbol).collect(Collectors.toSet());
//
//        String code = servletRequest.getParameter("code").toUpperCase();
//        String name = servletRequest.getParameter("name");
//        String sign = servletRequest.getParameter("sign");
//
//        if (allCodes.contains(code) && allNames.contains(name) && allSymbols.contains(sign)) {
            filterChain.doFilter(servletRequest, servletResponse);
//        }
//        throw new BadRequestException("The required form field is missing or non-existent currency");
    }
}
