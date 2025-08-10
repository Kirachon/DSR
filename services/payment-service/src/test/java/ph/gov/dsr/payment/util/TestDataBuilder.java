package ph.gov.dsr.payment.util;

import ph.gov.dsr.payment.dto.*;
import ph.gov.dsr.payment.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Utility class for building test data objects
 */
public class TestDataBuilder {

    public static class PaymentRequestBuilder {
        private String householdId = "HH-001";
        private String programName = "4Ps";
        private BigDecimal amount = new BigDecimal("1400.00");
        private Payment.PaymentMethod paymentMethod = Payment.PaymentMethod.BANK_TRANSFER;
        private PaymentRequest.BeneficiaryAccount beneficiaryAccount = defaultBeneficiaryAccount();
        private String description = "Test payment";
        private Map<String, Object> metadata = new HashMap<>();

        public PaymentRequestBuilder householdId(String householdId) {
            this.householdId = householdId;
            return this;
        }

        public PaymentRequestBuilder programName(String programName) {
            this.programName = programName;
            return this;
        }

        public PaymentRequestBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public PaymentRequestBuilder paymentMethod(Payment.PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public PaymentRequestBuilder beneficiaryAccount(PaymentRequest.BeneficiaryAccount beneficiaryAccount) {
            this.beneficiaryAccount = beneficiaryAccount;
            return this;
        }

        public PaymentRequestBuilder description(String description) {
            this.description = description;
            return this;
        }

        public PaymentRequestBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public PaymentRequest build() {
            return PaymentRequest.builder()
                .householdId(householdId)
                .programName(programName)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .beneficiaryAccount(beneficiaryAccount)
                .description(description)
                .metadata(metadata)
                .build();
        }

        private static PaymentRequest.BeneficiaryAccount defaultBeneficiaryAccount() {
            return PaymentRequest.BeneficiaryAccount.builder()
                .accountNumber("1234567890")
                .bankCode("LBP")
                .accountName("Juan Dela Cruz")
                .build();
        }
    }

    public static class PaymentBuilder {
        private UUID paymentId = UUID.randomUUID();
        private String householdId = "HH-001";
        private String programName = "4Ps";
        private BigDecimal amount = new BigDecimal("1400.00");
        private Payment.PaymentMethod paymentMethod = Payment.PaymentMethod.BANK_TRANSFER;
        private Payment.PaymentStatus status = Payment.PaymentStatus.PENDING;
        private String internalReferenceNumber = "PAY-2024-001";
        private String createdBy = "test-user";
        private LocalDateTime createdDate = LocalDateTime.now();

        public PaymentBuilder paymentId(UUID paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public PaymentBuilder householdId(String householdId) {
            this.householdId = householdId;
            return this;
        }

        public PaymentBuilder programName(String programName) {
            this.programName = programName;
            return this;
        }

        public PaymentBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public PaymentBuilder paymentMethod(Payment.PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public PaymentBuilder status(Payment.PaymentStatus status) {
            this.status = status;
            return this;
        }

        public PaymentBuilder internalReferenceNumber(String internalReferenceNumber) {
            this.internalReferenceNumber = internalReferenceNumber;
            return this;
        }

        public PaymentBuilder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public PaymentBuilder createdDate(LocalDateTime createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public Payment build() {
            return Payment.builder()
                .paymentId(paymentId)
                .householdId(householdId)
                .programName(programName)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .status(status)
                .internalReferenceNumber(internalReferenceNumber)
                .createdBy(createdBy)
                .createdDate(createdDate)
                .build();
        }
    }

    public static class PaymentBatchRequestBuilder {
        private UUID programId = UUID.randomUUID();
        private String programName = "4Ps";
        private List<PaymentRequest> paymentRequests = defaultPaymentRequests();
        private LocalDateTime scheduledDate = LocalDateTime.now().plusDays(1);
        private Map<String, Object> metadata = new HashMap<>();

        public PaymentBatchRequestBuilder programId(UUID programId) {
            this.programId = programId;
            return this;
        }

        public PaymentBatchRequestBuilder programName(String programName) {
            this.programName = programName;
            return this;
        }

        public PaymentBatchRequestBuilder paymentRequests(List<PaymentRequest> paymentRequests) {
            this.paymentRequests = paymentRequests;
            return this;
        }

        public PaymentBatchRequestBuilder scheduledDate(LocalDateTime scheduledDate) {
            this.scheduledDate = scheduledDate;
            return this;
        }

        public PaymentBatchRequestBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public PaymentBatchRequest build() {
            return PaymentBatchRequest.builder()
                .programId(programId)
                .programName(programName)
                .paymentRequests(paymentRequests)
                .scheduledDate(scheduledDate)
                .metadata(metadata)
                .build();
        }

        private static List<PaymentRequest> defaultPaymentRequests() {
            return Arrays.asList(
                new PaymentRequestBuilder().householdId("HH-001").build(),
                new PaymentRequestBuilder().householdId("HH-002").build()
            );
        }
    }

    public static class PaymentBatchBuilder {
        private UUID batchId = UUID.randomUUID();
        private String batchNumber = "BATCH-2024-001";
        private UUID programId = UUID.randomUUID();
        private String programName = "4Ps";
        private int totalPayments = 2;
        private BigDecimal totalAmount = new BigDecimal("2800.00");
        private PaymentBatch.BatchStatus status = PaymentBatch.BatchStatus.PENDING;
        private String createdBy = "test-user";
        private LocalDateTime createdDate = LocalDateTime.now();

        public PaymentBatchBuilder batchId(UUID batchId) {
            this.batchId = batchId;
            return this;
        }

        public PaymentBatchBuilder batchNumber(String batchNumber) {
            this.batchNumber = batchNumber;
            return this;
        }

        public PaymentBatchBuilder programId(UUID programId) {
            this.programId = programId;
            return this;
        }

        public PaymentBatchBuilder programName(String programName) {
            this.programName = programName;
            return this;
        }

        public PaymentBatchBuilder totalPayments(int totalPayments) {
            this.totalPayments = totalPayments;
            return this;
        }

        public PaymentBatchBuilder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public PaymentBatchBuilder status(PaymentBatch.BatchStatus status) {
            this.status = status;
            return this;
        }

        public PaymentBatchBuilder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public PaymentBatchBuilder createdDate(LocalDateTime createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public PaymentBatch build() {
            return PaymentBatch.builder()
                .batchId(batchId)
                .batchNumber(batchNumber)
                .programId(programId)
                .programName(programName)
                .totalPayments(totalPayments)
                .totalAmount(totalAmount)
                .status(status)
                .createdBy(createdBy)
                .createdDate(createdDate)
                .build();
        }
    }

    // Static factory methods for convenience
    public static PaymentRequestBuilder paymentRequest() {
        return new PaymentRequestBuilder();
    }

    public static PaymentBuilder payment() {
        return new PaymentBuilder();
    }

    public static PaymentBatchRequestBuilder paymentBatchRequest() {
        return new PaymentBatchRequestBuilder();
    }

    public static PaymentBatchBuilder paymentBatch() {
        return new PaymentBatchBuilder();
    }
}
