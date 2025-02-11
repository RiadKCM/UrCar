package fr.parisdauphine.service;

import fr.parisdauphine.entity.Car;
import fr.parisdauphine.entity.Cart;
import fr.parisdauphine.entity.Order;
import fr.parisdauphine.entity.User;
import fr.parisdauphine.repository.CarRepository;
import fr.parisdauphine.repository.CartRepository;
import fr.parisdauphine.repository.OrderRepository;
import org.hibernate.Hibernate;

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
    public Order placeOrder(User user) {
        Cart cart = cartRepository.findCartByUser(user);
        if (cart == null || cart.getCars().isEmpty()) {
            throw new IllegalStateException("Le panier est vide, impossible de créer une commande.");
        }
        // Charger explicitement les voitures pour éviter le lazy loading
        Hibernate.initialize(cart.getCars());
    
        // Vérifier que chaque voiture a bien ses attributs chargés
        List<Car> loadedCars = new ArrayList<>();
        for (Car car : cart.getCars()) {
            loadedCars.add(carRepository.findById(car.getId()));  // Charger chaque voiture en base
        }
        // Créer et enregistrer la commande
        Order order = new Order();
        order.setUser(user);
        order.setCars(loadedCars);
        order.setOrderDate(java.time.LocalDateTime.now());
        order.setStatus(Order.Status.EN_COURS);
    
        orderRepository.save(order); // Sauvegarde en base
        carRepository.updateCarStatusToSold(loadedCars);
    
        // Nettoyer le panier après commande
        cart.getCars().clear();
        cartRepository.update(cart);
    
        return order;  // ✅ Retourne l'objet `Order` complet
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
        if (cart == null || cart.getCars().isEmpty()) {
            return 0.0;
        }
        // Charger chaque voiture pour s'assurer d'avoir le prix
        double total = 0;
        for (Car car : cart.getCars()) {
            Car fullCar = carRepository.findById(car.getId());
            total += fullCar.getPrice();
        }
        return total;
    }    
}
