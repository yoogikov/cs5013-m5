import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Intentionally undocumented -- Session 5B asks students to add
// JavaDoc describing the contract of each public method.
public class OrderBook {

    private final Map<String, Order> store = new ConcurrentHashMap<>();

    public List<Order> list() {
        return List.copyOf(store.values());
    }

    public Optional<Order> find(String id) {
        return Optional.ofNullable(store.get(id));
    }

    /**
     * Create a new order for the given customer and amount, and register it in the store.
     *
     * <p>The new order has a randomly generated ID (a random UUID string), the status
     * {@link OrderStatus#NEW}, and a creation timestamp taken from the current system clock.
     * The returned order is the same instance that is stored in this order book under
     * its {@code id()}.
     *
     * <p>Thread safety: TODO
     * <p>Persistence/durability of the store: TODO
     * <p>Whether the customer must exist or be validated beyond being non-blank: TODO
     * <p>Currency, scale and rounding rules for {@code amount}: TODO
     *
     * @param customerId {@code String}: identifier of the customer placing the order;
     *                   must not be {@code null}, empty, or whitespace-only
     * @param amount     {@code BigDecimal}: order amount; must not be {@code null} and
     *                   must be strictly greater than zero
     * @return the newly created {@link Order}, never {@code null}, with status
     *         {@link OrderStatus#NEW}, a randomly generated UUID string as its ID, the given
     *         {@code customerId} and {@code amount}, and the creation time
     * @throws IllegalArgumentException if {@code customerId} is {@code null} or blank,
     *                                  or if {@code amount} is {@code null} or not positive
     */
    public Order create(String customerId, BigDecimal amount) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId required");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
        Order o = new Order(UUID.randomUUID().toString(),
                            customerId, amount,
                            OrderStatus.NEW, Instant.now());
        store.put(o.id(), o);
        return o;
    }

    public boolean cancel(String id) {
        Order existing = store.get(id);
        if (existing == null || existing.status() == OrderStatus.CANCELLED) {
            return false;
        }
        store.put(id, new Order(existing.id(), existing.customerId(),
                                existing.amount(), OrderStatus.CANCELLED,
                                existing.createdAt()));
        return true;
    }

    public BigDecimal totalFor(String customerId) {
        return store.values().stream()
                .filter(o -> customerId.equals(o.customerId())
                          && o.status() == OrderStatus.NEW)
                .map(Order::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
