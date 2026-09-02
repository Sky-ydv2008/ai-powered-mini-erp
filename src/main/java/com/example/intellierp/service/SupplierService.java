package com.example.intellierp.service;

import com.example.intellierp.entity.Product;
import com.example.intellierp.entity.Supplier;
import com.example.intellierp.entity.User;
import com.example.intellierp.exception.BadRequestException;
import com.example.intellierp.exception.ResourceNotFoundException;
import com.example.intellierp.repository.ProductRepository;
import com.example.intellierp.repository.PurchaseRepository;
import com.example.intellierp.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuditLogService auditLogService;

    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = supplierRepository.findAllByOrderByPerformanceScoreDesc();
        suppliers.forEach(this::recalculateSupplierMetrics);
        return suppliers;
    }

    public Supplier getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        recalculateSupplierMetrics(supplier);
        return supplier;
    }

    @Transactional
    public Supplier createSupplier(Supplier supplier, User user) {
        recalculateSupplierMetrics(supplier);
        Supplier saved = supplierRepository.save(supplier);
        auditLogService.logAction(user, "CREATE_SUPPLIER", "Supplier", saved.getId(),
                "Created supplier " + saved.getName(), null);
        return saved;
    }

    @Transactional
    public Supplier updateSupplier(Long id, Supplier details, User user) {
        Supplier supplier = getSupplierById(id);
        supplier.setName(details.getName());
        supplier.setContactPerson(details.getContactPerson());
        supplier.setEmail(details.getEmail());
        supplier.setPhone(details.getPhone());
        supplier.setAddress(details.getAddress());
        supplier.setLeadTimeDays(details.getLeadTimeDays());
        if (details.getOnTimeDeliveryRate() != null) supplier.setOnTimeDeliveryRate(details.getOnTimeDeliveryRate());
        if (details.getDefectRate() != null) supplier.setDefectRate(details.getDefectRate());
        if (details.getReturnRate() != null) supplier.setReturnRate(details.getReturnRate());
        if (details.getDelayedOrders() != null) supplier.setDelayedOrders(details.getDelayedOrders());

        recalculateSupplierMetrics(supplier);
        Supplier saved = supplierRepository.save(supplier);

        auditLogService.logAction(user, "UPDATE_SUPPLIER", "Supplier", saved.getId(),
                "Updated supplier " + saved.getName(), null);
        return saved;
    }

    @Transactional
    public void deleteSupplier(Long id, User user) {
        Supplier supplier = getSupplierById(id);
        if (!purchaseRepository.findBySupplierId(id).isEmpty()) {
            throw new BadRequestException("Cannot delete supplier '" + supplier.getName() + "' because purchase orders exist for this supplier.");
        }
        supplierRepository.delete(supplier);
        auditLogService.logAction(user, "DELETE_SUPPLIER", "Supplier", id,
                "Deleted supplier " + supplier.getName(), null);
    }

    public void recalculateSupplierMetrics(Supplier supplier) {
        if (supplier == null) return;

        BigDecimal onTime = supplier.getOnTimeDeliveryRate() != null ? supplier.getOnTimeDeliveryRate() : new BigDecimal("95.0");
        BigDecimal defect = supplier.getDefectRate() != null ? supplier.getDefectRate() : new BigDecimal("2.0");
        BigDecimal retRate = supplier.getReturnRate() != null ? supplier.getReturnRate() : new BigDecimal("1.5");
        BigDecimal totalPurchases = supplier.getTotalPurchases() != null ? supplier.getTotalPurchases() : BigDecimal.ZERO;
        int delayedOrders = supplier.getDelayedOrders() != null ? supplier.getDelayedOrders() : 0;

        // Quality score: 100 - (defectRate * 10)
        BigDecimal qualityScore = BigDecimal.valueOf(100).subtract(defect.multiply(BigDecimal.valueOf(10))).max(BigDecimal.ZERO).min(BigDecimal.valueOf(100));

        // Reliability score: 100 - (returnRate * 10)
        BigDecimal reliabilityScore = BigDecimal.valueOf(100).subtract(retRate.multiply(BigDecimal.valueOf(10))).max(BigDecimal.ZERO).min(BigDecimal.valueOf(100));

        // Price score: 85 baseline
        BigDecimal priceScore = new BigDecimal("85.00");

        // Performance Score = 40% Delivery + 30% Quality + 20% Price + 10% Reliability
        BigDecimal score = onTime.multiply(new BigDecimal("0.40"))
                .add(qualityScore.multiply(new BigDecimal("0.30")))
                .add(priceScore.multiply(new BigDecimal("0.20")))
                .add(reliabilityScore.multiply(new BigDecimal("0.10")))
                .setScale(2, RoundingMode.HALF_UP);

        supplier.setPerformanceScore(score);

        // Rating: (score / 20) -> 1.0 to 5.0
        BigDecimal rating = score.divide(new BigDecimal("20"), 1, RoundingMode.HALF_UP).min(new BigDecimal("5.0")).max(new BigDecimal("1.0"));
        supplier.setRating(rating);

        // Calculate Supplier Loss:
        // Defective Goods Loss: purchases * (defect / 100)
        BigDecimal defectiveLoss = totalPurchases.multiply(defect.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        // Late Delivery Loss: delayedOrders * 5000 (standard operational delay impact)
        BigDecimal delayLoss = BigDecimal.valueOf(delayedOrders).multiply(new BigDecimal("5000.00"));
        // Returned Goods Loss: purchases * (returnRate / 100)
        BigDecimal returnedLoss = totalPurchases.multiply(retRate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));

        BigDecimal totalLoss = defectiveLoss.add(delayLoss).add(returnedLoss).setScale(2, RoundingMode.HALF_UP);
        supplier.setEstimatedLoss(totalLoss);
    }

    public List<Map<String, Object>> getSupplierLossDetectorReport() {
        List<Supplier> suppliers = getAllSuppliers();
        List<Map<String, Object>> report = new ArrayList<>();

        for (Supplier s : suppliers) {
            Map<String, Object> item = new HashMap<>();
            item.put("supplierId", s.getId());
            item.put("supplierName", s.getName());
            item.put("totalPurchases", s.getTotalPurchases());
            item.put("delayedOrders", s.getDelayedOrders());
            item.put("onTimeDeliveryRate", s.getOnTimeDeliveryRate());
            item.put("defectRate", s.getDefectRate());
            item.put("returnRate", s.getReturnRate());
            item.put("performanceScore", s.getPerformanceScore());
            item.put("rating", s.getRating());
            item.put("estimatedLoss", s.getEstimatedLoss());

            BigDecimal totalPurchases = s.getTotalPurchases() != null ? s.getTotalPurchases() : BigDecimal.ZERO;
            BigDecimal defectiveLoss = totalPurchases.multiply((s.getDefectRate() != null ? s.getDefectRate() : BigDecimal.ZERO).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
            BigDecimal delayLoss = BigDecimal.valueOf(s.getDelayedOrders() != null ? s.getDelayedOrders() : 0).multiply(new BigDecimal("5000.00"));
            BigDecimal returnLoss = totalPurchases.multiply((s.getReturnRate() != null ? s.getReturnRate() : BigDecimal.ZERO).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));

            item.put("defectiveLoss", defectiveLoss);
            item.put("delayLoss", delayLoss);
            item.put("returnLoss", returnLoss);
            report.add(item);
        }

        // Sort by estimated loss descending
        report.sort((a, b) -> ((BigDecimal) b.get("estimatedLoss")).compareTo((BigDecimal) a.get("estimatedLoss")));
        return report;
    }

    public Map<String, Object> getSupplierRecommendation(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        List<Supplier> suppliers = getAllSuppliers();
        if (suppliers.isEmpty()) {
            return Collections.emptyMap();
        }

        Supplier best = suppliers.get(0); // highest performance score
        Map<String, Object> result = new HashMap<>();
        result.put("productName", product.getName());
        result.put("currentSupplier", product.getPreferredSupplier() != null ? product.getPreferredSupplier().getName() : "None");
        result.put("recommendedSupplier", best.getName());
        result.put("performanceScore", best.getPerformanceScore());
        result.put("rating", best.getRating());
        result.put("onTimeDeliveryRate", best.getOnTimeDeliveryRate() + "%");
        result.put("defectRate", best.getDefectRate() + "%");
        result.put("leadTimeDays", best.getLeadTimeDays() + " days");
        result.put("rationale", "Top reliability score with " + best.getOnTimeDeliveryRate() + "% on-time delivery and low " + best.getDefectRate() + "% defect rate.");
        return result;
    }
}
