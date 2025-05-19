package app.injectorHandler;

import app.controllers.AdminController;
import app.controllers.OrderController;
import app.persistence.*;
import app.service.CalculateBOM;

public class DependencyInjector {
    private final ConnectionPool connectionPool;

    // Mappers
    private final ProductMapper productMapper;
    private final AdminMapper adminMapper;
    private final OrderMapper orderMapper;
    private final CustomerMapper customerMapper;
    private final PostalCodeMapper postalCodeMapper;

    // Services
    private final CalculateBOM calculateBOM;


    // Controllers
    private final OrderController orderController;
    private final AdminController adminController;

    public DependencyInjector(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
      
        this.productMapper = new ProductMapper(connectionPool);
        this.orderMapper = new OrderMapper(connectionPool);
        this.adminMapper = new AdminMapper(connectionPool);
        this.postalCodeMapper = new PostalCodeMapper(connectionPool);
        this.customerMapper = new CustomerMapper(connectionPool);
        this.calculateBOM = new CalculateBOM(productMapper);

        this.adminController = new AdminController(adminMapper, customerMapper, productMapper);
        this.orderController = new OrderController(orderMapper, connectionPool, customerMapper, calculateBOM, postalCodeMapper);
    }

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    public ProductMapper getProductMapper() {
        return productMapper;
    }

    public AdminMapper getAdminMapper() {
        return adminMapper;
    }

    public OrderMapper getOrderMapper() {
        return orderMapper;
    }

    public CustomerMapper getCustomerMapper() {
        return customerMapper;
    }

    public CalculateBOM getCalculateBOM() {
        return calculateBOM;
    }

    public OrderController getOrderController() {
        return orderController;
    }

    public AdminController getAdminController() {
        return adminController;
    }
}
