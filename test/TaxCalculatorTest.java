import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

/**
 * Three seed tests. Coverage is intentionally light:
 *  - computeIncomeTax: only the middle slab branch is hit
 *  - computeVAT: only the 18% rate is hit
 *  - applyExemption: only the "under cap" branch is hit
 *  - roundToPaise: not exercised
 *  - isEligibleForReturn: not exercised at all
 */
public class TaxCalculatorTest {

    @Test
    public void incomeTax_middleSlab() {
        TaxCalculator c = new TaxCalculator();
        BigDecimal tax = c.computeIncomeTax(new BigDecimal("700000"));
        // 12500 (5% on 250k-500k) + 40000 (20% on 500k-700k) = 52500
        assertEquals(0, tax.compareTo(new BigDecimal("52500.00")));
    }

    @Test
    public void vat_eighteenPercent() {
        TaxCalculator c = new TaxCalculator();
        BigDecimal gst = c.computeVAT(new BigDecimal("1000"), 18);
        assertEquals(0, gst.compareTo(new BigDecimal("180.00")));
    }

    @Test
    public void exemption_underCap() {
        TaxCalculator c = new TaxCalculator();
        BigDecimal net = c.applyExemption(
                new BigDecimal("600000"), new BigDecimal("120000"));
        assertEquals(0, net.compareTo(new BigDecimal("480000")));
    }

    // ===== AI-generated tests for isEligibleForReturn (Session 5A, Part B) =====

    // ---------- (a) null / invalid input ----------

    @Test
    void isEligibleForReturn_nullIncome_returnsFalse() {
        // Arrange
        TaxCalculator calculator = new TaxCalculator();

        // Act
        boolean eligible = calculator.isEligibleForReturn(null, 40);

        // Assert
        assertFalse(eligible, "A missing income must never be eligible for return");
    }

    @Test
    void isEligibleForReturn_negativeAge_returnsFalse() {
        // Arrange
        TaxCalculator calculator = new TaxCalculator();
        BigDecimal veryHighIncome = new BigDecimal("10000000");

        // Act
        boolean eligible = calculator.isEligibleForReturn(veryHighIncome, -1);

        // Assert
        assertFalse(eligible, "An invalid (negative) age must never be eligible, regardless of income");
    }

    // ---------- (b) "empty" input: zero income ----------

    @Test
    void isEligibleForReturn_zeroIncome_returnsFalse() {
        // Arrange
        TaxCalculator calculator = new TaxCalculator();

        // Act
        boolean eligible = calculator.isEligibleForReturn(BigDecimal.ZERO, 30);

        // Assert
        assertFalse(eligible, "Someone with no income is not eligible for return");
    }

    // ---------- (c) boundary values: age ----------

    @Test
    void isEligibleForReturn_ageZeroIncomeAboveBaseThreshold_returnsTrue() {
        // Arrange
        TaxCalculator calculator = new TaxCalculator();
        BigDecimal income = new BigDecimal("250000.01");

        // Act
        boolean eligible = calculator.isEligibleForReturn(income, 0);

        // Assert
        assertTrue(eligible, "Age 0 is a valid age and uses the below-60 threshold");
    }

    @Test
    void isEligibleForReturn_age59IncomeBetweenBaseAndSeniorThreshold_returnsTrue() {
        // Arrange
        TaxCalculator calculator = new TaxCalculator();
        BigDecimal income = new BigDecimal("280000");

        // Act
        boolean eligible = calculator.isEligibleForReturn(income, 59);

        // Assert
        assertTrue(eligible, "At 59 the 250,000 threshold still applies, so 280,000 is eligible");
    }

    @Test
    void isEligibleForReturn_age60IncomeBetweenBaseAndSeniorThreshold_returnsFalse() {
        // Arrange
        TaxCalculator calculator = new TaxCalculator();
        BigDecimal income = new BigDecimal("280000");

        // Act
        boolean eligible = calculator.isEligibleForReturn(income, 60);

        // Assert
        assertFalse(eligible, "From 60 the 300,000 threshold applies, so 280,000 is not eligible");
    }

    @Test
    void isEligibleForReturn_age79IncomeBetweenSeniorAndSuperSeniorThreshold_returnsTrue() {
        // Arrange
        TaxCalculator calculator = new TaxCalculator();
        BigDecimal income = new BigDecimal("400000");

        // Act
        boolean eligible = calculator.isEligibleForReturn(income, 79);

        // Assert
        assertTrue(eligible, "At 79 the 300,000 threshold still applies, so 400,000 is eligible");
    }

