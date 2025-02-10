package fr.parisdauphine.service;

import fr.parisdauphine.entity.Car;
import fr.parisdauphine.entity.Cart;
import fr.parisdauphine.entity.Order;
import fr.parisdauphine.entity.User;
import fr.parisdauphine.repository.CartRepository;
import fr.parisdauphine.repository.OrderRepository;

import java.util.ArrayList;

public class CartService {

    private CartRepository cartRepository;
    private OrderRepository orderRepository;

    public CartService() {
        this.cartRepository = new CartRepository();
        this.orderRepository = new OrderRepository();
    }

    public void removeFromCart(User user, Car car) {
        cartRepository.removeCarFromCart(user, car);
    }

    public void addToCart(User currentUser, Car car) {
        cartRepository.addCarToCart(currentUser, car);
    }

    // Nouvelle méthode pour créer la commande à partir du panier
    public void placeOrder(User user) {
        Cart cart = cartRepository.findCartByUser(user);
        if (cart == null || cart.getCars().isEmpty()) {
            throw new IllegalStateException("Le panier est vide, impossible de créer une commande.");
        }

        // Crée une nouvelle commande à partir du panier
        Order order = new Order();
        order.setUser(user);
        order.setCars(cart.getCars());  // Liste des voitures du panier
        order.setOrderDate(java.time.LocalDateTime.now());  // Date de la commande
        order.setStatus(Order.Status.EN_COURS);  // Statut de la commande (à adapter selon votre logique)

        // Enregistre la commande dans la base de données
        orderRepository.save(order);

    }
}
