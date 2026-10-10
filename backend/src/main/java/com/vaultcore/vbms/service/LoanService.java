package com.vaultcore.vbms.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vaultcore.vbms.dto.LoanDtos.LoanApplicationRequest;
import com.vaultcore.vbms.dto.LoanDtos.LoanResponse;
import com.vaultcore.vbms.entity.Customer;
import com.vaultcore.vbms.entity.Loan;
import com.vaultcore.vbms.exception.ApiExceptions.BadRequest;
import com.vaultcore.vbms.repository.CustomerRepository;
import com.vaultcore.vbms.repository.LoanRepository;
import com.vaultcore.vbms.security.AuthorizationService.Caller;

@Service
public class LoanService {
    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;
    private static final Map<String, BigDecimal> INTEREST_RATES = Map.of(
            "personal loan", new BigDecimal("10.5"),
            "home loan", new BigDecimal("8.5"),
            "education loan", new BigDecimal("7.5"),
            "vehicle loan", new BigDecimal("9.0"));

    private final LoanRepository loans;
    private final CustomerRepository customers;

    public LoanService(LoanRepository loans, CustomerRepository customers) {
        this.loans = loans;
        this.customers = customers;
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> mine(Caller caller) {
        if (caller.customerId() == null) return List.of();
        return loans.findByCustomerCustomerIdOrderByAppliedAtDesc(caller.customerId()).stream()
                .map(this::response)
                .toList();
    }

    @Transactional
    public LoanResponse apply(LoanApplicationRequest request, Caller caller) {
        if (caller.customerId() == null) throw new BadRequest("A customer profile is required");
        String type = request.loanType().trim();
        BigDecimal rate = INTEREST_RATES.get(type.toLowerCase(Locale.ROOT));
        if (rate == null) throw new BadRequest("Unsupported loan type");

        Customer customer = customers.findById(caller.customerId())
                .orElseThrow(() -> new BadRequest("A customer profile is required"));
        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setLoanType(type);
        loan.setPrincipal(request.principal());
        loan.setInterestRate(rate);
        loan.setTenureMonths(request.tenureMonths());
        loan.setEmi(calculateEmi(request.principal(), rate, request.tenureMonths()));
        loan.setOutstanding(request.principal());
        loan.setStatus("PENDING");
        loan.setAppliedAt(LocalDateTime.now());
        return response(loans.save(loan));
    }

    private BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRate, int tenureMonths) {
        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("1200"), MATH_CONTEXT);
        BigDecimal rateFactor = BigDecimal.ONE.add(monthlyRate, MATH_CONTEXT)
                .pow(tenureMonths, MATH_CONTEXT);
        BigDecimal numerator = principal.multiply(monthlyRate, MATH_CONTEXT)
                .multiply(rateFactor, MATH_CONTEXT);
        BigDecimal denominator = rateFactor.subtract(BigDecimal.ONE, MATH_CONTEXT);
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private LoanResponse response(Loan loan) {
        return new LoanResponse(loan.getLoanId(), loan.getLoanType(), loan.getPrincipal(), loan.getInterestRate(),
                loan.getTenureMonths(), loan.getEmi(), loan.getOutstanding(), loan.getStatus(), loan.getAppliedAt(),
                loan.getApprovedAt());
    }
}
