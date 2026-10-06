# PROMPTS.md — Module 5 Hands-on

# Session 5A: AI-generated tests

## Part A – Baseline coverage

Ran `make coverage` with the three seed tests (JaCoCo 0.8.11, report at `coverage/index.html`).

Numbers for `TaxCalculator` (the class under test):

| Metric | Covered / Total | % |
|---|---|---|
| Line coverage | 20 / 41 | **48.8%** |
| Branch coverage | 15 / 44 | **34.1%** |

(The whole-project figure, which also counts the untested `OrderBook`/`Order`, is 33/80 lines = 41.3% and 15/60 branches = 25.0%.)

Branch coverage per method:

| Method | Branches covered | Branch % |
|---|---|---|
| `computeIncomeTax` | 5 / 10 | 50.0% |
| `computeVAT` | 6 / 14 | 42.9% |
| `applyExemption` | 4 / 8 | 50.0% |
| `roundToPaise` | 0 / 2 | 0.0% |
| `isEligibleForReturn` | 0 / 10 | 0.0% |

**Methods with < 50% branch coverage:** `computeVAT` (42.9%), `roundToPaise` (0%), `isEligibleForReturn` (0%).

Baseline mutation score (`make mutation`, seed tests only): 15 / 42 killed (36%), 17 mutants with no coverage.

## Part B – Generate tests with constraints

Chosen method: **`isEligibleForReturn`**. It is tied with `roundToPaise` for the lowest branch coverage (0%), and I picked it because it has 10 uncovered branches, compared with 2 for `roundToPaise`.

### Prompt (sent to an independent AI chat agent)

````
Generate JUnit 5 tests for the following method.
Rules:
- Use AAA style with explicit comments.
- One assertion per test.
- Test names of the form
  <method>_<condition>_<expected>.
- Cover: (a) null input, (b) empty input if applicable,
  (c) boundary values, (d) one representative happy path.
- Do NOT use mocks.
- Do NOT include implementation-copying assertions; each
  assertion must state a contract, not restate the code.

```java
    // Not exercised at all by the seed tests -- students must add coverage.
    public boolean isEligibleForReturn(BigDecimal grossIncome, int ageYears) {
        if (grossIncome == null || ageYears < 0) {
            return false;
        }
        BigDecimal threshold;
        if (ageYears >= 80) {
            threshold = new BigDecimal("500000");
        } else if (ageYears >= 60) {
            threshold = new BigDecimal("300000");
        } else {
            threshold = new BigDecimal("250000");
        }
        return grossIncome.compareTo(threshold) > 0;
    }
```
````

### AI reply (verbatim)

