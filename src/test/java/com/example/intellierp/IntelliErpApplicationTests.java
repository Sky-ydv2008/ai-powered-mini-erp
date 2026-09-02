package com.example.intellierp;

import com.example.intellierp.dto.LoginRequest;
import com.example.intellierp.dto.SaleCreateDto;
import com.example.intellierp.entity.*;
import com.example.intellierp.entity.enums.PaymentMethod;
import com.example.intellierp.entity.enums.ProductStatus;
import com.example.intellierp.entity.enums.SaleStatus;
import com.example.intellierp.exception.InsufficientStockException;
import com.example.intellierp.repository.*;
import com.example.intellierp.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class IntelliErpApplicationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleService saleService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProfitLossService profitLossService;

    @Autowired
    private AiInsightService aiInsightService;

    @Autowired
    private UserRepository userRepository;

    private User adminUser;

    @BeforeEach
    void setUp() {
        adminUser = userRepository.findByUsername("admin").orElse(null);
    }

    @Test
    @DisplayName("Context loads successfully")
    void contextLoads() {
        assertNotNull(adminUser);
    }

    @Test
    @DisplayName("Test 1: Authentication succeeds with correct credentials")
    void testAuthentication() {
        LoginRequest req = new LoginRequest("admin", "admin123");
        var response = authService.authenticateUser(req);
        assertNotNull(response.getToken());
        assertEquals("admin", response.getUsername());
    }

    @Test
    @DisplayName("Test 2: Sale decreases stock and calculates profit")
    void testSaleDecreasesStock() {
        Product product = productRepository.findAll().get(0);
        int initialStock = product.getCurrentStock();
        assertTrue(initialStock >= 2, "Product should have initial stock for test");

        SaleCreateDto dto = new SaleCreateDto();
        dto.setPaymentMethod(PaymentMethod.CASH);
        SaleCreateDto.SaleItemDto item = new SaleCreateDto.SaleItemDto();
        item.setProductId(product.getId());
        item.setQuantity(2);
        item.setSellingPrice(product.getSellingPrice());
        dto.setItems(List.of(item));

        Sale sale = saleService.createSale(dto, adminUser);
        assertNotNull(sale);
        assertEquals(SaleStatus.COMPLETED, sale.getStatus());

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(initialStock - 2, updatedProduct.getCurrentStock());
        assertTrue(sale.getProfit().compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    @DisplayName("Test 3: Cannot sell more stock than available (Business Rule)")
    void testCannotSellMoreThanAvailable() {
        Product product = productRepository.findAll().get(0);
        int currentStock = product.getCurrentStock();

        SaleCreateDto dto = new SaleCreateDto();
        dto.setPaymentMethod(PaymentMethod.CARD);
        SaleCreateDto.SaleItemDto item = new SaleCreateDto.SaleItemDto();
        item.setProductId(product.getId());
        item.setQuantity(currentStock + 500); // Excess quantity
        item.setSellingPrice(product.getSellingPrice());
        dto.setItems(List.of(item));

        assertThrows(InsufficientStockException.class, () -> saleService.createSale(dto, adminUser));
    }

    @Test
    @DisplayName("Test 4: Cancelled sale restores product stock")
    void testCancelledSaleRestoresStock() {
        Product product = productRepository.findAll().get(0);
        int stockBeforeSale = product.getCurrentStock();

        SaleCreateDto dto = new SaleCreateDto();
        SaleCreateDto.SaleItemDto item = new SaleCreateDto.SaleItemDto();
        item.setProductId(product.getId());
        item.setQuantity(1);
        item.setSellingPrice(product.getSellingPrice());
        dto.setItems(List.of(item));

        Sale sale = saleService.createSale(dto, adminUser);
        assertEquals(stockBeforeSale - 1, productRepository.findById(product.getId()).get().getCurrentStock());

        saleService.cancelSale(sale.getId(), "Customer cancellation test", adminUser);
        Product restoredProduct = productRepository.findById(product.getId()).get();
        assertEquals(stockBeforeSale, restoredProduct.getCurrentStock());
    }

    @Test
    @DisplayName("Test 5: Profit/Loss calculation computes Gross Profit, Net Profit, and Margin %")
    void testProfitLossCalculation() {
        Map<String, Object> pl = profitLossService.calculateProfitLoss("this_month", null, null);
        assertNotNull(pl);
        assertTrue(pl.containsKey("revenue"));
        assertTrue(pl.containsKey("costOfGoodsSold"));
        assertTrue(pl.containsKey("grossProfit"));
        assertTrue(pl.containsKey("netProfit"));
        assertTrue(pl.containsKey("profitMargin"));

        BigDecimal rev = (BigDecimal) pl.get("revenue");
        BigDecimal cogs = (BigDecimal) pl.get("costOfGoodsSold");
        BigDecimal gross = (BigDecimal) pl.get("grossProfit");
        assertEquals(rev.subtract(cogs), gross);
    }

    @Test
    @DisplayName("Test 6: Supplier Loss Detector quantifies defect and delay losses")
    void testSupplierLossCalculation() {
        Supplier sBharat = supplierRepository.findByNameIgnoreCase("Bharat Polymer Solutions").orElse(null);
        assertNotNull(sBharat);
        supplierService.recalculateSupplierMetrics(sBharat);

        assertTrue(sBharat.getEstimatedLoss().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(sBharat.getDelayedOrders() > 0);
    }

    @Test
    @DisplayName("Test 7: AI Insights Engine generates structured Explainable Insights")
    void testAiInsightEngine() {
        List<AiInsight> insights = aiInsightService.generateAllInsights();
        assertFalse(insights.isEmpty(), "AI Engine should discover anomalies in seed data");

        AiInsight first = insights.get(0);
        assertNotNull(first.getTitle());
        assertNotNull(first.getRootCause());
        assertNotNull(first.getRecommendation());
        assertNotNull(first.getSeverity());
    }

    @Test
    @DisplayName("Test 8: AI Assistant answers business queries")
    void testAiAssistantQuery() {
        Map<String, Object> resp = aiInsightService.askBusinessAssistant("Why did my profit decrease this month?");
        assertNotNull(resp);
        assertEquals("PROFIT_DIAGNOSTIC", resp.get("intent"));
        assertNotNull(resp.get("explanation"));
        assertNotNull(resp.get("recommendations"));
    }
}
