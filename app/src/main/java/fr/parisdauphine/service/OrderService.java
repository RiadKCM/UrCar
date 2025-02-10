package fr.parisdauphine.service;

import fr.parisdauphine.entity.Order;
import fr.parisdauphine.entity.User;
import fr.parisdauphine.repository.OrderRepository;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.List;

public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService() {
        this.orderRepository = new OrderRepository();
    }

    public List<Order> getOrdersByUser(User user) {
        try (Session session = orderRepository.getSessionFactory().openSession()) {
            Query<Order> query = session.createQuery(
                    "FROM Order o WHERE o.user.id = :userId ORDER BY o.orderDate DESC", Order.class);
            query.setParameter("userId", user.getId());
            return query.list();
        }
    }

    public void saveOrder(Order order) {
        orderRepository.save(order);
    }
}