    @Test
    void isEligibleForReturn_age80IncomeBetweenSeniorAndSuperSeniorThreshold_returnsFalse() {
        // Arrange
        TaxCalculator calculator = new TaxCalculator();
        BigDecimal income = new BigDecimal("400000");

        // Act
        boolean eligible = calculator.isEligibleForReturn(income, 80);

        // Assert
        assertFalse(eligible, "From 80 the 500,000 threshold applies, so 400,000 is not eligible");
    }

    // ---------- (c) boundary values: income at / just above each threshold ----------

    @Test
    void isEligibleForReturn_below60IncomeExactlyAtThreshold_returnsFalse() {
        // Arrange
        TaxCalculator calculator = new TaxCalculator();
        BigDecimal income = new BigDecimal("250000");

        // Act
        boolean eligible = calculator.isEligibleForReturn(income, 45);

        // Assert
        assertFalse(eligible, "Income equal to the threshold does not exceed it, so not eligible");
    }

    @Test
    void isEligibleForReturn_below60IncomeOneCentAboveThreshold_returnsTrue() {
        // Arrange
        TaxCalculator calculator = new TaxCalculator();
        BigDecimal income = new BigDecimal("250000.01");

        // Act
        boolean eligible = calculator.isEligibleForReturn(income, 45);

        // Assert
        assertTrue(eligible, "Any income strictly above the threshold is eligible");
    }

    @Test
    void isEligibleForReturn_below60IncomeAtThresholdWithTrailingZeros_returnsFalse() {
        // Arrange
        TaxCalculator calculator = new TaxCalculator();
        BigDecimal income = new BigDecimal("250000.00");

        // Act
        boolean eligible = calculator.isEligibleForReturn(income, 45);

        // Assert
        assertFalse(eligible, "Eligibility depends on numeric value, not scale: 250000.00 equals the threshold");
    }

    @Test
    void isEligibleForReturn_seniorIncomeExactlyAtThreshold_returnsFalse() {
        // Arrange
        TaxCalculator calculator = new TaxCalculator();
        BigDecimal income = new BigDecimal("300000");

        // Act
        boolean eligible = calculator.isEligibleForReturn(income, 65);

        // Assert
        assertFalse(eligible, "A senior with income equal to the threshold is not eligible");
    }

    @Test
    void isEligibleForReturn_seniorIncomeOneCentAboveThreshold_returnsTrue() {
        // Arrange
        TaxCalculator calculator = new TaxCalculator();
        BigDecimal income = new BigDecimal("300000.01");

        // Act
        boolean eligible = calculator.isEligibleForReturn(income, 65);

        // Assert
        assertTrue(eligible, "A senior with income strictly above the threshold is eligible");
    }

    @Test
    void isEligibleForReturn_superSeniorIncomeExactlyAtThreshold_returnsFalse() {
        // Arrange
        TaxCalculator calculator = new TaxCalculator();
        BigDecimal income = new BigDecimal("500000");

        // Act
        boolean eligible = calculator.isEligibleForReturn(income, 85);

        // Assert
        assertFalse(eligible, "A super-senior with income equal to the threshold is not eligible");
    }

    @Test
    void isEligibleForReturn_superSeniorIncomeOneCentAboveThreshold_returnsTrue() {
        // Arrange
        TaxCalculator calculator = new TaxCalculator();
        BigDecimal income = new BigDecimal("500000.01");

        // Act
        boolean eligible = calculator.isEligibleForReturn(income, 85);

        // Assert
        assertTrue(eligible, "A super-senior with income strictly above the threshold is eligible");
    }

    // ---------- (d) representative happy path ----------

    @Test
    void isEligibleForReturn_workingAgeAdultWithIncomeWellAboveThreshold_returnsTrue() {
        // Arrange
        TaxCalculator calculator = new TaxCalculator();
        BigDecimal income = new BigDecimal("1200000");

        // Act
        boolean eligible = calculator.isEligibleForReturn(income, 35);

        // Assert
        assertTrue(eligible, "A 35-year-old earning 1,200,000 is eligible for return");
    }

    // ===== Targeted test to kill the surviving L13 mutant (Session 5A, Part D) =====

    @Test
    void computeIncomeTax_zeroIncome_returnsZero() {
        // Arrange
        TaxCalculator calculator = new TaxCalculator();
        BigDecimal income = BigDecimal.ZERO;

        // Act
        BigDecimal tax = calculator.computeIncomeTax(income);

        // Assert
        assertEquals(0, tax.compareTo(BigDecimal.ZERO));
    }
}
