package fr.parisdauphine.service;

import fr.parisdauphine.entity.Car;
import fr.parisdauphine.entity.Cart;
import fr.parisdauphine.entity.Order;
import fr.parisdauphine.entity.User;
import fr.parisdauphine.repository.CarRepository;
import fr.parisdauphine.repository.CartRepository;
import fr.parisdauphine.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;

public class CartService {

    private CartRepository cartRepository;
    private OrderRepository orderRepository;
    private CarRepository carRepository;

    public CartService() {
        this.cartRepository = new CartRepository();
        this.orderRepository = new OrderRepository();
        this.carRepository = new CarRepository();
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

        carRepository.updateCarStatusToSold(cart.getCars());

        cart.getCars().clear();
        cartRepository.update(cart);
    }

    public List<Car> getCarsInCart(User user) {
        // Appeler le repository pour obtenir la liste des voitures dans le panier
        return cartRepository.findCarsInCart(user);
    }


    public void removeCarFromCart(User user, String carBrandModel) {
        Cart cart = cartRepository.findCartByUser(user);
        if (cart != null) {
            Car carToRemove = cart.getCars().stream()
                    .filter(car -> (car.getBrand() + " " + car.getModel()).equals(carBrandModel))
                    .findFirst()
                    .orElse(null);

            if (carToRemove != null) {
                cart.removeCar(carToRemove);
                cartRepository.update(cart);
            }
        }
    }

    public double getCartTotal(User user) {
        Cart cart = cartRepository.findCartByUser(user);
        return (cart != null) ? cart.getCars().stream().mapToDouble(Car::getPrice).sum() : 0;
    }


}