> Here are the JUnit 5 tests for `isEligibleForReturn`. Each one checks the documented contract: a person is eligible only when gross income is strictly greater than the threshold for their age bracket (below 60: 250,000; 60 to 79: 300,000; 80 and over: 500,000). Invalid input is never eligible.
>
> **Imports to add to `TaxCalculatorTest` (if they aren't there already):**
>
> ```java
> import java.math.BigDecimal;
>
> import org.junit.jupiter.api.Test;
>
> import static org.junit.jupiter.api.Assertions.assertFalse;
> import static org.junit.jupiter.api.Assertions.assertTrue;
> ```
>
> **Test methods:**
>
> ```java
>     // ---------- (a) null / invalid input ----------
>
>     @Test
>     void isEligibleForReturn_nullIncome_returnsFalse() {
>         // Arrange
>         TaxCalculator calculator = new TaxCalculator();
>
>         // Act
>         boolean eligible = calculator.isEligibleForReturn(null, 40);
>
>         // Assert
>         assertFalse(eligible, "A missing income must never be eligible for return");
>     }
>
>     @Test
>     void isEligibleForReturn_negativeAge_returnsFalse() {
>         // Arrange
>         TaxCalculator calculator = new TaxCalculator();
>         BigDecimal veryHighIncome = new BigDecimal("10000000");
>
>         // Act
>         boolean eligible = calculator.isEligibleForReturn(veryHighIncome, -1);
>
>         // Assert
>         assertFalse(eligible, "An invalid (negative) age must never be eligible, regardless of income");
>     }
>
>     // ---------- (b) "empty" input: zero income ----------
>
>     @Test
>     void isEligibleForReturn_zeroIncome_returnsFalse() {
>         // Arrange
>         TaxCalculator calculator = new TaxCalculator();
>
>         // Act
>         boolean eligible = calculator.isEligibleForReturn(BigDecimal.ZERO, 30);
>
>         // Assert
>         assertFalse(eligible, "Someone with no income is not eligible for return");
>     }
>
>     // ---------- (c) boundary values: age ----------
>
>     @Test
>     void isEligibleForReturn_ageZeroIncomeAboveBaseThreshold_returnsTrue() {
>         // Arrange
>         TaxCalculator calculator = new TaxCalculator();
>         BigDecimal income = new BigDecimal("250000.01");
>
>         // Act
>         boolean eligible = calculator.isEligibleForReturn(income, 0);
>
>         // Assert
>         assertTrue(eligible, "Age 0 is a valid age and uses the below-60 threshold");
>     }
>
>     @Test
>     void isEligibleForReturn_age59IncomeBetweenBaseAndSeniorThreshold_returnsTrue() {
>         // Arrange
>         TaxCalculator calculator = new TaxCalculator();
>         BigDecimal income = new BigDecimal("280000");
>
>         // Act
>         boolean eligible = calculator.isEligibleForReturn(income, 59);
>
>         // Assert
>         assertTrue(eligible, "At 59 the 250,000 threshold still applies, so 280,000 is eligible");
>     }
>
>     @Test
>     void isEligibleForReturn_age60IncomeBetweenBaseAndSeniorThreshold_returnsFalse() {
>         // Arrange
>         TaxCalculator calculator = new TaxCalculator();
>         BigDecimal income = new BigDecimal("280000");
>
>         // Act
>         boolean eligible = calculator.isEligibleForReturn(income, 60);
>
>         // Assert
>         assertFalse(eligible, "From 60 the 300,000 threshold applies, so 280,000 is not eligible");
>     }
>
>     @Test
>     void isEligibleForReturn_age79IncomeBetweenSeniorAndSuperSeniorThreshold_returnsTrue() {
>         // Arrange
>         TaxCalculator calculator = new TaxCalculator();
>         BigDecimal income = new BigDecimal("400000");
>
>         // Act
>         boolean eligible = calculator.isEligibleForReturn(income, 79);
>
>         // Assert
>         assertTrue(eligible, "At 79 the 300,000 threshold still applies, so 400,000 is eligible");
>     }
>
>     @Test
>     void isEligibleForReturn_age80IncomeBetweenSeniorAndSuperSeniorThreshold_returnsFalse() {
>         // Arrange
>         TaxCalculator calculator = new TaxCalculator();
>         BigDecimal income = new BigDecimal("400000");
>
>         // Act
>         boolean eligible = calculator.isEligibleForReturn(income, 80);
>
>         // Assert
>         assertFalse(eligible, "From 80 the 500,000 threshold applies, so 400,000 is not eligible");
>     }
>
>     // ---------- (c) boundary values: income at / just above each threshold ----------
>
>     @Test
>     void isEligibleForReturn_below60IncomeExactlyAtThreshold_returnsFalse() {
>         // Arrange
>         TaxCalculator calculator = new TaxCalculator();
>         BigDecimal income = new BigDecimal("250000");
>
>         // Act
>         boolean eligible = calculator.isEligibleForReturn(income, 45);
>
>         // Assert
>         assertFalse(eligible, "Income equal to the threshold does not exceed it, so not eligible");
>     }
>
>     @Test
>     void isEligibleForReturn_below60IncomeOneCentAboveThreshold_returnsTrue() {
>         // Arrange
>         TaxCalculator calculator = new TaxCalculator();
>         BigDecimal income = new BigDecimal("250000.01");
>
>         // Act
>         boolean eligible = calculator.isEligibleForReturn(income, 45);
>
>         // Assert
>         assertTrue(eligible, "Any income strictly above the threshold is eligible");
>     }
>
>     @Test
>     void isEligibleForReturn_below60IncomeAtThresholdWithTrailingZeros_returnsFalse() {
>         // Arrange
>         TaxCalculator calculator = new TaxCalculator();
>         BigDecimal income = new BigDecimal("250000.00");
>
>         // Act
>         boolean eligible = calculator.isEligibleForReturn(income, 45);
>
>         // Assert
>         assertFalse(eligible, "Eligibility depends on numeric value, not scale: 250000.00 equals the threshold");
>     }
>
>     @Test
>     void isEligibleForReturn_seniorIncomeExactlyAtThreshold_returnsFalse() {
>         // Arrange
>         TaxCalculator calculator = new TaxCalculator();
>         BigDecimal income = new BigDecimal("300000");
>
>         // Act
>         boolean eligible = calculator.isEligibleForReturn(income, 65);
>
>         // Assert
>         assertFalse(eligible, "A senior with income equal to the threshold is not eligible");
>     }
>
>     @Test
>     void isEligibleForReturn_seniorIncomeOneCentAboveThreshold_returnsTrue() {
>         // Arrange
>         TaxCalculator calculator = new TaxCalculator();
>         BigDecimal income = new BigDecimal("300000.01");
>
>         // Act
>         boolean eligible = calculator.isEligibleForReturn(income, 65);
>
>         // Assert
>         assertTrue(eligible, "A senior with income strictly above the threshold is eligible");
>     }
>
>     @Test
>     void isEligibleForReturn_superSeniorIncomeExactlyAtThreshold_returnsFalse() {
>         // Arrange
>         TaxCalculator calculator = new TaxCalculator();
>         BigDecimal income = new BigDecimal("500000");
>
>         // Act
>         boolean eligible = calculator.isEligibleForReturn(income, 85);
>
>         // Assert
>         assertFalse(eligible, "A super-senior with income equal to the threshold is not eligible");
>     }
>
>     @Test
>     void isEligibleForReturn_superSeniorIncomeOneCentAboveThreshold_returnsTrue() {
>         // Arrange
>         TaxCalculator calculator = new TaxCalculator();
>         BigDecimal income = new BigDecimal("500000.01");
>
>         // Act
>         boolean eligible = calculator.isEligibleForReturn(income, 85);
>
>         // Assert
>         assertTrue(eligible, "A super-senior with income strictly above the threshold is eligible");
>     }
>
>     // ---------- (d) representative happy path ----------
>
>     @Test
>     void isEligibleForReturn_workingAgeAdultWithIncomeWellAboveThreshold_returnsTrue() {
>         // Arrange
>         TaxCalculator calculator = new TaxCalculator();
>         BigDecimal income = new BigDecimal("1200000");
>
>         // Act
>         boolean eligible = calculator.isEligibleForReturn(income, 35);
>
>         // Assert
>         assertTrue(eligible, "A 35-year-old earning 1,200,000 is eligible for return");
>     }
> ```
>
> **Notes:**
> - **Empty input:** `BigDecimal` and `int` have no "empty" value, so zero income is the closest equivalent. If your assignment says empty input doesn't apply here, you can drop that test.
> - **Age-boundary tests:** these come in pairs (59/60 and 79/80) that use the same income. When the expected result flips between the two ages, that proves the bracket changes at that age. The income is chosen between the two thresholds, so the test isn't just repeating a constant from the code.
> - **Trailing-zeros test:** this pins down that `250000.00` counts the same as `250000`. A version using `equals` instead of `compareTo` would fail it.
> - **Constructor:** every test assumes `TaxCalculator` has a public no-argument constructor. If your existing test class already has a shared `calculator` field (for example one set up in `@BeforeEach`), remove the `new TaxCalculator()` line from each Arrange block and use that field instead.

All 16 generated test methods were pasted into `test/TaxCalculatorTest.java` (the imports were already covered by the existing `Assertions.*` wildcard import). `make test`: **19 tests found, 19 successful, 0 failed.**

## Part C – Read every assertion

For each assertion: does it encode a contract, or does it restate the code? Every expected value here is a boolean (`assertTrue`/`assertFalse`). None is computed from the input in a way that mirrors the method body. The question that remains is whether the *input → expected* pairing comes from the domain rule or was just read off the code.

The rule being tested: an individual must file a return only when gross income is **strictly above** the basic exemption limit for their age band. Those limits are Rs 2.5 L below 60, Rs 3 L for seniors aged 60–79, and Rs 5 L for super-seniors aged 80+, which are the real old-regime limits. Invalid input (null income, negative age) never triggers eligibility.

| # | Test | Verdict | Reasoning |
|---|---|---|---|
| 1 | `nullIncome_returnsFalse` | Contract | Missing data must not make someone eligible. |
| 2 | `negativeAge_returnsFalse` | Contract | Uses a very high income so the result can only come from the invalid-age rule. |
| 3 | `zeroIncome_returnsFalse` | Contract | No income means nothing to file. This is the closest thing to "empty input". |
| 4 | `ageZeroIncomeAboveBaseThreshold_returnsTrue` | Contract (kept) | Checks that 0 is a valid age, which is the boundary of the "negative age" guard. Odd in real life, but it pins the validity boundary. |
| 5–6 | `age59…_returnsTrue` / `age60…_returnsFalse` | Contract | Same income (2.8 L, between the two limits) on either side of the senior-citizen age. The flip in result encodes "seniors start at 60". |
| 7–8 | `age79…_returnsTrue` / `age80…_returnsFalse` | Contract | Same pattern for the super-senior age of 80. |
| 9–10 | `below60 … ExactlyAt / OneCentAbove` | Contract | Encodes "strictly above the limit" at the Rs 2.5 L limit. |
| 11 | `below60IncomeAtThresholdWithTrailingZeros_returnsFalse` | Contract (kept) | Rs 250000.00 is the same amount of money as Rs 250000, so eligibility must not depend on `BigDecimal` scale. |
| 12–13 | `senior … ExactlyAt / OneCentAbove` | Contract | Same "strictly above" rule at the Rs 3 L limit. |
| 14–15 | `superSenior … ExactlyAt / OneCentAbove` | Contract | Same "strictly above" rule at the Rs 5 L limit. |
| 16 | `workingAgeAdultWithIncomeWellAboveThreshold_returnsTrue` | Contract | Representative happy path. |

**Deleted / rewritten:** none. No assertion fails the "does this encode a contract" check. Because the method returns a boolean, the "expected = same formula as the code" pitfall can't really occur here.

Two caveats I noted:
- The thresholds in the tests have the same values as the constants in the code. That's acceptable because they are the published exemption limits, not something only the code defines. If the code had an arbitrary magic number, repeating it in the test would be weaker.
- The AI's note that "a version using `equals` instead of `compareTo` would fail" the trailing-zeros test is loosely worded, because `equals` cannot express `>` at all. The test itself is fine.

## Part D – Mutation testing

`make mutation` after adding the AI tests (report: `build/reports/pitest/index.html`).

**Chosen method `isEligibleForReturn`:** mutation score **11 / 11 (100%)**, **no surviving mutants**. Before the AI tests it was 0 / 11, all `NO_COVERAGE`.

Whole `TaxCalculator`: **26 / 42 killed (62%)**, up from 15 / 42 (36%). 6 mutants have no coverage and 10 survive.

The chosen method has no survivors, so I took a surviving mutant from elsewhere in the class. The surviving mutants at this point:

| Line | Method | Mutation | Note |
|---|---|---|---|
| 13 | `computeIncomeTax` | changed conditional boundary (`signum() < 0` → `<= 0`) | Killable: zero income would throw |
| 20 | `computeIncomeTax` | changed conditional boundary (`<= slab1` → `< slab1`) | Equivalent: income = 250000 gives 0 tax either way |
| 23 | `computeIncomeTax` | changed conditional boundary (`<= slab2` → `< slab2`) | Equivalent: income = 500000 gives 12500 either way |
| 26 | `computeIncomeTax` | changed conditional boundary (`<= slab3` → `< slab3`) | Equivalent: income = 1000000 gives 112500 either way |
| 38 | `computeVAT` | changed conditional boundary (`signum() < 0` → `<= 0`) | Killable: amount 0 would throw |
| 41 | `computeVAT` | negated conditional ×3 | GST rates 5 / 12 / 28 never tested |
| 55 | `applyExemption` | changed conditional boundary (`> cap` → `>= cap`) | Equivalent: exemption = cap gives cap either way |
| 57 | `applyExemption` | changed conditional boundary (`signum() < 0` → `<= 0`) | Equivalent in value (0 either way) |

Chosen mutant: **line 13, `income.signum() < 0` → `income.signum() <= 0`** in `computeIncomeTax`.

### Prompt (sent to a fresh independent AI chat agent, with the method source attached for reference)

```
The mutant at line 13 (`income.signum() < 0` -> `income.signum() <= 0`) survived
the test suite. Write ONE new JUnit test that would kill this
mutant without changing any other test. Explain in one
sentence why this test kills the mutant.
```

### AI reply (verbatim)

> Here's a test that kills the mutant:
>
> ```java
>     @Test
>     void computeIncomeTax_zeroIncome_returnsZero() {
>         // Arrange
>         TaxCalculator calculator = new TaxCalculator();
>         BigDecimal income = BigDecimal.ZERO;
>
>         // Act
>         BigDecimal tax = calculator.computeIncomeTax(income);
>
>         // Assert
>         assertEquals(0, tax.compareTo(BigDecimal.ZERO));
>     }
> ```
>
> **Why it kills the mutant:** An income of exactly zero is the only input where `signum() < 0` and `signum() <= 0` give different results: the original code lets zero through and returns a tax of zero, but the mutant throws an `IllegalArgumentException`, so the test fails against the mutant.
>
> (Note: I used `compareTo` rather than `assertEquals(BigDecimal.ZERO, tax)` because `BigDecimal.equals` also compares scale. Here that doesn't matter, since the method returns `BigDecimal.ZERO` itself, but `compareTo` keeps the assertion robust if the implementation changes.)

I added the test unchanged as `computeIncomeTax_zeroIncome_returnsZero`. It encodes a real contract: zero income is valid and owes zero tax. `make test`: **20 / 20 pass**.

Re-ran `make mutation`: the **line-13 conditional-boundary mutant is now KILLED**. The test also covers the `return tax` path on line 21, so the `NO_COVERAGE` "replaced return value with null" mutant there is killed too.

# Part E – Reflect

**Coverage, `TaxCalculator`:**

| Metric | Baseline (seed tests) | After AI tests + targeted test | Change |
|---|---|---|---|
| Line coverage | 20 / 41 (48.8%) | 29 / 41 (70.7%) | +21.9 pp |
| Branch coverage | 15 / 44 (34.1%) | 26 / 44 (59.1%) | +25.0 pp |
| `isEligibleForReturn` branch coverage | 0 / 10 (0%) | 10 / 10 (100%) | +100 pp |

**Mutation score:**

| Scope | Seed only | After AI tests, before targeted test | After targeted test |
|---|---|---|---|
| `isEligibleForReturn` | 0 / 11 (0%) | 11 / 11 (100%) | 11 / 11 (100%) |
| `computeIncomeTax` (targeted mutant's method) | 6 / 13 (46%) | 6 / 13 (46%) | 8 / 13 (62%) |
| Whole `TaxCalculator` | 15 / 42 (36%) | 26 / 42 (62%) | **28 / 42 (67%)** |

**Usefulness:** Mutation testing felt more useful than coverage. Line coverage already showed `computeIncomeTax`'s guard line as covered, yet a zero-income bug could slip through. PIT pointed to that exact boundary, and it also showed which survivors were equivalent mutants that no test can kill.
